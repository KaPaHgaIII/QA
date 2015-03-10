package ru.kapahgaiii.qa.core.tools;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Collection;

public class StringMaster {

    private static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (Exception ignored) {
        }
    }

    public static String sha256(String text) {
        try {
            md.update(text.getBytes("UTF-8"));

            byte[] digest = md.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(Integer.toHexString(0xFF & b));
            }
            return hexString.toString();
        } catch (UnsupportedEncodingException e) {
            return null;
        }

    }

    public static String join(Collection<?> objects, String delimiter) {
        return join(objects, delimiter, null, null);
    }

    public static String join(Collection<?> objects, String delimiter, String start, String end) {
        StringBuilder builder = new StringBuilder();

        if (start != null) {
            builder.append(start);
        }

        int i = 0;
        for (Object o : objects) {
            if (i != 0) {
                builder.append(delimiter);
            }
            builder.append(o.toString());
            i++;
        }

        if (end != null) {
            builder.append(end);
        }

        return builder.toString();
    }
}
