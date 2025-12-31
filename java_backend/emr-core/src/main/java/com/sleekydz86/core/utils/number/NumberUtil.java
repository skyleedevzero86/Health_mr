package com.sleekydz86.core.utils.number;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtil {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.KOREA);
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#,##0.00%");

    public static String formatNumber(Number number) {
        return number != null ? DECIMAL_FORMAT.format(number) : "0";
    }

    public static String formatCurrency(Number amount) {
        return amount != null ? CURRENCY_FORMAT.format(amount) : "₩0";
    }

    public static String formatPercent(double value) {
        return PERCENT_FORMAT.format(value);
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static double round(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }

    public static double floor(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.floor(value * scale) / scale;
    }

    public static double ceil(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.ceil(value * scale) / scale;
    }
}

