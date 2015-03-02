package ru.kapahgaiii.qa.core.tools;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class StringEncoder {

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
}
