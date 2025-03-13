package net.jo.http;

import com.sonalb.net.http.Header;

public class HttpResult {
    private String result = "";
    private byte[] bytes_result = null;
    private String filepath = null;
    private String final_URL = "";
    private String md5Sign = "";
    private Header response_headers = new Header();
    private boolean sign = false;
    private String type = null;

    public HttpResult() {}

    public HttpResult(String result, boolean sign, String final_url) {
        this.result = result;
        this.sign = sign;
        this.final_URL = final_url;
    }

    public HttpResult(String result, boolean sign, String final_url, Header header) {
        this.result = result;
        this.sign = sign;
        this.final_URL = final_url;
        this.response_headers = header;
    }

    public String getResult() {
        return this.result == null ? "" : this.result;
    }

    public void setResult(String str) {
        this.result = str;
    }

    public byte[] getBytesResult() {
        if(this.bytes_result == null){
            try {
                return this.result.getBytes("UTF-8");
            } catch (Exception ex){
                return this.result.getBytes();
            }
        }
        return bytes_result;
    }

    public void setBytesResult(byte[] bytes_result) {
        this.bytes_result = bytes_result;
    }

    public boolean isSign() {
        return this.sign;
    }

    public void setSign(boolean z) {
        this.sign = z;
    }

    public String getFinal_URL() {
        return this.final_URL == null ? "" : this.final_URL;
    }

    public void setFinal_URL(String str) {
        this.final_URL = str;
    }

    public String getMd5Sign() {
        return this.md5Sign;
    }

    public void setMd5Sign(String str) {
        this.md5Sign = str;
    }

    public String getFilepath() {
        return this.filepath;
    }

    public void setFilepath(String str) {
        this.filepath = str;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String str) {
        this.type = str;
    }

    public Header getResponse_headers() {
        return this.response_headers;
    }

    public void setResponse_headers(Header header) {
        this.response_headers = header;
    }
    public boolean indexOf(boolean z, String... strArr) {
        if (z) {
            for (String str : strArr) {
                if (getResult().indexOf(str) < 0) {
                    return false;
                }
            }
            return true;
        }
        for (String str2 : strArr) {
            if (getResult().indexOf(str2) > -1) {
                return true;
            }
        }
        return false;
    }

    public boolean eqURL(String str) {
        return getFinal_URL().toLowerCase().indexOf(str.toLowerCase()) > -1;
    }

}
