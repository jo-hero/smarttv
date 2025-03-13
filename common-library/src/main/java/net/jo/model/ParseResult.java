package net.jo.model;

import java.util.Map;

public class ParseResult {
    private String play_url;
    private String referer;
    private Map<String,String> headers;

    public ParseResult(){}

    public ParseResult(String play_url, String referer, Map<String, String> headers) {
        this.play_url = play_url;
        this.referer = referer;
        this.headers = headers;
    }

    public String getPlay_url() {
        return play_url;
    }

    public void setPlay_url(String play_url) {
        this.play_url = play_url;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
