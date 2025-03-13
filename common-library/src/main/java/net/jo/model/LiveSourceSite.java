package net.jo.model;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class LiveSourceSite {
    private String name;
    private int  type = 0;
    private String url;
    //playerType	none	播放器	0：系統；1：IJK；2：EXO
    private int playerType = 1;
    //"http://epg.51zmt.top:8000/api/diyp/?ch={name}&date={date}"
    private String epg;

    //单个频道的epg链接
    private String channel_epg;

    //"https://epg.v1.mk/logo/{name}.png"
    private String logo;

    //回看參數
    private Catchup catchup;

    private String ua;

    private String origin;

    private String referer;

    private int timeout;

    private ArrayList<LiveChannelGroup> liveChannelGroups;

    private boolean pass;

    private boolean activated;

    private int width;

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public JSONObject getHeader() {
        return header;
    }

    public void setHeader(JSONObject header) {
        this.header = header;
    }

    private JSONObject header;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPlayerType() {
        return playerType;
    }

    public void setPlayerType(int playerType) {
        this.playerType = playerType;
    }

    public String getEpg() {
        return epg;
    }

    public void setEpg(String epg) {
        this.epg = epg;
    }

    public String getChannel_epg() {
        return channel_epg;
    }

    public void setChannel_epg(String channel_epg) {
        this.channel_epg = channel_epg;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Catchup getCatchup() {
        return catchup;
    }

    public void setCatchup(Catchup catchup) {
        this.catchup = catchup;
    }

    public ArrayList<LiveChannelGroup> getLiveChannelGroups() {
        return liveChannelGroups = liveChannelGroups == null ? new ArrayList<LiveChannelGroup>() : liveChannelGroups;
    }

    public void setLiveChannelGroups(ArrayList<LiveChannelGroup> liveChannelGroups) {
        this.liveChannelGroups = liveChannelGroups;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setActivated(LiveSourceSite item) {
        this.activated = item.equals(this);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public LiveChannelGroup find(LiveChannelGroup item) {
        for (LiveChannelGroup group : getLiveChannelGroups()) {
            if (group.getName().equals(item.getName())) {
                return group;
            }
        }
        getLiveChannelGroups().add(item);
        return item;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof LiveSourceSite)) return false;
        LiveSourceSite it = (LiveSourceSite) obj;
        return getName().equals(it.getName()) && getUrl().equals(it.getUrl());
    }
}
