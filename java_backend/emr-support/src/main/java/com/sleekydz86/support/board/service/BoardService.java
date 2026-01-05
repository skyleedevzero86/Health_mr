package com.sleekydz86.support.board.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.core.tenant.TenantContext;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.repository.DepartmentRepository;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.support.board.dto.request.BoardCreateRequest;
import com.sleekydz86.support.board.dto.request.BoardUpdateRequest;
import com.sleekydz86.core.file.upload.FileUploadService;
import com.sleekydz86.core.file.storage.FileStorageService;
import com.sleekydz86.support.board.dto.response.BoardDetailResponse;
import com.sleekydz86.support.board.dto.response.BoardFileResponse;
import com.sleekydz86.support.board.dto.response.BoardListResponse;
import com.sleekydz86.support.board.dto.response.BoardResponse;
import com.sleekydz86.support.board.dto.response.BoardViewerResponse;
import com.sleekydz86.support.board.entity.BoardEntity;
import com.sleekydz86.support.board.entity.BoardFileEntity;
import com.sleekydz86.support.board.entity.BoardHashtagEntity;
import com.sleekydz86.support.board.entity.BoardViewEntity;
import com.sleekydz86.support.board.factory.BoardFactory;
import com.sleekydz86.support.board.factory.HashtagFactory;
import com.sleekydz86.support.board.repository.BoardCommentRepository;
import com.sleekydz86.support.board.repository.BoardFileRepository;
import com.sleekydz86.support.board.repository.BoardHashtagRepository;
import com.sleekydz86.support.board.repository.BoardLikeRepository;
import com.sleekydz86.support.board.repository.BoardRepository;
import com.sleekydz86.support.board.repository.BoardViewRepository;
import com.sleekydz86.support.board.service.domain.BoardDomainService;
import com.sleekydz86.support.board.strategy.BoardPermissionStrategy;
import com.sleekydz86.support.board.type.BoardType;
import com.sleekydz86.support.board.type.NoticeType;
import com.sleekydz86.support.board.valueobject.Hashtag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService implements BaseService<BoardEntity, Long> {

    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardHashtagRepository boardHashtagRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardViewRepository boardViewRepository;
    private final BoardFileRepository boardFileRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final BoardFactory boardFactory;
    private final HashtagFactory hashtagFactory;
    private final BoardDomainService boardDomainService;
    private final List<BoardPermissionStrategy> permissionStrategies;
    private final EventPublisher eventPublisher;
    private final FileUploadService fileUploadService;
    private final FileStorageService fileStorageService;

    @Transactional
    public BoardResponse createBoard(Long userId, BoardCreateRequest request) {
        UserEntity author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        DepartmentEntity department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new NotFoundException("부서를 찾을 수 없습니다."));
        }

        boardDomainService.validateBoardCreate(author, request.getBoardType(), request.getDepartmentId());

        BoardEntity board = boardFactory.create(
                author,
                department,
                request.getBoardType(),
                request.getNoticeType(),
                request.getTitle(),
                request.getContent()
        );

        BoardEntity saved = boardRepository.save(board);

        if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
            List<Hashtag> hashtags = hashtagFactory.createFromStrings(request.getHashtags());
            for (Hashtag hashtag : hashtags) {
                BoardHashtagEntity boardHashtag = BoardHashtagEntity.builder()
                        .board(saved)
                        .hashtag(hashtag)
                        .build();
                boardHashtagRepository.save(boardHashtag);
            }
        }

        eventPublisher.publish(new com.sleekydz86.core.event.domain.BoardCreatedEvent(
                saved.getBoardId(),
                saved.getAuthor().getId(),
                saved.getAuthor().getName(),
                saved.getBoardType().name(),
                saved.getTitle(),
                saved.getCreatedAt()
        ));

        Long fileCount = boardFileRepository.countByBoard_BoardIdAndDeletedFalse(saved.getBoardId());
        return BoardResponse.from(saved, getHashtags(saved.getBoardId()), fileCount);
    }

    public BoardDetailResponse getBoard(Long boardId, Long userId) {
        BoardEntity board = boardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new NotFoundException("게시판을 찾을 수 없습니다."));

        if (board.getDeleted()) {
            throw new NotFoundException("삭제된 게시판입니다.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        Long departmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        BoardPermissionStrategy strategy = findStrategy(board.getBoardType());
        
        if (!strategy.canRead(board, user, departmentId)) {
            throw new IllegalArgumentException("게시판을 조회할 권한이 없습니다.");
        }

        if (!boardViewRepository.existsByBoard_BoardIdAndUser_Id(boardId, userId)) {
            BoardViewEntity boardView = BoardViewEntity.builder()
                    .board(board)
                    .user(user)
                    .build();
            boardViewRepository.save(boardView);
        }

        board.increaseViewCount();
        boardRepository.save(board);

        List<String> hashtags = getHashtags(boardId);
        List<com.sleekydz86.support.board.dto.response.CommentResponse> comments = getComments(boardId);
        List<BoardFileResponse> files = getFiles(boardId);
        Boolean isLiked = boardLikeRepository.existsByBoard_BoardIdAndUser_Id(boardId, userId);

        return BoardDetailResponse.from(board, hashtags, comments, files, isLiked);
    }

    public BoardListResponse getBoards(BoardType boardType, Long departmentId, Pageable pageable, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        Page<BoardEntity> boardPage;
        
        if (boardType == BoardType.DEPARTMENT && departmentId != null) {
            DepartmentEntity department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new NotFoundException("부서를 찾을 수 없습니다."));
            boardPage = boardRepository.findByBoardTypeAndDepartmentAndDeletedFalse(boardType, department, pageable);
        } else if (boardType != null) {
            boardPage = boardRepository.findByBoardTypeAndDeletedFalse(boardType, pageable);
        } else {
            boardPage = boardRepository.findByDeletedFalse(pageable);
        }

        List<BoardResponse> responses = boardPage.getContent().stream()
                .map(board -> {
                    Long fileCount = boardFileRepository.countByBoard_BoardIdAndDeletedFalse(board.getBoardId());
                    return BoardResponse.from(board, getHashtags(board.getBoardId()), fileCount);
                })
                .collect(Collectors.toList());

        return new BoardListResponse(boardPage.getTotalElements(), responses);
    }

    @Transactional
    public BoardResponse updateBoard(Long boardId, Long userId, BoardUpdateRequest request) {
        BoardEntity board = boardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new NotFoundException("게시판을 찾을 수 없습니다."));

        if (board.getDeleted()) {
            throw new NotFoundException("삭제된 게시판입니다.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        BoardPermissionStrategy strategy = findStrategy(board.getBoardType());
        if (!strategy.canUpdate(board, user)) {
            throw new IllegalArgumentException("게시판을 수정할 권한이 없습니다.");
        }

        board.update(request.getTitle(), request.getContent());

        boardHashtagRepository.findByBoard_BoardId(boardId).forEach(boardHashtagRepository::delete);

        if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
            List<Hashtag> hashtags = hashtagFactory.createFromStrings(request.getHashtags());
            for (Hashtag hashtag : hashtags) {
                BoardHashtagEntity boardHashtag = BoardHashtagEntity.builder()
                        .board(board)
                        .hashtag(hashtag)
                        .build();
                boardHashtagRepository.save(boardHashtag);
            }
        }

        Long fileCount = boardFileRepository.countByBoard_BoardIdAndDeletedFalse(boardId);
        return BoardResponse.from(board, getHashtags(boardId), fileCount);
    }

    @Transactional
    public void deleteBoard(Long boardId, Long userId) {
        BoardEntity board = boardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new NotFoundException("게시판을 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        BoardPermissionStrategy strategy = findStrategy(board.getBoardType());
        if (!strategy.canDelete(board, user)) {
            throw new IllegalArgumentException("게시판을 삭제할 권한이 없습니다.");
        }

        board.delete();
    }

    @Transactional
    public void toggleLike(Long boardId, Long userId) {
        BoardEntity board = boardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new NotFoundException("게시판을 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        boardLikeRepository.findByBoard_BoardIdAndUser_Id(boardId, userId)
                .ifPresentOrElse(
                        like -> {
                            boardLikeRepository.delete(like);
                            board.decreaseLikeCount();
                        },
                        () -> {
                            com.sleekydz86.support.board.entity.BoardLikeEntity newLike = com.sleekydz86.support.board.entity.BoardLikeEntity.builder()
                                    .board(board)
                                    .user(user)
                                    .build();
                            boardLikeRepository.save(newLike);
                            board.increaseLikeCount();
                        }
                );

        boardRepository.save(board);
    }

    private List<String> getHashtags(Long boardId) {
        return boardHashtagRepository.findByBoard_BoardId(boardId).stream()
                .map(hashtag -> hashtag.getHashtag().getValue())
                .collect(Collectors.toList());
    }

    private List<com.sleekydz86.support.board.dto.response.CommentResponse> getComments(Long boardId) {
        List<com.sleekydz86.support.board.entity.BoardCommentEntity> comments = 
                boardCommentRepository.findByBoard_BoardIdAndDeletedFalseOrderByCreatedAtAsc(boardId);
        
        return comments.stream()
                .filter(comment -> !comment.isReply())
                .map(comment -> {
                    List<com.sleekydz86.support.board.dto.response.CommentResponse> replies = comments.stream()
                            .filter(reply -> reply.isReply() && 
                                    reply.getParentComment() != null && 
                                    reply.getParentComment().getCommentId().equals(comment.getCommentId()))
                            .map(com.sleekydz86.support.board.dto.response.CommentResponse::from)
                            .collect(Collectors.toList());
                    return com.sleekydz86.support.board.dto.response.CommentResponse.from(comment, replies);
                })
                .collect(Collectors.toList());
    }

    public List<BoardViewerResponse> getBoardViewers(Long boardId, Long userId) {
        BoardEntity board = boardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new NotFoundException("게시판을 찾을 수 없습니다."));

        if (board.getBoardType() != BoardType.NOTICE) {
            throw new IllegalArgumentException("공지게시판만 조회자 목록을 볼 수 있습니다.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        if (!user.isAdmin() && !board.isWrittenBy(userId)) {
            throw new IllegalArgumentException("관리자 또는 작성자만 조회자 목록을 볼 수 있습니다.");
        }

        List<BoardViewEntity> views = boardViewRepository.findByBoard_BoardIdOrderByViewedAtDesc(boardId);
        return views.stream()
                .map(BoardViewerResponse::from)
                .collect(Collectors.toList());
    }

    public List<BoardResponse> getAlertNotices() {
        List<BoardEntity> alertBoards = boardRepository.findByBoardTypeAndDeletedFalse(
                BoardType.NOTICE, 
                org.springframework.data.domain.PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent().stream()
                .filter(board -> board.getNoticeType() == NoticeType.ALERT)
                .limit(3)
                .collect(Collectors.toList());

        return alertBoards.stream()
                .map(board -> {
                    Long fileCount = boardFileRepository.countByBoard_BoardIdAndDeletedFalse(board.getBoardId());
                    return BoardResponse.from(board, getHashtags(board.getBoardId()), fileCount);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardFileResponse uploadFile(Long boardId, Long userId, MultipartFile file) throws IOException {
        BoardEntity board = boardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new NotFoundException("게시판을 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        BoardPermissionStrategy strategy = findStrategy(board.getBoardType());
        if (!strategy.canUpdate(board, user)) {
            throw new IllegalArgumentException("파일을 업로드할 권한이 없습니다.");
        }

        String filePath = fileUploadService.uploadFile(file);

        BoardFileEntity boardFile = BoardFileEntity.builder()
                .board(board)
                .originalFilename(file.getOriginalFilename())
                .savedFilename(filePath.substring(filePath.lastIndexOf("/") + 1))
                .filePath(filePath)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        BoardFileEntity saved = boardFileRepository.save(boardFile);
        return BoardFileResponse.from(saved);
    }

    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        BoardFileEntity boardFile = boardFileRepository.findByFileId(fileId)
                .orElseThrow(() -> new NotFoundException("파일을 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        BoardPermissionStrategy strategy = findStrategy(boardFile.getBoard().getBoardType());
        if (!strategy.canUpdate(boardFile.getBoard(), user)) {
            throw new IllegalArgumentException("파일을 삭제할 권한이 없습니다.");
        }

        try {
            fileStorageService.deleteFile(boardFile.getFilePath());
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }

        boardFile.delete();
        boardFileRepository.save(boardFile);
    }

    public byte[] downloadFile(Long fileId) throws IOException {
        BoardFileEntity boardFile = boardFileRepository.findByFileId(fileId)
                .orElseThrow(() -> new NotFoundException("파일을 찾을 수 없습니다."));

        if (boardFile.getDeleted()) {
            throw new NotFoundException("삭제된 파일입니다.");
        }

        return fileStorageService.readFile(boardFile.getFilePath());
    }

    public BoardFileEntity getBoardFile(Long fileId) {
        return boardFileRepository.findByFileId(fileId)
                .orElseThrow(() -> new NotFoundException("파일을 찾을 수 없습니다."));
    }

    private List<BoardFileResponse> getFiles(Long boardId) {
        return boardFileRepository.findByBoard_BoardIdAndDeletedFalse(boardId).stream()
                .map(BoardFileResponse::from)
                .collect(Collectors.toList());
    }

    private BoardPermissionStrategy findStrategy(BoardType boardType) {
        return permissionStrategies.stream()
                .filter(strategy -> strategy.supports(boardType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 게시판 타입입니다: " + boardType));
    }
}

