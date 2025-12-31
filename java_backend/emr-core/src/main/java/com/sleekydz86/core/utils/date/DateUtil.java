package com.sleekydz86.core.utils.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    public static LocalDate addDays(LocalDate date, long days) {
        return date != null ? date.plusDays(days) : null;
    }

    public static LocalDate subtractDays(LocalDate date, long days) {
        return date != null ? date.minusDays(days) : null;
    }

    public static boolean isAfter(LocalDate date1, LocalDate date2) {
        return date1 != null && date2 != null && date1.isAfter(date2);
    }

    public static boolean isBefore(LocalDate date1, LocalDate date2) {
        return date1 != null && date2 != null && date1.isBefore(date2);
    }

    public static long daysBetween(LocalDate date1, LocalDate date2) {
        return date1 != null && date2 != null ? ChronoUnit.DAYS.between(date1, date2) : 0;
    }

    public static int getDayOfWeek(LocalDate date) {
        return date != null ? date.getDayOfWeek().getValue() : 0;
    }
}

