package net.jo.model;

import java.io.Serializable;

public class UpdateInfo implements Serializable {
    private String download_dir = "update";
    private boolean force;
    private String update_tips;
    private String update_url;
    private int version_code;

    public UpdateInfo() {
    }

    public UpdateInfo(int i, String str, boolean z, String str2) {
        this.version_code = i;
        this.update_url = str;
        this.force = z;
        this.update_tips = str2;
    }

    public int getVersion_code() {
        return this.version_code;
    }

    public void setVersion_code(int i) {
        this.version_code = i;
    }

    public String getUpdate_url() {
        return this.update_url;
    }

    public void setUpdate_url(String str) {
        this.update_url = str;
    }

    public boolean isForce() {
        return this.force;
    }

    public void setForce(boolean z) {
        this.force = z;
    }

    public String getUpdate_tips() {
        return this.update_tips;
    }

    public void setUpdate_tips(String str) {
        this.update_tips = str;
    }

    public String getDownload_dir() {
        return this.download_dir;
    }

    public void setDownload_dir(String str) {
        this.download_dir = str;
    }
}
