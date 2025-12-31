package com.sleekydz86.core.security.validation;

import java.nio.file.Paths;
import java.util.regex.Pattern;

public class InputValidationUtil {

    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script|javascript:|onerror=|onload=|onclick=|onmouseover=|onfocus=|onblur=)"
    );

    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(\\.\\./|\\.\\.\\\\)");

    private static final Pattern COMMAND_INJECTION_PATTERN = Pattern.compile(
            "[;&|`$(){}]"
    );

    public static boolean containsXSS(String input) {
        if (input == null) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }


    public static boolean containsPathTraversal(String input) {
        if (input == null) {
            return false;
        }
        return PATH_TRAVERSAL_PATTERN.matcher(input).find();
    }

    public static boolean containsCommandInjection(String input) {
        if (input == null) {
            return false;
        }
        return COMMAND_INJECTION_PATTERN.matcher(input).find();
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        return input.replaceAll("<[^>]*>", "")
                .replaceAll("javascript:", "")
                .replaceAll("on\\w+=", "");
    }

    public static String normalizeFilename(String filename) {
        if (filename == null) {
            return null;
        }

        String normalized = filename.replaceAll("(\\.\\./|\\.\\.\\\\)", "");

        normalized = normalized.replaceAll("[/\\\\]", "");

        normalized = normalized.replaceAll("[^a-zA-Z0-9._-]", "");
        return normalized;
    }

    public static boolean isValidPath(String path) {
        if (path == null) {
            return false;
        }
        try {
            Paths.get(path).normalize();
            return !containsPathTraversal(path);
        } catch (Exception e) {
            return false;
        }
    }
}

