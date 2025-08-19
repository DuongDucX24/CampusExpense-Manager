package com.example.se07101campusexpenses.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class FormatUtils {
    private FormatUtils() {}

    /**
     * Attach a TextWatcher that formats the EditText as integer currency with dot group separators.
     * Examples: 1000 -> 1.000 ; 10000000 -> 10.000.000 ; empty -> "".
     * The formatting removes any trailing decimal part entirely.
     */
    public static void applyDotGroupingFormatter(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            boolean selfChange;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (selfChange) return;
                String raw = s.toString();
                // Strip all non-digits
                String digits = raw.replaceAll("[^0-9]", "");
                if (digits.isEmpty()) {
                    selfChange = true;
                    s.clear();
                    selfChange = false;
                    return;
                }
                // Avoid leading zeros explosion
                digits = digits.replaceFirst("^0+(?!$)", "");
                // Format with dot groupings
                String formatted = groupWithDots(digits);
                if (!formatted.equals(raw)) {
                    selfChange = true;
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                    selfChange = false;
                }
            }
        });
    }

    /**
     * Convert a formatted amount (e.g., "1.234.567") to a plain numeric string without separators.
     */
    public static String stripGrouping(String input) {
        if (input == null) return "";
        return input.replace(".", "").trim();
    }

    /** Format a double as a dot-grouped integer string without decimals (e.g., 1000.0 -> "1.000"). */
    public static String formatDoubleWithDots(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(0, RoundingMode.DOWN); // drop decimals
        return groupWithDots(bd.toPlainString());
    }

    /** Format a long as a dot-grouped string (e.g., 1000 -> "1.000"). */
    public static String formatLongWithDots(long value) {
        return groupWithDots(Long.toString(value));
    }

    private static String groupWithDots(String digits) {
        // Use BigInteger to avoid scientific notation
        BigInteger value = new BigInteger(digits);
        String s = value.toString();
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            sb.append(s.charAt(i));
            int posFromEnd = len - i - 1;
            if (posFromEnd > 0 && posFromEnd % 3 == 0) sb.append('.');
        }
        return sb.toString();
    }
}
