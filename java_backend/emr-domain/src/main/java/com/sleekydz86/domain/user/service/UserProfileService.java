package com.sleekydz86.domain.user.service;

import com.sleekydz86.core.file.upload.FileUploadService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new com.sleekydz86.core.common.exception.custom.NotFoundException(
                        "사용자를 찾을 수 없습니다. ID: " + userId));

        String imagePath = fileUploadService.uploadFile(file);

        return imagePath;
    }

    @Transactional
    public void updateProfile(Long userId, String name, String email, String telNum) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new com.sleekydz86.core.common.exception.custom.NotFoundException(
                        "사용자를 찾을 수 없습니다. ID: " + userId));

        if (name != null && !name.isBlank()) {
            user.updateProfile(name, user.getGender(), user.getAddress());
        }
        if (email != null && !email.isBlank()) {
            user.changeEmail(com.sleekydz86.domain.common.valueobject.Email.of(email));
        }
        if (telNum != null && !telNum.isBlank()) {
            user.changePhoneNumber(com.sleekydz86.domain.common.valueobject.PhoneNumber.of(telNum));
        }

        userRepository.save(user);
    }
}

