package net.jo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class VideoGroup implements Serializable {
    private String name;
    private int pagecount = 99999;
    private LinkedHashMap<String, LinkedHashMap<String, String>> all_tags;
    private ArrayList<VideoInfo> items;
    private ArrayList<NavItem> navs;
    private ArrayList<VideoInfo> slide_items;

    public VideoGroup() {
        this.name = "vg_tag";
        this.items = new ArrayList<>();
        this.navs = new ArrayList<>();
        this.slide_items = new ArrayList<>();
        this.pagecount = 99999;
    }

    public VideoGroup(ArrayList<VideoInfo> items, String name) {
        this.items = items;
        this.name = name;
    }

    public VideoGroup(ArrayList<VideoInfo> slide_items, ArrayList<VideoInfo> items, String name) {
        this.slide_items = slide_items;
        this.items = items;
        this.name = name;
    }

    public VideoGroup(ArrayList<VideoInfo> slide_items, ArrayList<VideoInfo> items, String name, LinkedHashMap<String, LinkedHashMap<String, String>> all_tags, int pagecount) {
        this.slide_items = slide_items;
        this.items = items;
        this.name = name;
        this.all_tags = all_tags;
        this.pagecount = pagecount;
    }

    public ArrayList<VideoInfo> getSlide_items() {
        return this.slide_items;
    }

    public void setSlide_items(ArrayList<VideoInfo> slide_items) {
        this.slide_items = slide_items;
    }

    public ArrayList<VideoInfo> getItems() {
        return this.items;
    }

    public void setItems(ArrayList<VideoInfo> items) {
        this.items = items;
    }

    public ArrayList<NavItem> getNavs() {
        return this.navs;
    }

    public void setNavs(ArrayList<NavItem> navs) {
        this.navs = navs;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getAll_tags() {
        return all_tags;
    }

    public void setAll_tags(LinkedHashMap<String, LinkedHashMap<String, String>> all_tags) {
        this.all_tags = all_tags;
    }

    public int getPagecount() {
        return pagecount;
    }

    public void setPagecount(int pagecount) {
        this.pagecount = pagecount;
    }
}
