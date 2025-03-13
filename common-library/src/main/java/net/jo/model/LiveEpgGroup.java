package net.jo.model;

import com.google.gson.Gson;

import net.jo.common.Trans;
import net.jo.common.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class LiveEpgGroup {

    private String key;

    private String date;

    private ArrayList<LiveEpgItem> epg_data;

    private int width;

    public static LiveEpgGroup objectFrom(String data, String key, SimpleDateFormat format) {
        try {
            LiveEpgGroup item = new Gson().fromJson(data, LiveEpgGroup.class);
            item.setTime(format);
            item.setKey(key);
            return item;
        } catch (Exception e) {
            return new LiveEpgGroup();
        }
    }

    public static LiveEpgGroup create(String key, String date) {
        LiveEpgGroup item = new LiveEpgGroup();
        item.setKey(key);
        item.setDate(date);
        item.setList(new ArrayList<LiveEpgItem>());
        return item;
    }

    public String getKey() {
        return Utils.isEmpty(key) ? "" : key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDate() {
        return Utils.isEmpty(date) ? "" : date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<LiveEpgItem> getList() {
        return epg_data == null ? new ArrayList<LiveEpgItem>() : epg_data;
    }

    public void setList(ArrayList<LiveEpgItem> list) {
        this.epg_data = list;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean equal(String date) {
        return getDate().equals(date);
    }

    private void setTime(SimpleDateFormat format) {
        setList(new ArrayList<>(new LinkedHashSet<>(getList())));
        for (LiveEpgItem item : getList()) {
            item.setStartTime(Utils.format(format, getDate().concat(item.getStart())));
            item.setEndTime(Utils.format(format, getDate().concat(item.getEnd())));
            item.setTitle(Trans.s2t(item.getTitle()));
        }
    }

    public String getEpg() {
        for (LiveEpgItem item : getList()) if (item.isSelected()) return item.format();
        return "";
    }

    public LiveEpgGroup selected() {
        for (LiveEpgItem item : getList()) item.setSelected(item.isInRange());
        return this;
    }

    public int getSelected() {
        for (int i = 0; i < getList().size(); i++) if (getList().get(i).isSelected()) return i;
        return -1;
    }

    public int getInRange() {
        for (int i = 0; i < getList().size(); i++) if (getList().get(i).isInRange()) return i;
        return -1;
    }

}