package com.sleekydz86.domain.message.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.domain.message.dto.MessageListResponse;
import com.sleekydz86.domain.message.dto.MessageResponse;
import com.sleekydz86.domain.message.dto.MessageSendRequest;
import com.sleekydz86.domain.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @AuthRole
    public ResponseEntity<Map<String, Object>> sendMessage(
            @AuthUser Long senderId,
            @Valid @RequestBody MessageSendRequest request) {
        try {
            MessageResponse response = messageService.sendMessage(senderId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "메시지 전송 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "메시지 전송 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{messageId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getMessage(
            @AuthUser Long userId,
            @PathVariable Long messageId) {
        try {
            MessageResponse response = messageService.getMessage(messageId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "메시지 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "메시지 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/sent")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getSentMessages(@AuthUser Long userId) {
        try {
            MessageListResponse response = messageService.getSentMessages(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "보낸 메시지 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "메시지 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/received")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getReceivedMessages(@AuthUser Long userId) {
        try {
            MessageListResponse response = messageService.getReceivedMessages(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "받은 메시지 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "메시지 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/all")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getAllMessages(@AuthUser Long userId) {
        try {
            MessageListResponse response = messageService.getAllMessages(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "전체 메시지 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "메시지 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{messageId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @AuthUser Long userId,
            @PathVariable Long messageId) {
        try {
            messageService.deleteMessage(messageId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "메시지 삭제 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "메시지 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

