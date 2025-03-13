package net.jo.common;

import com.orhanobut.hawk.Hawk;

public class HawkConfig {
    public static final String TRACELESS_MODE = "traceless_mode";

    //直播源地址 LiveSourceSites 里的url
    public static final String LIVE_HISTORY = "live_history";
    //频道历史
    public static final String LIVE_CHANNEL_HISTORY = "live_channel_history";

    public static final String PARSE_LIBS_URL = "parse_libs_url";
    public static final String HOME_WALLPAPER_URL = "home_wallpaper_url";

    public static final String AUTO_SWITCH_SOURCE_WHEN_PLAY_FAIL = "auto_switch_source_when_play_fail";
    public static final String AUTO_SKIP_HEAD_TAIL = "auto_skip_head_tail";
    public static final String OPEN_MIRROR = "open_mirror";

    public static final String PLAYER_FACTORY = "player_factory";
    public static final String CODING_TYPE = "coding_type";
    public static final String SUBTITLE_TEXT_SIZE = "subtitle_text_size";
    public static final String SUBTITLE_BOTTOM_PADDING = "subtitle_bottom_padding";

    public static final String ACROSS = "across";
    public static final String INVERT = "invert";
    public static final String CHANGE = "change";

    public static final String SCALE = "scale";
    public static final String SCALE_LIVE = "scale_live";

    public static final String AUTO_REMOVE_AD = "auto_remove_ad";


    public static final String PLAY_RENDER = "play_render"; //0 texture 2
    public static final String PLAY_TUNNEL = "play_tunnel"; //0 texture 2
    public static final String PLAY_RTSP = "play_rtsp";


    public static int getPlayerFactory() {
        return Hawk.get(HawkConfig.PLAYER_FACTORY, 1);
    }

    public static void putPlayerFactory(int player_factory) {
        Hawk.put(HawkConfig.PLAYER_FACTORY, player_factory);
    }

    public static boolean isUsingMediaCodec(int player) {
        return Hawk.get(HawkConfig.CODING_TYPE + player, true);
    }

    public static void setUsingMediaCodec(int player, boolean flag) {
        Hawk.put(HawkConfig.CODING_TYPE + player, flag);
    }

    public static float getSubtitleTextSize() {
        return Hawk.get(HawkConfig.SUBTITLE_TEXT_SIZE);
    }

    public static void putSubtitleTextSize(float value) {
        Hawk.put(HawkConfig.SUBTITLE_TEXT_SIZE, value);
    }

    public static float getSubtitleBottomPadding() {
        return Hawk.get(HawkConfig.SUBTITLE_BOTTOM_PADDING);
    }

    public static void putSubtitleBottomPadding(float value) {
        Hawk.put(HawkConfig.SUBTITLE_BOTTOM_PADDING, value);
    }

    public static boolean isInvert() {
        return Hawk.get(HawkConfig.INVERT, false);
    }

    public static void putInvert(boolean invert) {
        Hawk.put(HawkConfig.INVERT, invert);
    }

    public static boolean isAcross() {
        return Hawk.get(HawkConfig.ACROSS, true);
    }

    public static void putAcross(boolean across) {
        Hawk.put(HawkConfig.ACROSS, across);
    }

    public static boolean isChange() {
        return Hawk.get(HawkConfig.CHANGE, true);
    }

    public static void putChange(boolean change) {
        Hawk.put(HawkConfig.CHANGE, change);
    }

    public static int getScale() {
        return Hawk.get(HawkConfig.SCALE, 0);
    }

    public static void putScale(int scale) {
        Hawk.put(HawkConfig.SCALE, scale);
    }

    public static int getLiveScale() {
        return Hawk.get(HawkConfig.SCALE_LIVE, getScale());
    }

    public static void putLiveScale(int scale) {
        Hawk.put(HawkConfig.SCALE_LIVE, scale);
    }

    //this.getHome().getName() + LiveConfig.SYMBOL + channel.getGroup().getName() + LiveConfig.SYMBOL + channel.getName() + LiveConfig.SYMBOL + channel.getCurrent()
    public static void putLiveChannelHistory(String data){
        Hawk.put(HawkConfig.LIVE_CHANNEL_HISTORY, data);
    }

    public static String getLiveChannelHistory(){
        return Hawk.get(HawkConfig.LIVE_CHANNEL_HISTORY, "");
    }

    public static void putLiveHistory(String data){
        Hawk.put(HawkConfig.LIVE_HISTORY, data);
    }

    public static String getLiveHistory(){
        return Hawk.get(HawkConfig.LIVE_HISTORY, "");
    }

    public static void putParseLibsUrl(String data){
        Hawk.put(HawkConfig.PARSE_LIBS_URL, data);
    }

    public static String getParseLibsUrl(){
        return Hawk.get(HawkConfig.PARSE_LIBS_URL, "");
    }

    public static void putHomeWallpaperUrl(String data){
        Hawk.put(HawkConfig.HOME_WALLPAPER_URL, data);
    }

    public static String getHomeWallpaperUrl(){
        return Hawk.get(HawkConfig.HOME_WALLPAPER_URL, "");
    }

    public static int getPlayRender() {
        return Hawk.get(HawkConfig.PLAY_RENDER, 0);
    }

    public static void putPlayRender(int render) {
        Hawk.put(HawkConfig.PLAY_RENDER, render);
    }

    public static boolean isTunnel() {
        return Hawk.get(HawkConfig.PLAY_TUNNEL, false);
    }

    public static void putTunnel(boolean tunnel) {
        Hawk.put(HawkConfig.PLAY_TUNNEL, tunnel);
    }


    public static int getPlayRtsp() {
        return Hawk.get(HawkConfig.PLAY_RTSP, 0);
    }

    public static void putPlayRtsp(int rtsp) {
        Hawk.put(HawkConfig.PLAY_RTSP, rtsp);
    }

    public static boolean isTraceless() {
        return Hawk.get(HawkConfig.TRACELESS_MODE, false);
    }

    public static void putTraceLess(boolean traceless) {
        Hawk.put(HawkConfig.TRACELESS_MODE, traceless);
    }

    public static boolean isAutoRemoveAd() {
        return Hawk.get(HawkConfig.AUTO_REMOVE_AD, false);
    }

    public static void putAutoRemoveAd(boolean auto_remove_ad) {
        Hawk.put(HawkConfig.AUTO_REMOVE_AD, auto_remove_ad);
    }
}
