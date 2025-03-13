package net.jo.model;

import android.text.TextUtils;
import android.view.View;


import com.alibaba.fastjson.JSONObject;
import com.google.common.net.HttpHeaders;

import net.jo.common.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * channelIndex : 频道索引号
 * channelNum : 频道名称
 * channelSourceNames : 频道源名称
 * channelUrls : 频道源地址
 * sourceIndex : 频道源索引
 * sourceNum : 频道源总数
 */
public class LiveChannelItem {

    private List<String> urls;

    private ArrayList<String> sourceNames;

    private String tvgName;

    private String number;

    private String logo;

    private String epg;

    private String name;

    private String ua;

    private String click;

    private String format;

    private String origin;

    private String referer;

    private Catchup catchup;

    private JSONObject header;

    private Integer parse;

    private Drm drm;

    private Integer channelIndex;

    private int sourceIndex;

    private boolean selected;
    private LiveChannelGroup group;
    private String url;
    private String msg;
    //这个频道的EPG
    private LiveEpgGroup data;
    private int line;

    public static LiveChannelItem objectFrom(JSONObject element) {
        return JSONObject.parseObject(element.toJSONString(), LiveChannelItem.class);
    }

    public static LiveChannelItem create(int number) {
        return new LiveChannelItem().setNumber(number);
    }

    public static LiveChannelItem create(String name) {
        return new LiveChannelItem(name);
    }

    public static LiveChannelItem create(LiveChannelItem channel) {
        return new LiveChannelItem().copy(channel);
    }

    public static LiveChannelItem error(String msg) {
        LiveChannelItem result = new LiveChannelItem();
        result.setMsg(msg);
        return result;
    }

    public LiveChannelItem() {
    }

    public LiveChannelItem(String name) {
        this.name = name;
    }

    public String getTvgName() {
        return TextUtils.isEmpty(tvgName) ? getName() : tvgName;
    }

    public void setTvgName(String tvgName) {
        this.tvgName = tvgName;
    }

