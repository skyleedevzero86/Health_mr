package com.sleekydz86.support.board.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.support.board.dto.request.CommentCreateRequest;
import com.sleekydz86.support.board.dto.response.CommentResponse;
import com.sleekydz86.support.board.entity.BoardCommentEntity;
import com.sleekydz86.support.board.entity.BoardEntity;
import com.sleekydz86.support.board.repository.BoardCommentRepository;
import com.sleekydz86.support.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCommentService implements BaseService<BoardCommentEntity, Long> {

    private final BoardCommentRepository boardCommentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse createComment(Long boardId, Long userId, CommentCreateRequest request) {
        BoardEntity board = boardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new NotFoundException("게시판을 찾을 수 없습니다."));

        if (board.getDeleted()) {
            throw new NotFoundException("삭제된 게시판입니다.");
        }

        UserEntity author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        BoardCommentEntity parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = boardCommentRepository.findByCommentId(request.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException("부모 댓글을 찾을 수 없습니다."));
        }

        BoardCommentEntity comment = BoardCommentEntity.builder()
                .board(board)
                .author(author)
                .parentComment(parentComment)
                .content(request.getContent())
                .build();

        BoardCommentEntity saved = boardCommentRepository.save(comment);
        board.increaseCommentCount();
        boardRepository.save(board);

        return CommentResponse.from(saved);
    }

    public List<CommentResponse> getComments(Long boardId) {
        List<BoardCommentEntity> comments = boardCommentRepository
                .findByBoard_BoardIdAndDeletedFalseOrderByCreatedAtAsc(boardId);
        return comments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, String content) {
        BoardCommentEntity comment = boardCommentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        if (comment.getDeleted()) {
            throw new NotFoundException("삭제된 댓글입니다.");
        }

        if (!comment.isWrittenBy(userId)) {
            throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
        }

        comment.update(content);
        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        BoardCommentEntity comment = boardCommentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.isWrittenBy(userId)) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }

        comment.delete();
        comment.getBoard().decreaseCommentCount();
        boardRepository.save(comment.getBoard());
    }

}

