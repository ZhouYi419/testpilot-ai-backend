package com.zy.testpilotai.common.utils;

import java.security.MessageDigest;

public class HashUtils {

    public static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String value = Integer.toHexString(0xff & b);
                if (value.length() == 1) {
                    hex.append('0');
                }
                hex.append(value);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算文件 Hash 失败", e);
        }
    }
}