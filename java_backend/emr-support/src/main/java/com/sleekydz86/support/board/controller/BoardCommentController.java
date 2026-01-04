package com.sleekydz86.support.board.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.support.board.dto.request.CommentCreateRequest;
import com.sleekydz86.support.board.dto.response.CommentResponse;
import com.sleekydz86.support.board.service.BoardCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boards/{boardId}/comments")
@RequiredArgsConstructor
public class BoardCommentController {

    private final BoardCommentService boardCommentService;

    @PostMapping
    @AuthRole
    public ResponseEntity<Map<String, Object>> createComment(
            @AuthUser Long userId,
            @PathVariable Long boardId,
            @Valid @RequestBody CommentCreateRequest request) {
        try {
            CommentResponse response = boardCommentService.createComment(boardId, userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "댓글 작성 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "댓글 작성 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping
    @AuthRole
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Long boardId) {
        try {
            List<CommentResponse> responses = boardCommentService.getComments(boardId);
            return ResponseEntity.ok(Map.of(
                    "message", "댓글 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "댓글 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{commentId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> updateComment(
            @AuthUser Long userId,
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentCreateRequest request) {
        try {
            CommentResponse response = boardCommentService.updateComment(commentId, userId, request.getContent());
            return ResponseEntity.ok(Map.of(
                    "message", "댓글 수정 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "댓글 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{commentId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> deleteComment(
            @AuthUser Long userId,
            @PathVariable Long boardId,
            @PathVariable Long commentId) {
        try {
            boardCommentService.deleteComment(commentId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "댓글 삭제 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "댓글 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

