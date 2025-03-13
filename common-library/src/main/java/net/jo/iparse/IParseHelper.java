package net.jo.iparse;

import net.jo.http.HttpSimpleUtils;
import net.jo.model.ParseResult;
import net.jo.model.VideoGroup;
import net.jo.model.VideoRelateInfo;
import net.jo.bean.Callback;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public interface IParseHelper {
    String getServerUrl();

    HttpSimpleUtils getHsu();

    Map<String, String> getHeaders();

    /**
     * 解析主页视频数据
     * @param vgs
     * @return
     */
    LinkedHashMap<String, String> parseHomePage(ArrayList<VideoGroup> vgs);

    /**
     *
     * @param html_content 可以是内容也可以是http开头的链接或是自定义的链接如category://
     * @param nav_name
     * @param limit
     * @return
     */
    VideoGroup parse(String html_content, String nav_name, int limit);

    /**
     * 解析视频播放链接
     * @param episode_tag
     * @param video_name
     * @param resource_tag
     * @param resource_url
     * @param callback
     * @return
     * @throws Exception
     */
    ParseResult parse(String episode_tag, String video_name, String video_alias_name, String resource_tag, String resource_url, Callback callback) throws Exception;

    /**
     * 获取视频详情中的视频播放集数和资源
     * @param vod_url
     * @param vri
     * @return
     * @throws Exception
     */
    LinkedHashMap<String, String> findResourceList(String vod_url, VideoRelateInfo vri) throws Exception;

    /**
     *
     * @param current_url 当前获取视频的链接
     * @param tag_url 被选中的分类视频的链接
     * @param tag_key 被选中的哪一分类列表
     * @return
     */
    String composeTagUrl(String current_url, String tag_url, String tag_key);

    /**
     * 搜索视频
     * @param keyword
     * @return
     */
    VideoGroup doSearch(String keyword);

    /**
     * 通过给的页码和url组装页面页面url
     * @param page_url
     * @param page
     * @return
     */
    String getPageUrl(String page_url, int page);
}