    public List<String> getUrls() {
        return urls = urls == null ? new ArrayList<String>() : urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public ArrayList<String> getSourceNames() {
        return sourceNames;
    }

    public void setSourceNames(ArrayList<String> sourceNames) {
        this.sourceNames = sourceNames;
    }

    public String getNumber() {
        return TextUtils.isEmpty(number) ? "" : number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLogo() {
        return TextUtils.isEmpty(logo) ? "" : logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getEpg() {
        return TextUtils.isEmpty(epg) ? "" : epg;
    }

    public void setEpg(String epg) {
        this.epg = epg;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUa() {
        return TextUtils.isEmpty(ua) ? "" : ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public String getClick() {
        return TextUtils.isEmpty(click) ? "" : click;
    }

    public void setClick(String click) {
        this.click = click;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getOrigin() {
        return TextUtils.isEmpty(origin) ? "" : origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getReferer() {
        return TextUtils.isEmpty(referer) ? "" : referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public Catchup getCatchup() {
        return catchup == null ? new Catchup() : catchup;
    }

    public void setCatchup(Catchup catchup) {
        this.catchup = catchup;
    }

    public JSONObject getHeader() {
        return header;
    }

    public void setHeader(JSONObject header) {
        this.header = header;
    }

    public Integer getParse() {
        return parse == null ? 0 : parse;
    }

    public void setParse(Integer parse) {
        this.parse = parse;
    }

    public Drm getDrm() {
        return drm;
    }

    public void setDrm(Drm drm) {
        this.drm = drm;
    }

    public LiveChannelGroup getGroup() {
        return group;
    }

    public void setGroup(LiveChannelGroup group) {
        this.group = group;
    }

    public String getUrl() {
        return TextUtils.isEmpty(url) ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMsg() {
        return TextUtils.isEmpty(msg) ? "" : msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean hasMsg() {
        return getMsg().length() > 0;
    }

    public LiveEpgGroup getData() {
        return data == null ? new LiveEpgGroup() : data;
    }

    public void setData(LiveEpgGroup data) {
        this.data = data;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = Math.max(line, 0);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setSelected(LiveChannelItem item) {
        this.selected = item.equals(this);
    }

    public int getLineVisible() {
        return isOnly() ? View.GONE : View.VISIBLE;
    }

/*    public void loadLogo(ImageView view) {
        ImageUtils.loadLive(getLogo(), view);
    }*/

    public void addUrls(String... urls) {
        getUrls().addAll(new ArrayList<>(Arrays.asList(urls)));
    }

    public void nextLine() {
        setLine(getLine() < getUrls().size() - 1 ? getLine() + 1 : 0);
    }

    public void prevLine() {
        setLine(getLine() > 0 ? getLine() - 1 : getUrls().size() - 1);
    }

    public String getCurrent() {
        return Utils.isEmpty(getUrls()) ? "" : getUrls().get(getLine());
    }

    public boolean isOnly() {
        return getUrls().size() == 1;
    }

    public boolean isLast() {
        return Utils.isEmpty(getUrls()) || getLine() == getUrls().size() - 1;
    }

    /**
     * 查看是否支持回看
     * @return
     */
    public boolean hasCatchup() {
        if (Utils.isEmpty(getCatchup()) && getCurrent().contains("/PLTV/")) setCatchup(Catchup.PLTV());
        if (!Utils.isEmpty(getCatchup().getRegex())) return getCatchup().match(getCurrent());
        return !Utils.isEmpty(getCatchup());
    }

    public String getLineText() {
        if (getUrls().size() <= 1) return "";
        if (getCurrent().contains("$")) return getCurrent().split("\\$")[1];
        return "Line ," + getLine() + 1;
    }

    public LiveChannelItem setNumber(int number) {
        setNumber(String.format(Locale.getDefault(), "%03d", number));
        return this;
    }

    public Integer getChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(Integer channelIndex) {
        this.channelIndex = channelIndex;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public void setSourceIndex(int sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public LiveChannelItem group(LiveChannelGroup group) {
        setGroup(group);
        return this;
    }

    public void live(LiveSourceSite live) {
        this.setEpg(live.getChannel_epg());
        if (!Utils.isEmpty(live.getUa()) && Utils.isEmpty(getUa())) setUa(live.getUa());
        if (live.getHeader() != null && getHeader() == null) setHeader(live.getHeader());
        if (!Utils.isEmpty(live.getOrigin()) && Utils.isEmpty(getOrigin())) setOrigin(live.getOrigin());
        if (!Utils.isEmpty(live.getCatchup()) && Utils.isEmpty(getCatchup())) setCatchup(live.getCatchup());
        if (!Utils.isEmpty(live.getReferer()) && Utils.isEmpty(getReferer())) setReferer(live.getReferer());
        if (!Utils.isEmpty(live.getLogo()) && live.getLogo().contains("{") && !getLogo().startsWith("http")) setLogo(live.getLogo().replace("{name}", getTvgName()).replace("{logo}", getLogo()));
    }

    public void setLine(String line) {
        setLine(getUrls().indexOf(line));
    }

    public Map<String, String> getMapHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        if(!Utils.isEmpty(this.getHeader())){
            for(String key : this.getHeader().keySet()){
                headers.put(key, this.getHeader().getString(key));
            }
        }
        if (!Utils.isEmpty(getUa())) headers.put(HttpHeaders.USER_AGENT, getUa());
        if (!Utils.isEmpty(getOrigin())) headers.put(HttpHeaders.ORIGIN, getOrigin());
        if (!Utils.isEmpty(getReferer())) headers.put(HttpHeaders.REFERER, getReferer());
        return headers;
    }

    public LiveChannelItem copy(LiveChannelItem item) {
        setCatchup(item.getCatchup());
        setReferer(item.getReferer());
        setTvgName(item.getTvgName());
        setHeader(item.getHeader());
        setNumber(item.getNumber());
        setOrigin(item.getOrigin());
        setFormat(item.getFormat());
        setParse(item.getParse());
        setClick(item.getClick());
        setLogo(item.getLogo());
        setName(item.getName());
        setUrls(item.getUrls());
        setData(item.getData());
        setDrm(item.getDrm());
        setEpg(item.getEpg());
        setUa(item.getUa());
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LiveChannelItem)) return false;
        LiveChannelItem it = (LiveChannelItem) obj;
        return getName().equals(it.getName()) || (!Utils.isEmpty(getNumber()) && !Utils.isEmpty(it.getNumber()) && getNumber().equals(it.getNumber()));
    }

    public boolean isEmpty(){
        return this.name == null || this.name.isEmpty();
    }
}