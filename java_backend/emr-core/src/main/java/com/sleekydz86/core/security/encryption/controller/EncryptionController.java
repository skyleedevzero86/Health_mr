package com.sleekydz86.core.security.encryption.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.security.encryption.EncryptionService;
import com.sleekydz86.core.security.encryption.dto.DecryptRequest;
import com.sleekydz86.core.security.encryption.dto.DecryptResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/encryption")
@RequiredArgsConstructor
public class EncryptionController {

    private final EncryptionService encryptionService;

    @PostMapping("/decrypt")
    @AuthRole
    public ResponseEntity<DecryptResponse> decrypt(@Valid @RequestBody DecryptRequest request) {
        try {
            String decryptedText = encryptionService.decrypt(request.getEncryptedText());
            return ResponseEntity.ok(DecryptResponse.of(decryptedText));
        } catch (Exception e) {
            throw new IllegalArgumentException("복호화에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/encrypt")
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<DecryptResponse> encrypt(@Valid @RequestBody DecryptRequest request) {
        try {
            String encryptedText = encryptionService.encrypt(request.getEncryptedText());
            return ResponseEntity.ok(DecryptResponse.of(encryptedText));
        } catch (Exception e) {
            throw new IllegalArgumentException("암호화에 실패했습니다: " + e.getMessage());
        }
    }
}

