package net.jo.common;

import android.app.Application;

import com.alibaba.fastjson.JSONObject;

import net.jo.http.HttpSimpleUtils;
import net.jo.model.VideoGroup;
import net.jo.model.VideoInfo;
import net.jo.bean.Callback;
import net.jo.iparse.IParseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Constants {
    public static SimpleDateFormat Format_Time = new SimpleDateFormat("yyyy-MM-ddHH:mm", Locale.getDefault());
    public static SimpleDateFormat Format_Date = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());

    public static JSONObject PARSE_LIBS_CONFIGS = null;

    public static Application APP = null;
    public static ArrayList<VideoGroup> home_datas;
    public static IParseHelper ParseHelper = null;
    public static HttpSimpleUtils HSU = new HttpSimpleUtils();
    public static Callback HotKeywordDBUtils = null;
    public static Callback ParseCallback = null;

    public static LinkedHashMap<String, String> Navs = new LinkedHashMap<>();
    public static String Version_Name = "";
    public static String Black_Video_Names = "";

    public static List<String> Nav_Filter = new ArrayList();
    public static Map<String, String> headers = new HashMap();
    public static List<String> Limit_Tags = new ArrayList();

    //快進時間單位
    public static final int INTERVAL_SEEK = 10 * 1000;
    //控件隱藏時間
    public static final int INTERVAL_HIDE = 5 * 1000;
    //網路偵測間隔
    public static final int INTERVAL_TRAFFIC = 1000;

    static {
        Nav_Filter.add("连续剧");
        Nav_Filter.add("电视剧");
        Nav_Filter.add("电影");
        Nav_Filter.add("动漫");
        Nav_Filter.add("综艺");

        Limit_Tags.add("伦理");
        Limit_Tags.add("限制级");
        Limit_Tags.add("伦理片");

        headers.put("Connection", "keep-alive");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Upgrade-Insecure-Requests", "1");
    }

    public static boolean isBlack(VideoInfo videoInfo){
        for(String black_video_name : Constants.Black_Video_Names.split("&")){
            if(videoInfo.getName().contains(black_video_name)){
                return true;
            }
        }
        return false;
    }
}
