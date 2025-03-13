package net.jo.common.base64;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class Base64 {
    public static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    public static String pad = "=";

    public static String encode(String str) {
        return encode(chars, str, "UTF-8");
    }

    public static String decode(String str) {
        return decode(chars, str, "UTF-8");
    }

    public static String encode(String str, String str2) {
        return encode(chars, str, str2);
    }

    public static String decode(String str, String str2) {
        return decode(chars, str, str2);
    }

    public static String encode(String str, String str2, String str3) {
        StringBuilder sb = new StringBuilder();
        String binary = binary(str2.getBytes(), 2);
        int i = 0;
        int i2 = 0;
        while (binary.length() % 24 != 0) {
            binary = binary + "0";
            i2++;
        }
        while (i <= binary.length() - 6) {
            int i3 = i + 6;
            int parseInt = Integer.parseInt(binary.substring(i, i3), 2);
            if (parseInt != 0 || i < binary.length() - i2) {
                sb.append(str.charAt(parseInt));
            } else {
                sb.append(pad);
            }
            i = i3;
        }
        return sb.toString();
    }

    public static String decode(String str, String str2, String str3) {
        String str4 = "";
        for (int i = 0; i < str2.length(); i++) {
            char charAt = str2.charAt(i);
            if (charAt != '=') {
                String binaryString = Integer.toBinaryString(str.indexOf(charAt));
                while (binaryString.length() != 6) {
                    binaryString = "0" + binaryString;
                }
                str4 = str4 + binaryString;
            }
        }
        String substring = str4.substring(0, str4.length() - (str4.length() % 8));
        byte[] bArr = new byte[(substring.length() / 8)];
        for (int i2 = 0; i2 < substring.length() / 8; i2++) {
            int i3 = i2 * 8;
            bArr[i2] = (byte) Integer.parseInt(substring.substring(i3, i3 + 8), 2);
        }
        try {
            return new String(bArr, str3);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String binary(byte[] bArr, int i) {
        String bigInteger = new BigInteger(1, bArr).toString(i);
        while (bigInteger.length() % 8 != 0) {
            bigInteger = "0" + bigInteger;
        }
        return bigInteger;
    }
}
