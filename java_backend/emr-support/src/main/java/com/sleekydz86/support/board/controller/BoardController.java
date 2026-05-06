package com.sleekydz86.support.board.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.support.board.dto.request.BoardCreateRequest;
import com.sleekydz86.support.board.dto.request.BoardUpdateRequest;
import com.sleekydz86.support.board.dto.response.BoardDetailResponse;
import com.sleekydz86.support.board.dto.response.BoardFileResponse;
import com.sleekydz86.support.board.dto.response.BoardListResponse;
import com.sleekydz86.support.board.dto.response.BoardResponse;
import com.sleekydz86.support.board.dto.response.BoardViewerResponse;
import com.sleekydz86.support.board.service.BoardService;
import com.sleekydz86.support.board.type.BoardType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    @AuthRole
    public ResponseEntity<Map<String, Object>> createBoard(
            @AuthUser Long userId,
            @Valid @RequestBody BoardCreateRequest request) {
        BoardResponse response = boardService.createBoard(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "게시판 작성 성공",
                "data", response
        ));
    }

    @GetMapping("/{boardId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getBoard(
            @AuthUser Long userId,
            @PathVariable Long boardId) {
        BoardDetailResponse response = boardService.getBoard(boardId, userId);
        return ResponseEntity.ok(Map.of(
                "message", "게시판 조회 성공",
                "data", response
        ));
    }

    @GetMapping
    @AuthRole
    public ResponseEntity<Map<String, Object>> getBoards(
            @AuthUser Long userId,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 20) Pageable pageable) {
        BoardListResponse response = boardService.getBoards(boardType, departmentId, pageable, userId);
        return ResponseEntity.ok(Map.of(
                "message", "게시판 목록 조회 성공",
                "data", response
        ));
    }

    @PutMapping("/{boardId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> updateBoard(
            @AuthUser Long userId,
            @PathVariable Long boardId,
            @Valid @RequestBody BoardUpdateRequest request) {
        BoardResponse response = boardService.updateBoard(boardId, userId, request);
        return ResponseEntity.ok(Map.of(
                "message", "게시판 수정 성공",
                "data", response
        ));
    }

    @DeleteMapping("/{boardId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> deleteBoard(
            @AuthUser Long userId,
            @PathVariable Long boardId) {
        boardService.deleteBoard(boardId, userId);
        return ResponseEntity.ok(Map.of(
                "message", "게시판 삭제 성공"
        ));
    }

    @PostMapping("/{boardId}/like")
    @AuthRole
    public ResponseEntity<Map<String, Object>> toggleLike(
            @AuthUser Long userId,
            @PathVariable Long boardId) {
        boardService.toggleLike(boardId, userId);
        return ResponseEntity.ok(Map.of(
                "message", "좋아요 처리 성공"
        ));
    }

    @GetMapping("/{boardId}/viewers")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getBoardViewers(
            @AuthUser Long userId,
            @PathVariable Long boardId) {
        List<BoardViewerResponse> responses = boardService.getBoardViewers(boardId, userId);
        return ResponseEntity.ok(Map.of(
                "message", "조회자 목록 조회 성공",
                "data", responses
        ));
    }

    @GetMapping("/notices/alert")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getAlertNotices() {
        List<BoardResponse> responses = boardService.getAlertNotices();
        return ResponseEntity.ok(Map.of(
                "message", "알림공지 조회 성공",
                "data", responses
        ));
    }

    @PostMapping("/{boardId}/files")
    @AuthRole
    public ResponseEntity<Map<String, Object>> uploadFile(
            @AuthUser Long userId,
            @PathVariable Long boardId,
            @RequestParam("file") MultipartFile file) throws IOException {
        BoardFileResponse response = boardService.uploadFile(boardId, userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "파일 업로드 성공",
                "data", response
        ));
    }

    @GetMapping("/files/{fileId}/download")
    @AuthRole
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        com.sleekydz86.support.board.entity.BoardFileEntity boardFile = 
                boardService.getBoardFile(fileId);
        byte[] fileData = boardService.downloadFile(fileId);
        
        ByteArrayResource resource = new ByteArrayResource(fileData);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + boardFile.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(boardFile.getContentType() != null ? 
                        boardFile.getContentType() : "application/octet-stream"))
                .body(resource);
    }

    @DeleteMapping("/files/{fileId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> deleteFile(
            @AuthUser Long userId,
            @PathVariable Long fileId) {
        boardService.deleteFile(fileId, userId);
        return ResponseEntity.ok(Map.of(
                "message", "파일 삭제 성공"
        ));
    }
}

