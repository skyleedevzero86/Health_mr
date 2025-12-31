package com.sleekydz86.core.security.masking.util;

public class DataMaskingUtil {

    private static final String MASKING_CHAR = "*";

    public static String maskRRN(String rrn) {
        if (rrn == null || rrn.length() < 8) {
            return rrn;
        }
        return rrn.substring(0, 8) + "******";
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.length() < 4) {
            return phone;
        }
        int visibleLength = Math.min(4, cleaned.length() / 2);
        String visible = cleaned.substring(cleaned.length() - visibleLength);
        String masked = MASKING_CHAR.repeat(cleaned.length() - visibleLength);
        return masked + visible;
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return email;
        }
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return MASKING_CHAR.repeat(localPart.length()) + "@" + domain;
        }
        return localPart.substring(0, 2) + MASKING_CHAR.repeat(3) + "@" + domain;
    }

    public static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + MASKING_CHAR;
        }
        return name.charAt(0) + MASKING_CHAR.repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    public static String maskAddress(String address) {
        if (address == null || address.length() <= 3) {
            return address;
        }
        int visibleLength = Math.min(6, address.length() / 2);
        return address.substring(0, visibleLength) + " " + MASKING_CHAR.repeat(3);
    }

    public static String maskAccount(String account) {
        if (account == null || account.length() < 4) {
            return account;
        }
        String cleaned = account.replaceAll("[^0-9]", "");
        if (cleaned.length() < 4) {
            return account;
        }
        int visibleLength = Math.min(4, cleaned.length() / 2);
        String visible = cleaned.substring(cleaned.length() - visibleLength);
        String masked = MASKING_CHAR.repeat(cleaned.length() - visibleLength);
        return masked + visible;
    }
    public static String maskDefault(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return MASKING_CHAR.repeat(Math.min(value.length(), 10));
    }
}

