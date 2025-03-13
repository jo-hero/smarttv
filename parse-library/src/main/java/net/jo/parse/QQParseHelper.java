package net.jo.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import net.jo.common.Constants;
import net.jo.common.StopException;
import net.jo.http.HttpResult;
import net.jo.http.HttpSimpleUtils;
import net.jo.model.NavItem;
import net.jo.model.ParseResult;
import net.jo.model.VideoGroup;
import net.jo.model.VideoInfo;
import net.jo.model.VideoRelateInfo;

import net.jo.bean.Callback;
import net.jo.common.Utils;
import net.jo.iparse.IParseHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QQParseHelper implements IParseHelper {
    private JSONObject config;
    private HttpSimpleUtils hsu;
    private NavItem nav_template;
    private Map<String, String> headers = new HashMap();
    private List<String> all_serverUrls = new ArrayList<String>();
    private String serverUrl = null;

    public String getServerUrl(){
        return this.serverUrl;
    }

    public HttpSimpleUtils getHsu() {
        return hsu;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public QQParseHelper(JSONObject config, NavItem nav_template){
        this.config = config;
        this.all_serverUrls = Utils.parseUrls(config.getJSONArray("urls"));
        this.serverUrl = this.all_serverUrls.get(0);
        this.nav_template = nav_template;
        this.hsu = new HttpSimpleUtils();
        this.hsu.setCookie("video_platform","2","pbaccess.video.qq.com");
        this.hsu.setCookie("video_guid","62a4a2d643f4d53d","pbaccess.video.qq.com");
        this.hsu.setCookie("vversion_name","8.2.95","pbaccess.video.qq.com");
        this.headers.put("Connection", "keep-alive");
        this.headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        this.headers.put("Accept-Encoding", "gzip, deflate");
        this.headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        this.headers.put("Upgrade-Insecure-Requests", "1");
    }

    @Override
    public LinkedHashMap<String, String> parseHomePage(ArrayList<VideoGroup> vgs) {
        vgs.add(this.parse("channel_id://100101", "推荐", 20));

        LinkedHashMap<String, String> tabs = new LinkedHashMap<>();
        tabs.put("电影",   "channel_id://100173?sort=75");
        tabs.put("电视剧", "channel_id://100113?sort=75");
        tabs.put("少儿",   "channel_id://100150?sort=75");
        tabs.put("动漫",   "channel_id://100119?sort=75");
        tabs.put("综艺",   "channel_id://100109?sort=75");
        tabs.put("记录片", "channel_id://100105?sort=75");
        return tabs;
    }

    @Override
    public VideoGroup parse(String html_content, String nav_name, int limit) {
        //如果html_content是https://开头说明只是一个链接，需要动态获取
        if(html_content.toLowerCase().startsWith("channel_id://")){
            String channel_id = html_content.replace("channel_id://", "").split("\\?")[0];
            JSONObject params = JSONObject.parseObject("{\"page_context\":{},\"page_params\":{},\"page_bypass_params\":{\"params\":{\"data_mode\":\"default\",\"user_mode\":\"default\"},\"abtest_bypass_id\":\"62a4a2d643f4d53d\"}}");
            if(channel_id.equals("100101")){
                //page_params
                params.getJSONObject("page_params").put("page_id", channel_id);
                params.getJSONObject("page_params").put("page_type", "channel");

                //page_bypass_params->params
                params.getJSONObject("page_bypass_params").getJSONObject("params").put("page_id", channel_id);
                params.getJSONObject("page_bypass_params").getJSONObject("params").put("page_type", "channel");

                //page_bypass_params
                params.getJSONObject("page_bypass_params").put("scene", "channel");
            } else {
                String page = "0";
                StringBuffer filter_params = new StringBuffer();
                try {
                    String exts = html_content.replace("channel_id://", "").split("\\?")[1];
                    for(String ext : exts.split("&")){
                        if(Utils.isEmpty(ext)){
                            continue;
                        }
                        if(ext.split("=")[0].equals("page")){
                            page = ext.split("=")[1];
                            continue;
                        }
                        if(filter_params.length() > 0){
                            filter_params.append("&");
                        }
                        filter_params.append(ext.split("=")[0]);
                        filter_params.append("=");
                        filter_params.append(ext.split("=")[1]);
                    }
                } catch (Exception ex){
                    filter_params.append("sort=75");
                }

                params.getJSONObject("page_context").put("page_index", page);
                //page_params
                params.getJSONObject("page_params").put("page_id", "channel_list_second_page");
                params.getJSONObject("page_params").put("page", page);
                params.getJSONObject("page_params").put("channel_id", channel_id);
                params.getJSONObject("page_params").put("filter_params", filter_params.toString());
                params.getJSONObject("page_params").put("page_type", "operation");

                //page_bypass_params->params
                params.getJSONObject("page_bypass_params").getJSONObject("params").put("page_id", "channel_list_second_page");
                params.getJSONObject("page_bypass_params").getJSONObject("params").put("page", page);
                params.getJSONObject("page_bypass_params").getJSONObject("params").put("channel_id", channel_id);
                params.getJSONObject("page_bypass_params").getJSONObject("params").put("filter_params", filter_params.toString());
                params.getJSONObject("page_bypass_params").getJSONObject("params").put("page_type", "operation");
                //page_bypass_params
                params.getJSONObject("page_bypass_params").put("scene", "operation");
            }
            params.getJSONObject("page_bypass_params").getJSONObject("params").put("caller_id", "3000010");
            params.getJSONObject("page_bypass_params").getJSONObject("params").put("platform_id", "2");

            ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
            Map<String,String> headers = new HashMap<String,String>();
            headers.put("sec-ch-ua","\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"");
            headers.put("accept","*/*");
            headers.put("sec-ch-ua-mobile","?0");
            headers.put("content-type","application/json");
            headers.put("origin","https://v.qq.com");
            headers.put("sec-fetch-site","same-site");
            headers.put("sec-fetch-mode","cors");
            headers.put("sec-fetch-dest","empty");
            headers.put("accept-encoding","gzip, deflate, br");
            headers.put("accept-language","zh-CN,zh;q=0.9");

            HttpResult result = null;
            while (result == null || !result.isSign()) {
                result = this.hsu.doPostBody_Json("https://pbaccess.video.qq.com/trpc.vector_layout.page_view.PageService/getPage?video_appid=3000010", params.toString(), this.getServerUrl(), headers, "UTF-8");
                if(result == null || !result.isSign()){
                    try {
                        Thread.sleep(2000);
                    } catch(Exception ex){}
                }
            }
            html_content = result.getResult().trim();
        }

        VideoGroup vg = new VideoGroup();
        vg.setName(nav_name);
        if(!Utils.isEmpty(Utils.getString(html_content, "(window\\.__PINIA[_].)"))){
            ArrayList<VideoInfo> videoInfos = new ArrayList<>();
            String __pinia = Utils.getString(html_content, "\\<script\\>\\s*window\\.__PINIA[_].\\s*=\\s*(.*?)\\<\\/script\\>\\s*\\<\\/head\\>");
            __pinia = __pinia.replace(":undefined",":null");
            if(__pinia.indexOf("Array.prototype.slice.call")>=0){
                __pinia = __pinia.replaceAll("Array\\.prototype\\.slice\\.call\\((.*?)\\),", "$1,");
                __pinia = __pinia.replaceAll("Array\\.prototype\\.slice\\.call\\((.*?)\\),", "$1,");
            }

            JSONObject pinia = JSONObject.parseObject(__pinia);
            JSONArray topList = pinia.getJSONObject("topList").getJSONArray("data");
            for (int i = 0; i < topList.size(); i++) {
                VideoInfo videoInfo = new VideoInfo();
                JSONObject item = topList.getJSONObject(i);
                videoInfo.setImage(item.getString("pic"));
                videoInfo.setTag(item.getString("timelong"));
                videoInfo.setUrl("https://v.qq.com/x/cover/"+item.getString("id")+".html");
                videoInfo.setName(item.getString("title"));
                videoInfo.setDescription(item.getString("secondTitle"));
                if (limit < 0 || videoInfos.size() < limit) {
                    if(!Constants.isBlack(videoInfo)){
                        videoInfos.add(videoInfo);
                    }
                }
                Constants.HotKeywordDBUtils.call(videoInfo.getName());
            }
            vg.setItems(videoInfos);
        } else {
            JSONObject initial_state = JSONObject.parseObject(html_content.replace("@type", "type"));
            if(initial_state.getInteger("ret") != 0){
                return vg;
            }

            JSONArray CardList = initial_state.getJSONObject("data").getJSONArray("CardList");
            for (int i = 0; i < CardList.size(); i++) {
                JSONObject Card = CardList.getJSONObject(i);
                if(Card.getString("type").equals("channel_list_filter")){
                    String channel_id = Card.getJSONObject("params").getString("channel_id");
                    JSONArray children_list = Card.getJSONObject("children_list").getJSONObject("list").getJSONArray("cards");
                    LinkedHashMap<String, LinkedHashMap<String, String>> all_tags = new LinkedHashMap<>();
                    for(int j=0; j<children_list.size(); j++){
                        JSONObject children_Card = children_list.getJSONObject(j);
                        if(children_Card.getJSONObject("params").containsKey("filter_key")){
                            String filter_key = children_Card.getJSONObject("params").getString("filter_key");
                            String index_name = children_Card.getJSONObject("params").getString("index_name");
                            String option_name = children_Card.getJSONObject("params").getString("option_name");
                            String option_value = children_Card.getJSONObject("params").getString("option_value");
                            String is_selected = "0";
                            if(children_Card.getJSONObject("params").containsKey("is_selected")){
                                is_selected = children_Card.getJSONObject("params").getString("is_selected");
                            }
                            if(!all_tags.containsKey(index_name)){
                                all_tags.put(index_name, new LinkedHashMap<String, String>());
                            }
                            String tag_name = "全部" + index_name;
                            all_tags.get(index_name).put(option_value.equals("-1") ? tag_name : option_name, "channel_id://"+channel_id+"?"+filter_key + "=" + option_value + (is_selected.equals("1")?"?active":""));
                        }
                    }
                    vg.setAll_tags(all_tags);
                } else if(Card.getString("type").equals("channel_list_poster") || Card.getString("type").equals("pc_carousel") || Card.getString("type").equals("pc_shelves")){
                    ArrayList<VideoInfo> videoInfos = new ArrayList<VideoInfo>();
                    if(Card.getJSONObject("params").containsKey("total_video")){
                        int total_page = Card.getJSONObject("params").getInteger("total_video")/Card.getJSONObject("params").getInteger("page_size");
                        vg.setPagecount(total_page);
                    }

                    JSONArray children_list = Card.getJSONObject("children_list").getJSONObject("list").getJSONArray("cards");
                    for(int j=0;j<children_list.size();j++){
                        JSONObject children_Card = children_list.getJSONObject(j);
                        if(children_Card.getString("type").contains("card_ad") || !children_Card.getJSONObject("params").containsKey("cid")){
                            continue;
                        }
                        if(children_Card.getJSONObject("params").getString("cid").startsWith("http")){
                            continue;
                        }
                        if(children_Card.getJSONObject("params").containsKey("stream_id")){
                            continue;
                        }
                        if(children_Card.getJSONObject("params").containsKey("ctype")){
                            if(children_Card.getJSONObject("params").getString("ctype").equals("short_video")){
                                continue;
                            }
                            if(children_Card.getJSONObject("params").getString("ctype").equals("program")){
                                continue;
                            }
                        }

                        VideoInfo videoInfo = new VideoInfo();
                        if(children_Card.getJSONObject("params").containsKey("new_pic_vt")){
                            videoInfo.setImage(children_Card.getJSONObject("params").getString("new_pic_vt"));
                        } else if(children_Card.getJSONObject("params").containsKey("image_url")){
                            videoInfo.setImage(children_Card.getJSONObject("params").getString("image_url"));
                        } else if(children_Card.getJSONObject("params").containsKey("ready_image_url")){
                            videoInfo.setImage(children_Card.getJSONObject("params").getString("ready_image_url"));
                        } else if(children_Card.getJSONObject("params").containsKey("image_url_vertical")){
                            videoInfo.setImage(children_Card.getJSONObject("params").getString("image_url_vertical"));
                        } else {
                            continue;
                        }
                        //videoInfo.setTag(children_Card.getJSONObject("params").getString("topic_label"));
                        videoInfo.setTag("");
                        videoInfo.setUrl("https://v.qq.com/x/cover/"+children_Card.getJSONObject("params").getString("cid")+".html");
                        videoInfo.setName(children_Card.getJSONObject("params").getString("title"));
                        if(children_Card.getJSONObject("params").containsKey("mz_title")){
                            videoInfo.setName(children_Card.getJSONObject("params").getString("mz_title"));
                        }
                        if(children_Card.getJSONObject("params").containsKey("stitle_pc")){
                            videoInfo.setDescription(children_Card.getJSONObject("params").getString("stitle_pc"));
                        }
                        if(children_Card.getJSONObject("params").containsKey("rec_subtitle")){
                            videoInfo.setDescription(children_Card.getJSONObject("params").getString("rec_subtitle"));
                        }
                        if(children_Card.getJSONObject("params").containsKey("rec_normal_reason")){
                            videoInfo.setDescription(children_Card.getJSONObject("params").getString("rec_normal_reason"));
                        }
                        if(children_Card.getJSONObject("params").containsKey("second_title")){
                            videoInfo.setDescription(children_Card.getJSONObject("params").getString("second_title"));
                        }
                        if (limit < 0 || videoInfos.size() < limit) {
                            if(!Constants.isBlack(videoInfo)){
                                videoInfos.add(videoInfo);
                            }
                        }
                        Constants.HotKeywordDBUtils.call(videoInfo.getName());
                    }
                    if(limit > 0){
                        Collections.shuffle(videoInfos);
                    }
                    if(Card.getString("type").equals("pc_carousel")){
                        vg.setSlide_items(videoInfos);
                    } else {
                        vg.setItems(videoInfos);
                    }
                }
            }

            if(limit > -1 && vg.getAll_tags() != null){
                String tag = ((String[]) vg.getAll_tags().keySet().toArray(new String[0]))[0];
                if (vg.getAll_tags().containsKey("剧情")) {
                    tag = "剧情";
                } else if (vg.getAll_tags().containsKey("类型")) {
                    tag = "类型";
                } else if (vg.getAll_tags().containsKey("年份")) {
                    tag = "年份";
                }
                int i = 0;
                ArrayList<NavItem> navItems = new ArrayList<>();
                for (String catalog : vg.getAll_tags().get(tag).keySet()) {
                    navItems.add(NavItem.newByTemplate(nav_template, catalog,  vg.getAll_tags().get(tag).get(catalog)));
                    i++;
                    if (i >= 8) {
                        break;
                    }
                }
                vg.setNavs(navItems);
            }
        }
        return vg;
    }

    @Override
    public ParseResult parse(String episode_tag, String video_name, String video_alias_name, String resource_tag, String resource_url, Callback callback) throws Exception {
        callback.call(20, resource_url);
        HttpResult result = null;
        while (result == null || !result.isSign()) {
            result = this.getHsu().doGetBody(resource_url, resource_url, this.getHeaders(), "UTF-8");
            if(result.indexOf(true,"title>检测中","location.href")){
                result = this.getHsu().doGetBody(this.getLocation(result), resource_url, this.getHeaders(), "UTF-8");
            }
        }
        if (result.indexOf(false, "由于版权原因，影片已屏蔽")) {
            throw new StopException("由于版权原因，影片已屏蔽");
        }

        String cid = Utils.getString(result.getResult(), "cid\":\"(.*?)\",");
        VideoGroup vg = this.getRecommend(cid, 10);
        Constants.ParseCallback.call(vg);

        callback.call(30, "/jx/?url="+resource_url);
        ParseResult pr = VIPParseHelper.parse(episode_tag, video_name, video_alias_name, resource_tag, resource_url, callback);
        //清空广告
        AdsUtils.removeAds(config, pr);
        return pr;
    }

    private VideoGroup getRecommend(String cid, int limit){
        JSONObject params = JSONObject.parseObject("{\"page_params\":{\"req_from\":\"web\",\"page_type\":\"detail_operation\",\"cid\":\""+cid+"\",\"page_id\":\"detail_page_recommend_tab\",\"lid\":\"\"},\"has_cache\":1}");
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("sec-ch-ua","\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"");
        headers.put("accept","*/*");
        headers.put("sec-ch-ua-mobile","?0");
        headers.put("content-type","application/json");
        headers.put("origin","https://v.qq.com");
        headers.put("sec-fetch-site","same-site");
        headers.put("sec-fetch-mode","cors");
        headers.put("sec-fetch-dest","empty");
        headers.put("accept-encoding","gzip, deflate, br");
        headers.put("accept-language","zh-CN,zh;q=0.9");

        HttpResult result = null;
        while (result == null || !result.isSign()) {
            result = this.hsu.doPostBody_Json("https://pbaccess.video.qq.com/trpc.universal_backend_service.page_server_rpc.PageServer/GetPageData?video_appid=3000010&vplatform=2", params.toString(), this.getServerUrl(), headers, "UTF-8");
            if(result == null || !result.isSign()){
                try {
                    Thread.sleep(2000);
                } catch(Exception ex){}
            }
        }
        ArrayList<VideoInfo> videoInfos = new ArrayList<VideoInfo>();
        JSONObject data = JSONObject.parseObject(result.getResult()).getJSONObject("data");
        JSONArray module_list_datas = data.getJSONArray("module_list_datas");
        for(int i=0;i<module_list_datas.size();i++) {
            JSONObject module_list_data = module_list_datas.getJSONObject(i);
            JSONArray module_datas = module_list_data.getJSONArray("module_datas");
            for (int j = 0; j < module_datas.size(); j++) {
                JSONArray item_datas = module_datas.getJSONObject(j).getJSONObject("item_data_lists").getJSONArray("item_datas");
                for(int l=0;l<item_datas.size();l++) {
                    JSONObject item_data = item_datas.getJSONObject(l);
                    if(!Utils.isEmpty(item_data.getString("item_id"))){

                        JSONObject item_params = item_data.getJSONObject("item_params");

                        VideoInfo videoInfo = new VideoInfo();
                        if(item_params.containsKey("image_url")){
                            videoInfo.setImage(item_params.getString("image_url"));
                        }
                        videoInfo.setTag(item_params.getString("tag_right_text"));
                        videoInfo.setUrl("https://v.qq.com/x/cover/"+item_data.getString("item_id")+".html");
                        videoInfo.setName(item_params.getString("title"));
                        videoInfo.setDescription(item_params.getString("sub_title"));
                        if (limit < 0 || videoInfos.size() < limit) {
                            if(!Constants.isBlack(videoInfo)){
                                videoInfos.add(videoInfo);
                            }
                        }
                        Constants.HotKeywordDBUtils.call(videoInfo.getName());
                    }
                }
            }
        }

        VideoGroup vg = new VideoGroup();
        vg.setName("guess_list");
        vg.setItems(videoInfos);
        return vg;
    }

    @Override
    public String composeTagUrl(String url, String tag_url, String tag_key) {
        Map<String,String> params1 = Utils.TranslateString2Map(url.split("\\?")[1]);
        Map<String,String> params2 = Utils.TranslateString2Map(tag_url.split("\\?")[1]);

        if (tag_key.contains("排序") && params2.containsKey("sort")) {//sort
            params1.put("sort",params2.get("sort"));
        } else if (tag_key.contains("地区") && params2.containsKey("iarea")) {//iarea
            params1.put("iarea",params2.get("iarea"));
        } else if (tag_key.contains("年龄") && params2.containsKey("iyear")) {//iyear
            params1.put("iyear",params2.get("iyear"));
        } else if (tag_key.contains("性别") && params2.containsKey("gender")) {//gender
            params1.put("gender",params2.get("gender"));
        } else if (tag_key.contains("类型") && params2.containsKey("itype")) {//itype
            params1.put("itype",params2.get("itype"));
        } else if (tag_key.contains("类型") && params2.containsKey("feature")) {//itype
            params1.put("feature",params2.get("feature"));
        } else if (tag_key.contains("资费") && params2.containsKey("ipay")) {//ipay
            params1.put("ipay",params2.get("ipay"));
        } else if (tag_key.contains("资费") && params2.containsKey("pay")) {//ipay
            params1.put("pay",params2.get("pay"));
        } else if (tag_key.contains("特色") && params2.containsKey("characteristic")) {//characteristic
            params1.put("characteristic",params2.get("characteristic"));
        } else if (tag_key.contains("时间") && params2.containsKey("iyear")) {//iyear
            params1.put("iyear",params2.get("iyear"));
        } else if (tag_key.contains("年份") && params2.containsKey("year")) {//iyear
            params1.put("year",params2.get("year"));
        } else if (tag_key.contains("独家") && params2.containsKey("exclusive")) {//exclusive
            params1.put("exclusive",params2.get("exclusive"));
        } else if (tag_key.contains("分类") && params2.containsKey("item")) {//exclusive
            params1.put("item",params2.get("item"));
        } else if (tag_key.contains("状态") && params2.containsKey("anime_status")) {//exclusive
            params1.put("anime_status",params2.get("anime_status"));
        }
        StringBuffer sb = new StringBuffer(url.split("\\?")[0]);
        sb.append("?");

        for (String key : params1.keySet()) {
            sb.append(key);
            sb.append("=");
            sb.append(params1.get(key));
            sb.append("&");
        }
        return sb.toString().substring(0, sb.lastIndexOf("&"));
    }

    @Override
    public VideoGroup doSearch(String keyword) {
        VideoGroup vg = new VideoGroup();
        if (Utils.isEmpty(keyword)) {
            return vg;
        }

        JSONObject params = JSONObject.parseObject("{\"version\":\"24072901\",\"clientType\":1,\"filterValue\":\"\",\"uuid\":\""+ UUID.randomUUID().toString()+"\",\"retry\":0,\"query\":\""+keyword+"\",\"pagenum\":0,\"pagesize\":30,\"queryFrom\":0,\"searchDatakey\":\"\",\"transInfo\":\"\",\"isneedQc\":true,\"preQid\":\"\",\"adClientInfo\":\"\",\"extraInfo\":{\"isNewMarkLabel\":\"1\",\"multi_terminal_pc\":\"1\"}}");

        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("sec-ch-ua","\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"");
        headers.put("accept","*/*");
        headers.put("sec-ch-ua-mobile","?0");
        headers.put("content-type","application/json");
        headers.put("origin","https://v.qq.com");
        headers.put("sec-fetch-site","same-site");
        headers.put("sec-fetch-mode","cors");
        headers.put("sec-fetch-dest","empty");
        headers.put("accept-encoding","gzip, deflate, br");
        headers.put("accept-language","zh-CN,zh;q=0.9");
        headers.put("trpc-trans-info","{\"trpc-env\":\"\"}");

        HttpResult result = null;
        while (result == null || !result.isSign()) {
            try {
                result = this.hsu.doPostBody_Json("https://pbaccess.video.qq.com/trpc.videosearch.mobile_search.MultiTerminalSearch/MbSearch?vplatform=2", params.toString(), this.getServerUrl(), headers, "UTF-8");
            } catch (Exception e) {}
        }
        vg.setName("search");
        JSONObject initial_state = JSONObject.parseObject(result.getResult().trim().replace("@type", "type"));
        if(initial_state.getInteger("ret") != 0){
            return vg;
        }

        ArrayList<VideoInfo> videoInfos = new ArrayList<>();
        JSONArray itemList = initial_state.getJSONObject("data").getJSONObject("normalList").getJSONArray("itemList");
        for (int i = 0; i < itemList.size(); i++) {
            JSONObject item = itemList.getJSONObject(i);
            if(item.getJSONObject("videoInfo") != null && item.getJSONObject("videoInfo").containsKey("title")) {
                Constants.HotKeywordDBUtils.call(item.getJSONObject("videoInfo").getString("title"));
                JSONArray episodeSites = item.getJSONObject("videoInfo").getJSONArray("episodeSites");
                for (int j = 0; j < episodeSites.size(); j++) {
                    if (episodeSites.getJSONObject(j).getIntValue("uiType") == 1 || episodeSites.getJSONObject(j).getString("showName").equals("腾讯视频")) {
                        JSONArray episodeInfoList = episodeSites.getJSONObject(j).getJSONArray("episodeInfoList");


                        VideoInfo videoInfo = new VideoInfo();
                        videoInfo.setImage(item.getJSONObject("videoInfo").getString("imgUrl"));
                        videoInfo.setDescription(item.getJSONObject("videoInfo").getString("descrip"));
                        videoInfo.setName(item.getJSONObject("videoInfo").getString("title"));

                        //videoInfo.setTag(children_Card.getJSONObject("params").getString("topic_label"));
                        videoInfo.setTag("");
                        videoInfo.setUrl("https://v.qq.com/x/cover/" + episodeInfoList.getJSONObject(0).getString("id") + ".html");
                        if (!Constants.isBlack(videoInfo)) {
                            videoInfos.add(videoInfo);
                        }
                    }
                }
            }
        }
        vg.setItems(videoInfos);
        return vg;
    }

    @Override
    public LinkedHashMap<String, String> findResourceList(String vod_url, VideoRelateInfo vri) throws Exception {
        HttpResult result = null;
        while (result == null || !result.isSign()) {
            result = this.hsu.doGetBody(vod_url, null, this.headers, "UTF-8");
            if(result.indexOf(false,"NCTOKENSTR","NCAPPKEY")){
                result = null;
                Thread.sleep(1000);
            }
        }
        if(result.indexOf(false, "伦理片", "限制级")) {
            throw new StopException("Warning!限制级资源，禁止播放！");
        }

        Document doc = Jsoup.parse(result.getResult());
        String cover_id = null;
        try {
            String __pinia = Utils.getString(doc.html(), "\\<script\\>\\s*window\\.__PINIA[_].\\s*=\\s*(.*?)\\<\\/script\\>\\s*\\<\\/head\\>");
            __pinia = __pinia.replace(":undefined",":null");
            if(__pinia.indexOf("Array.prototype.slice.call")>=0){
                __pinia = __pinia.replaceAll("Array\\.prototype\\.slice\\.call\\((.*?)\\),", "$1,");
                __pinia = __pinia.replaceAll("Array\\.prototype\\.slice\\.call\\((.*?)\\),", "$1,");
            }
            JSONObject pinia = JSONObject.parseObject(__pinia.replaceAll("\\s*Array\\.prototype\\.slice\\.call\\(\\{", "{").replaceAll("\\}\\),\\s*\"tabs\":", "},\"tabs\":"));
            String video_name = pinia.getJSONObject("global").getJSONObject("coverInfo").getString("title");
            cover_id = pinia.getJSONObject("global").getJSONObject("coverInfo").getString("cover_id");
            vri.setName(video_name);
            String area_name = pinia.getJSONObject("global").getJSONObject("coverInfo").getString("area_name");;
            vri.setRegion(area_name);
            String publish_date = pinia.getJSONObject("global").getJSONObject("coverInfo").getString("publish_date");;
            vri.setRelease_time(publish_date);
            String leading_actor = pinia.getJSONObject("global").getJSONObject("coverInfo").getString("leading_actor");;
            vri.setActor(leading_actor);
            String description = pinia.getJSONObject("global").getJSONObject("coverInfo").getString("description");;
            vri.setDetail(description);
            vri.setScore(Utils.getString(__pinia, "\"score\":\\s*\"(.*?)\","));
            vri.setCategory(Utils.getString(__pinia, "main_genres\":\"(.*?)\","));
            vri.setIs_movie(__pinia.contains("type\":1"));
        } catch (Exception e){
            String video_name = Utils.getString(doc.html(), "\"coverInfo\":\\{.*?,\"title\":\"(.*?)\",");
            cover_id = Utils.getString(doc.html(),"\"cover_id\":\"(\\w+)\",");
            vri.setName(video_name);
            String area_name = Utils.getString(doc.html(), "\"area_name\":\"(.*?)\"");
            vri.setRegion(area_name);
            String publish_date = Utils.getString(doc.html(), "\"publish_date\":\"(.*?)\"");
            vri.setRelease_time(publish_date);
            String leading_actor = Utils.getString(doc.html(), "\"area_name\":\"(.*?)\"");
            vri.setActor(leading_actor);
            String description = Utils.getString(doc.html(), "\"leading_actor\":\\[(.*?)\\]");
            vri.setDetail(description);
            vri.setScore(Utils.getString(doc.html(), "\"score\":\\s*\"(.*?)\","));
            vri.setCategory(Utils.getString(doc.html(), "main_genres\":\"(.*?)\","));
            vri.setIs_movie(doc.html().contains("type\":1"));
        }

        if (Constants.Limit_Tags.contains(vri.getCategory())) {
            throw new StopException("当前影视资源被软件识别为敏感资源,无法正常播放！");
        }

        StringBuilder resource_data = new StringBuilder();
        int page_num = 0;
        while(this.getQQResourceList(page_num,cover_id,resource_data)){
            page_num++;
        }
        if(resource_data.toString().split("#").length>1){
            vri.setIs_movie(false);
        }
        String resource = resource_data.toString();
        if(vri.isIs_movie()){
            resource = resource_data.toString().split("\\$")[1];
        }
        LinkedHashMap<String, String> resourcelist = new LinkedHashMap<>();
        resourcelist.put("咸鱼云解析", resource);
        resourcelist.put("采集站搜索", resource);
        resourcelist.put("虾米解析", resource);
        resourcelist.put("醉仙解析", resource);
        resourcelist.put("yparse云解析", resource);
        return resourcelist;
    }

    private boolean getQQResourceList(int page_num,String cover_id,StringBuilder resource_sb){
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("sec-ch-ua","\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"");
        headers.put("accept","*/*");
        headers.put("sec-ch-ua-mobile","?0");
        headers.put("content-type","application/json");
        headers.put("origin","https://v.qq.com");
        headers.put("sec-fetch-site","same-site");
        headers.put("sec-fetch-mode","cors");
        headers.put("sec-fetch-dest","empty");
        headers.put("accept-encoding","gzip, deflate, br");
        headers.put("accept-language","zh-CN,zh;q=0.9");

        boolean has_next = false;
        try {
            int page_count = 100;
            String jsonparams = "{\"page_params\":{\"req_from\":\"web_vsite\",\"page_id\":\"vsite_episode_list\",\"page_type\":\"detail_operation\",\"id_type\":\"1\",\"page_size\":\"\",\"cid\":\""+cover_id+"\",\"vid\":\"\",\"lid\":\"\",\"page_num\":\"\",\"page_context\":\"chapter_name=&cid="+cover_id+"&detail_page_type=1&filter_rule_id=&id_type=1&is_nocopyright=false&is_skp_style=false&lid=&list_page_context=&mvl_strategy_id=&need_tab=1&order=&page_num="+page_num+"&page_size="+page_count+"&episode_begin="+((page_num*page_count)+1)+"&episode_end="+((page_num+1)*page_count)+"&episode_step="+page_count+"&req_from=web_vsite&req_from_second_type=&req_type=0&siteName=&tab_type=1&title_style=&ui_type=null&un_strategy_id=13dc6f30819942eb805250fb671fb082&watch_together_pay_status=0&year=\",\"detail_page_type\": \"1\"},\"has_cache\": 1}";
            HttpResult jx_result = null;
            while (jx_result == null || !jx_result.isSign()) {
                jx_result = this.hsu.doPostBody_Json("https://pbaccess.video.qq.com/trpc.universal_backend_service.page_server_rpc.PageServer/GetPageData?video_appid=3000010&vplatform=2&vversion_name=8.2.96", jsonparams, this.getServerUrl(), headers, "UTF-8");
            }
            JSONObject data = JSONObject.parseObject(jx_result.getResult()).getJSONObject("data");
            JSONArray module_list_datas = data.getJSONArray("module_list_datas");
            for(int i=0;i<module_list_datas.size();i++){
                JSONObject module_list_data = module_list_datas.getJSONObject(i);
                JSONArray module_datas = module_list_data.getJSONArray("module_datas");
                for(int j=0;j<module_datas.size();j++){
                    JSONArray item_datas = module_datas.getJSONObject(j).getJSONObject("item_data_lists").getJSONArray("item_datas");
                    has_next = item_datas.size()>=page_count;

                    int skip_count = 0;//预告片数量
                    for(int l=0;l<item_datas.size();l++){
                        JSONObject item_data = item_datas.getJSONObject(l);
                        JSONObject item_params = item_data.getJSONObject("item_params");
                        if(!item_params.containsKey("cid") || item_params.getInteger("duration") < 120 && (item_params.getString("title").contains("预告") || item_params.getString("title").contains("彩蛋") || item_params.getString("play_title").contains("预告") || item_params.getString("union_title").contains("预告"))){
                            skip_count++;
                            continue;
                        }
                        String url = this.getServerUrl() + "x/cover/"+item_params.getString("cid")+"/"+item_params.getString("vid")+".html";
                        String video_name = "";
                        try {
                            Integer.valueOf(item_params.getString("title"));
                            video_name = "第"+item_params.getString("title")+"集";//((page_num*100)+l+1)
                        } catch (Exception ex){
                            video_name = "第"+((page_num*page_count)+l+1-skip_count)+"集";//((page_num*100)+l+1)item_params.getString("title");
                        }
                        if (resource_sb.length() > 0) {
                            resource_sb.append("#");
                        }
                        resource_sb.append(video_name);
                        resource_sb.append("$");
                        resource_sb.append(url);
                        resource_sb.append("$");
                        try {
                            Integer.valueOf(item_params.getString("title"));
                            resource_sb.append(item_params.getString("play_title").replaceAll("\\$", "￥").replace("#", "@"));//video_subtitle
                        } catch (Exception ex){
                            resource_sb.append(item_params.getString("title").replaceAll("\\$", "￥").replace("#", "@"));
                        }
                    }
                }
            }
        } catch (Exception ex){}
        return has_next;
    }

    @Override
    public String getPageUrl(String page_url, int page){
        if(!page_url.contains("&page=")){
            page_url+="&page=0";
        }
       return page_url.replaceAll("page=\\d+", "page="+page);
    }

    private String getLocation(HttpResult result){
        String location_href = Utils.getString(result.getResult(),"location.href\\s*=\\s*\"(.*?)\";");
        location_href = Utils.resolveURI(result.getFinal_URL(),location_href);
        return location_href;
    }
}
