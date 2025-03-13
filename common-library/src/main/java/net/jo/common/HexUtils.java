package net.jo.common;

public class HexUtils {
    /**
     * 将二进制比特数据转成十六进制的String类型
     * @param b	二进制的比特数组
     * @return	String型的十六进制字符串
     */
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1) {
                hs += ("0" + stmp);
            } else {
                hs += stmp;
            }
        }
        return hs.toUpperCase();
    }

    /**
     * 将十六进制比特数据数组转成二进制的比特数据数组
     * @param b	十六进制数据数组
     * @return	二进制数据数组
     */
    public static byte[] hex2byte(byte[] b){
        if ((b.length % 2) != 0)throw new IllegalArgumentException("长度不是偶数");

        byte[] b2 = new byte[b.length / 2];

        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

}
