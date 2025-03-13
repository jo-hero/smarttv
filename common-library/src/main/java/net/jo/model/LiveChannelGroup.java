package net.jo.model;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

import net.jo.common.Utils;
import net.jo.common_library.R;

import java.util.ArrayList;
import java.util.List;

public class LiveChannelGroup {

    private ArrayList<LiveChannelItem> channel;

    private String name;

    private String pass;

    private Integer groupIndex;

    private boolean selected;

    //组内当前选中的第几个频道
    private int position;
    private int width;

    public static List<LiveChannelGroup> arrayFrom(String str) {
        List<LiveChannelGroup> items = JSONObject.parseArray(str, LiveChannelGroup.class);
        return items == null ? new ArrayList<LiveChannelGroup>() : (ArrayList)items;
    }

    public static LiveChannelGroup create() {
        return create("Live", false);
    }

    public static LiveChannelGroup create(String name, boolean pass) {
        return new LiveChannelGroup(name, pass);
    }

    public LiveChannelGroup(String name) {
        this(name, false);
    }

    public LiveChannelGroup(String name, boolean pass) {
        this.name = name;
        this.position = -1;
        if (name.contains("_")) parse(pass);
        if (name.isEmpty()) setName("Live");
    }

    private void parse(boolean pass) {
        String[] splits = name.split("_");
        setName(splits[0]);
        if (pass || splits.length == 1) return;
        setPass(splits[1]);
    }

    public ArrayList<LiveChannelItem> getChannel() {
        return channel = channel == null ? new ArrayList<LiveChannelItem>() : channel;
    }

    public void setChannel(ArrayList<LiveChannelItem> channel) {
        this.channel = channel;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return TextUtils.isEmpty(pass) ? "" : pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public Integer getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(Integer groupIndex) {
        this.groupIndex = groupIndex;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isHidden() {
        return !TextUtils.isEmpty(getPass());
    }

    public boolean isEmpty() {
        return getChannel().isEmpty();
    }

    public int find(int number) {
        return getChannel().lastIndexOf(LiveChannelItem.create(number));
    }

    public int find(String name) {
        for(int i=0;i<channel.size();i++){
            if(channel.get(i).getName().equals(name)){
                return i;
            }
        }
        return -1;
    }

    public void add(LiveChannelItem channel) {
        int index = getChannel().indexOf(channel);
        if (index == -1) getChannel().add(LiveChannelItem.create(channel));
        else getChannel().get(index).getUrls().addAll(channel.getUrls());
    }

    public LiveChannelItem find(LiveChannelItem channel) {
        int index = getChannel().indexOf(channel);
        if (index != -1) return getChannel().get(index);
        getChannel().add(channel);
        return channel;
    }

    public LiveChannelItem current() {
        return getChannel().get(getPosition()).group(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof LiveChannelGroup)) return false;
        LiveChannelGroup it = (LiveChannelGroup) obj;
        return getName().equals(it.getName()) && getChannel().size() == it.getChannel().size();
    }
}