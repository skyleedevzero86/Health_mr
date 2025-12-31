package com.sleekydz86.core.file.download;

import com.sleekydz86.core.file.storage.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private final FileStorageService fileStorageService;


    public void downloadFile(String filePath, String originalFilename, HttpServletResponse response) throws IOException {
        if (!fileStorageService.exists(filePath)) {
            throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + filePath);
        }

        byte[] fileBytes = fileStorageService.readFile(filePath);
        String contentType = getContentType(originalFilename);

        String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        response.setContentType(contentType);
        response.setContentLength(fileBytes.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");
        response.setHeader("Content-Transfer-Encoding", "binary");

        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(fileBytes);
            outputStream.flush();
        }
    }

    private String getContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "xlsx", "xls" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}

