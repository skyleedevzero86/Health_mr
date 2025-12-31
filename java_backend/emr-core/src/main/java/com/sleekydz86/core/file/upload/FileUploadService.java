package com.sleekydz86.core.file.upload;

import com.sleekydz86.core.file.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final FileStorageService fileStorageService;

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,application/pdf,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet}")
    private String allowedTypes;

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;

    public String uploadFile(MultipartFile file) throws IOException {
        validateFile(file);
        validateFileType(file);
        validateFileName(file.getOriginalFilename());

        return fileStorageService.saveFile(file);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 제한을 초과했습니다. 최대 크기: " + maxFileSize + " bytes");
        }
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));

        if (contentType == null || !allowedTypeList.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않은 파일 타입입니다. 허용 타입: " + allowedTypes);
        }
    }

    private void validateFileName(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("파일명이 비어있습니다.");
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("잘못된 파일명입니다.");
        }

        // 특수문자 제거
        // filename = filename.replaceAll("[^a-zA-Z0-9._-]", "");
    }
}

