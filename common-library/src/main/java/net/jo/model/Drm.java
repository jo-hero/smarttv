package net.jo.model;

import android.text.TextUtils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;

import java.util.UUID;

public class Drm {

    private String key;

    private String type;

    public static Drm create(String key, String type) {
        return new Drm(key, type);
    }

    private Drm(String key, String type) {
        this.key = key;
        this.type = type;
    }

    private String getKey() {
        return TextUtils.isEmpty(key) ? "" : key;
    }

    private String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    private UUID getUUID() {
        if (getType().contains("playready")) return C.PLAYREADY_UUID;
        if (getType().contains("widevine")) return C.WIDEVINE_UUID;
        if (getType().contains("clearkey")) return C.CLEARKEY_UUID;
        return C.UUID_NIL;
    }

    public MediaItem.DrmConfiguration get() {
        return new MediaItem.DrmConfiguration.Builder(getUUID()).setLicenseUri(getKey()).build();
    }
}