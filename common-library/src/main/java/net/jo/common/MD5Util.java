package net.jo.common;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MD5Util {
    protected static char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getStringMD5(String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(str.getBytes("UTF-8"));
            return bufferToHex(instance.digest());
        } catch (Exception unused) {
            return null;
        }
    }

    public static String getFileMD5String(File file) throws IOException {
        try {
            return getMD5(new FileInputStream(file));
        } catch (Exception unused) {
            return null;
        }
    }

    public static String getMD5(InputStream in) {
        String name = "";
        try {
            byte[] bytes = new byte[1024];
            int len = 0;
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            while ((len = in.read(bytes)) > 0) {
                messagedigest.update(bytes, 0, len);
            }
            name = bufferToHex(messagedigest.digest());
        } catch (MalformedURLException localMalformedURLException) {
            try {
                in.close();
            } catch (Exception localException) {
            }
        } catch (IOException localIOException) {
            try {
                in.close();
            } catch (Exception localException1) {
            }
        } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
            try {
                in.close();
            } catch (Exception localException2) {
            }
        } finally {
            try {
                in.close();
            } catch (Exception localException3) {
            }
        }
        return name;
    }

    private static byte[] md5(byte[] bArr) throws NoSuchAlgorithmException {
        MessageDigest instance = MessageDigest.getInstance("MD5");
        instance.update(bArr);
        return instance.digest();
    }

    public static String bufferToHex(byte[] bArr) {
        return bufferToHex(bArr, 0, bArr.length);
    }

    private static String bufferToHex(byte[] bArr, int i, int i2) {
        StringBuffer stringBuffer = new StringBuffer(i2 * 2);
        int i3 = i2 + i;
        while (i < i3) {
            appendHexPair(bArr[i], stringBuffer);
            i++;
        }
        return stringBuffer.toString();
    }

    private static void appendHexPair(byte b, StringBuffer stringBuffer) {
        char c = hexDigits[(b & 240) >> 4];
        char c2 = hexDigits[b & 15];
        stringBuffer.append(c);
        stringBuffer.append(c2);
    }

    public static byte[] getHmacMd5(byte[] bArr, byte[] bArr2) throws NoSuchAlgorithmException {
        byte[] bArr3 = new byte[64];
        byte[] bArr4 = new byte[64];
        for (int i = 0; i < 64; i++) {
            bArr3[i] = 54;
            bArr4[i] = 92;
        }
        byte[] bArr5 = new byte[64];
        if (bArr.length > 64) {
            bArr = md5(bArr);
        }
        for (int i2 = 0; i2 < bArr.length; i2++) {
            bArr5[i2] = bArr[i2];
        }
        if (bArr.length < 64) {
            for (int length = bArr.length; length < bArr5.length; length++) {
                bArr5[length] = 0;
            }
        }
        byte[] bArr6 = new byte[64];
        for (int i3 = 0; i3 < 64; i3++) {
            bArr6[i3] = (byte) (bArr5[i3] ^ bArr3[i3]);
        }
        byte[] bArr7 = new byte[(bArr6.length + bArr2.length)];
        for (int i4 = 0; i4 < bArr6.length; i4++) {
            bArr7[i4] = bArr6[i4];
        }
        for (int i5 = 0; i5 < bArr2.length; i5++) {
            bArr7[bArr5.length + i5] = bArr2[i5];
        }
        byte[] md5 = md5(bArr7);
        byte[] bArr8 = new byte[64];
        for (int i6 = 0; i6 < 64; i6++) {
            bArr8[i6] = (byte) (bArr5[i6] ^ bArr4[i6]);
        }
        byte[] bArr9 = new byte[(bArr8.length + md5.length)];
        for (int i7 = 0; i7 < bArr8.length; i7++) {
            bArr9[i7] = bArr8[i7];
        }
        for (int i8 = 0; i8 < md5.length; i8++) {
            bArr9[bArr5.length + i8] = md5[i8];
        }
        return md5(bArr9);
    }

    public static String HmacSHA1(String str, String str2) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(str2.getBytes("UTF-8"), "HmacSHA1");
            Mac instance = Mac.getInstance("HmacSHA1");
            instance.init(secretKeySpec);
            return Base64.encodeToString(instance.doFinal(str.getBytes("UTF-8")), 0);
        } catch (Exception unused) {
            return null;
        }
    }
}
