package net.jo.model;

import java.io.Serializable;

public class VideoInfo implements Serializable {
    private String description;
    private String image;
    private String name;
    private String tag;
    private String url;

    public VideoInfo() {
    }

    public VideoInfo(String str, String str2, String str3, String str4, String str5) {
        this.url = str;
        this.name = str2;
        this.description = str3;
        this.tag = str4;
        this.image = str5;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String str) {
        this.description = str;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String str) {
        this.tag = str;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String str) {
        this.image = str;
    }
}
