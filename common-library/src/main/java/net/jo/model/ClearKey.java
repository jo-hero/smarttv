package net.jo.model;

import android.util.Base64;

import com.alibaba.fastjson.JSONObject;

import net.jo.common.HexUtils;
import net.jo.common.Utils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ClearKey {
    private List<Keys> keys;

    private String type;

    public static ClearKey objectFrom(String str) throws Exception {
        ClearKey item = JSONObject.toJavaObject(JSONObject.parseObject(str), ClearKey.class);
        if (item.keys == null) throw new Exception();
        return item;
    }

    public static ClearKey get(String line) {
        ClearKey item = new ClearKey();
        item.keys = new ArrayList<>();
        item.type = "temporary";
        item.addKeys(line);
        return item;
    }

    private void addKeys(String line) {
        int flags = Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP;
        for (String s : line.split(",")) {
            String[] a = s.split(":");
            String kid = Utils.base64(HexUtils.hex2byte(a[0].trim().getBytes(Charset.forName("UTF-8"))), flags).replace("=", "");
            String k = Utils.base64(HexUtils.hex2byte(a[1].trim().getBytes(Charset.forName("UTF-8"))), flags).replace("=", "");
            keys.add(new Keys(kid, k));
        }
    }

    public static class Keys {

        private String kty;

        private String k;

        private String kid;

        public Keys(String kid, String k) {
            this.kty = "oct";
            this.kid = kid;
            this.k = k;
        }
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}