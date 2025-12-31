package com.sleekydz86.core.utils.string;

import java.util.regex.Pattern;

public class StringUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$"
    );

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String trim(String str) {
        return str != null ? str.trim() : null;
    }

    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.replaceAll("[^0-9-]", "")).matches();
    }

    public static String encode(String str) {
        if (str == null) {
            return null;
        }
        try {
            return java.net.URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            return str;
        }
    }

    public static String decode(String str) {
        if (str == null) {
            return null;
        }
        try {
            return java.net.URLDecoder.decode(str, "UTF-8");
        } catch (Exception e) {
            return str;
        }
    }
}

