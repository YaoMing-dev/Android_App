// File: app/src/main/java/com/example/newtrade/utils/PriceFormatter.java
package com.example.newtrade.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PriceFormatter {

    private static final DecimalFormat VND_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        VND_FORMAT = new DecimalFormat("#,###", symbols);
    }

    public static String format(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return "Free";
        }

        return VND_FORMAT.format(price) + " ₫";
    }

    public static String format(double price) {
        return format(BigDecimal.valueOf(price));
    }

    public static String formatWithCurrency(BigDecimal price, String currency) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return "Free";
        }

        String formatted = VND_FORMAT.format(price);

        switch (currency.toUpperCase()) {
            case "USD":
                return "$" + formatted;
            case "VND":
            default:
                return formatted + " ₫";
        }
    }
}