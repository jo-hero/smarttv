package net.jo.common;

import java.io.File;

public class EncodingDetect extends Encoding {
    public static String getEncode(String filePath) {
        return javaname[new BytesEncodingDetect().detectEncoding(new File(filePath))];
    }
}
