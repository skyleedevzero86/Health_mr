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
        MessageResponse response = messageService.sendMessage(senderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "메시지 전송 성공",
                "data", response
        ));
    }

    @GetMapping("/{messageId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getMessage(
            @AuthUser Long userId,
            @PathVariable Long messageId) {
        MessageResponse response = messageService.getMessage(messageId, userId);
        return ResponseEntity.ok(Map.of(
                "message", "메시지 조회 성공",
                "data", response
        ));
    }

    @GetMapping("/sent")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getSentMessages(@AuthUser Long userId) {
        MessageListResponse response = messageService.getSentMessages(userId);
        return ResponseEntity.ok(Map.of(
                "message", "보낸 메시지 조회 성공",
                "data", response
        ));
    }

    @GetMapping("/received")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getReceivedMessages(@AuthUser Long userId) {
        MessageListResponse response = messageService.getReceivedMessages(userId);
        return ResponseEntity.ok(Map.of(
                "message", "받은 메시지 조회 성공",
                "data", response
        ));
    }

    @GetMapping("/all")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getAllMessages(@AuthUser Long userId) {
        MessageListResponse response = messageService.getAllMessages(userId);
        return ResponseEntity.ok(Map.of(
                "message", "전체 메시지 조회 성공",
                "data", response
        ));
    }

    @DeleteMapping("/{messageId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @AuthUser Long userId,
            @PathVariable Long messageId) {
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.ok(Map.of(
                "message", "메시지 삭제 성공"
        ));
    }
}

