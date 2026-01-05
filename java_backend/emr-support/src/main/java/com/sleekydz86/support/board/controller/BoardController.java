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
        try {
            BoardResponse response = boardService.createBoard(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "게시판 작성 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "게시판 작성 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{boardId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getBoard(
            @AuthUser Long userId,
            @PathVariable Long boardId) {
        try {
            BoardDetailResponse response = boardService.getBoard(boardId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "게시판 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "게시판 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping
    @AuthRole
    public ResponseEntity<Map<String, Object>> getBoards(
            @AuthUser Long userId,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            BoardListResponse response = boardService.getBoards(boardType, departmentId, pageable, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "게시판 목록 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "게시판 목록 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{boardId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> updateBoard(
            @AuthUser Long userId,
            @PathVariable Long boardId,
            @Valid @RequestBody BoardUpdateRequest request) {
        try {
            BoardResponse response = boardService.updateBoard(boardId, userId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "게시판 수정 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "게시판 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{boardId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> deleteBoard(
            @AuthUser Long userId,
            @PathVariable Long boardId) {
        try {
            boardService.deleteBoard(boardId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "게시판 삭제 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "게시판 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/{boardId}/like")
    @AuthRole
    public ResponseEntity<Map<String, Object>> toggleLike(
            @AuthUser Long userId,
            @PathVariable Long boardId) {
        try {
            boardService.toggleLike(boardId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "좋아요 처리 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "좋아요 처리 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{boardId}/viewers")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getBoardViewers(
            @AuthUser Long userId,
            @PathVariable Long boardId) {
        try {
            List<BoardViewerResponse> responses = boardService.getBoardViewers(boardId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "조회자 목록 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "조회자 목록 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/notices/alert")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getAlertNotices() {
        try {
            List<BoardResponse> responses = boardService.getAlertNotices();
            return ResponseEntity.ok(Map.of(
                    "message", "알림공지 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "알림공지 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/{boardId}/files")
    @AuthRole
    public ResponseEntity<Map<String, Object>> uploadFile(
            @AuthUser Long userId,
            @PathVariable Long boardId,
            @RequestParam("file") MultipartFile file) {
        try {
            BoardFileResponse response = boardService.uploadFile(boardId, userId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "파일 업로드 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "파일 업로드 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/files/{fileId}/download")
    @AuthRole
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/files/{fileId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> deleteFile(
            @AuthUser Long userId,
            @PathVariable Long fileId) {
        try {
            boardService.deleteFile(fileId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "파일 삭제 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "파일 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

