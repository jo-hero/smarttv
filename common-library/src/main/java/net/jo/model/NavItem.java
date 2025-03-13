package net.jo.model;

import java.io.Serializable;

public class NavItem implements Serializable {
    private Class<?> activity_class;
    private int bg = -1;
    private String description;
    private int icon = -1;
    private String name;
    private Object params;

    public NavItem(int icon, int bg, String name, String description, Class<?> activity_class, Object params) {
        this.icon = icon;
        this.bg = bg;
        this.name = name;
        this.description = description;
        this.activity_class = activity_class;
        this.params = params;
    }

    public int getIcon() {
        return this.icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getBg() {
        return this.bg;
    }

    public void setBg(int bg) {
        this.bg = bg;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Class<?> getActivity_class() {
        return this.activity_class;
    }

    public void setActivity_class(Class<?> activity_class) {
        this.activity_class = activity_class;
    }

    public Object getParams() {
        return this.params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public static NavItem newByTemplate(NavItem nav_template,String nav_name, String params){
        return new NavItem(nav_template.getIcon(),nav_template.getBg(),nav_name,nav_template.getDescription(),nav_template.getActivity_class(),params);
    }
}
