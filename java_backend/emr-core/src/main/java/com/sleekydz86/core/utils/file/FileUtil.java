package com.sleekydz86.core.utils.file;

import java.io.File;

public class FileUtil {

    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public static boolean isValidFileType(String filename, String[] allowedExtensions) {
        if (filename == null || allowedExtensions == null) {
            return false;
        }
        String extension = getFileExtension(filename);
        for (String allowed : allowedExtensions) {
            if (extension.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    public static String normalizeFilename(String filename) {
        if (filename == null) {
            return null;
        }
        // 특수문자 제거 (알파벳, 숫자, 점, 하이픈, 언더스코어만 허용)
        return filename.replaceAll("[^a-zA-Z0-9._-]", "");
    }

    public static boolean fileExists(String filePath) {
        if (filePath == null) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }
}

