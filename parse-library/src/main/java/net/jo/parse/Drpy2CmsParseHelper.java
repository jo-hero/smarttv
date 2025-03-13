package net.jo.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.tvbox.quickjs.JSArray;
import com.github.tvbox.quickjs.JSModule;
import com.github.tvbox.quickjs.JSObject;
import com.github.tvbox.quickjs.QuickJSContext;

import net.jo.bean.Callback;
import net.jo.common.Constants;
import net.jo.common.StopException;
import net.jo.common.Utils;
import net.jo.common.js.JSEngine;
import net.jo.http.HttpResult;
import net.jo.http.HttpSimpleUtils;
import net.jo.iparse.IParseHelper;
import net.jo.model.NavItem;
import net.jo.model.ParseResult;
import net.jo.model.VideoGroup;
import net.jo.model.VideoInfo;
import net.jo.model.VideoRelateInfo;

import org.jsoup.Jsoup;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * https://tkzy7.com/api.php/provide/vod/?ac=list
 * https://tkzy7.com/api.php/provide/vod/?ac=detail 有些maccms10 是 videolist
 * ids 影片ID 多个使用,隔开
 * t 类型
 * h 最近多少小时内
 * pg 页数
 * wd 搜索like
 * at 输出格式 可选xml json
 *
 * key = hkckfglfdjkdksdk ASCII
 * iv = cmfjdocktdeslfcg ASCII
 * AES/CBC/PKCS5Padding
 * https://api.yakangyl.com/TomorrowMovies/search?version=9&keys=HZ&page=0&pageSize=30
 *
 * https://taopianapi.com/cjapi/mc/vod/json/m3u8.html?&ac=list&wd=%E9%95%BF%E7%9B%B8%E6%80%9D
 */
public class Drpy2CmsParseHelper implements IParseHelper {
    private JSONObject config;
    private HttpSimpleUtils hsu;
    private NavItem nav_template;
    private Map<String, String> headers = new HashMap();
    private JSObject jsObject = null;
    private JSEngine.JSThread jsThread = null;
    //{"class":[{"type_id":"dianying","type_name":"电影"},{"type_id":"dianshiju","type_name":"电视剧"},{"type_id":"dongman","type_name":"动漫"},{"type_id":"zongyi","type_name":"综艺"}],"filters":{"dianying":[{"key":"class","name":"剧情","value":[{"n":"全部","v":""},{"n":"剧情","v":"剧情"},{"n":"喜剧","v":"喜剧"},
    private JSONObject clazzs = null;
    private List<String> all_serverUrls = new ArrayList<String>();
    private String serverUrl = "https://dianying.im/";
    private String ext_config = null;

    public String getServerUrl(){
        return this.serverUrl;
    }

    public HttpSimpleUtils getHsu() {
        return hsu;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Drpy2CmsParseHelper(JSONObject config, NavItem nav_template){
        this.config = config;
        this.all_serverUrls = Utils.parseUrls(config.getJSONArray("urls"));
        this.nav_template = nav_template;
        this.hsu = new HttpSimpleUtils();
        this.headers.put("Connection", "keep-alive");
        this.headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        this.headers.put("Accept-Encoding", "gzip, deflate");
        this.headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        this.headers.put("Upgrade-Insecure-Requests", "1");
    }

    @Override
    public LinkedHashMap<String, String> parseHomePage(ArrayList<VideoGroup> vgs) {int rollCount = 0;
        HttpResult result = null;
        while (result == null || !result.isSign()) {
            this.serverUrl = this.all_serverUrls.get(rollCount % this.all_serverUrls.size());
            rollCount++;
            result = this.hsu.doGetBody(this.getServerUrl(), null, headers, "UTF-8");
            if(result == null || !result.isSign()){
                try {
                    Thread.sleep(2000);
                } catch(Exception ex){}
            } else if(result.indexOf(false, "收藏本页面到浏览器")){
                result = null;
            }
        }
        result = null;
        while(result == null || !result.isSign()) {
            result = this.hsu.doGetBody(config.getString("ext"), null, null, "UTF-8");
        }
        this.ext_config = result.getResult().trim();
        this.ext_config = this.ext_config.replaceAll("([\"']*)host([\"']*):([\"']*).*?([\"']*),", "$1host$2:$3"+this.serverUrl+"$4,");

        checkLoaderJS();
        try {
            this.clazzs = JSONObject.parseObject(postFunc("home", true));
            VideoGroup vg = this.parse(postFunc("homeVod"), "推荐", 20);
            vg.getNavs().clear();
            vgs.add(vg);
            vgs.get(0).setSlide_items(new ArrayList<VideoInfo>());

            LinkedHashMap<String, String> tabs = new LinkedHashMap<>();
            if(clazzs.containsKey("filters")){
                for(int i=0;i<clazzs.getJSONArray("class").size();i++){
                    JSONObject clazz = clazzs.getJSONArray("class").getJSONObject(i);
                    if(clazzs.getJSONObject("filters").containsKey(clazz.getString("type_id"))){
                        tabs.put(clazz.getString("type_name"),   "category://"+clazz.getString("type_id")+"?pg=1");//https:/www.youku.com/category/data?c=97&type=show&p=2
                    }
                }
            }
            return tabs;
        } catch (Exception ex){
            return null;
        }
    }

    @Override
    public VideoGroup parse(String html_content, String nav_name, int limit) {
        VideoGroup vg = new VideoGroup();
        //如果html_content是category://开头说明只是一个链接，需要动态获取
        if(html_content.toLowerCase().startsWith("category://")){
            Map<String, String> selected_filters = new HashMap<String, String>();
            String category_id = html_content.replace("category://", "").split("\\?")[0];
            String pg = "1";
            try {
                String exts = html_content.replace("category://", "").split("\\?")[1];
                for(String ext : exts.split("&")){
                    if(Utils.isEmpty(ext)){
                        continue;
                    }
                    if(ext.split("=")[0].equals("pg")){
                        pg = ext.split("=")[1];
                        continue;
                    }
                    selected_filters.put(ext.split("=")[0], ext.split("=")[1]);
                }
            } catch (Exception ex){}

            vg.setAll_tags(getTags(category_id, selected_filters));
            if(limit > -1){
                String tag = ((String[]) vg.getAll_tags().keySet().toArray(new String[0]))[0];
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
            //动态请求内容数据
            html_content = this.categoryContent(category_id, pg, true, selected_filters);
        }

        JSONArray vod_lists = null;
        try {
            JSONObject vods = JSONObject.parseObject(html_content);
            if(vods.containsKey("pagecount")){
                vg.setPagecount(vods.getInteger("pagecount"));
            }
            vod_lists = vods.getJSONArray("list");
        } catch (Exception ex){
            vod_lists = JSONArray.parseArray(html_content);
        }
        ArrayList<VideoInfo> videoInfos = new ArrayList<>();
        for (int i = 0; i<vod_lists.size(); i++) {
            JSONObject vod = vod_lists.getJSONObject(i);
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setName(vod.getString("vod_name"));
            videoInfo.setTag(vod.getString("vod_remarks"));
            videoInfo.setDescription(vod.getString("vod_content"));
            videoInfo.setImage(vod.getString("vod_pic"));
            videoInfo.setUrl(vod.getString("vod_id"));

            if (limit < 0 || videoInfos.size() < limit) {
                videoInfos.add(videoInfo);
            }
            Constants.HotKeywordDBUtils.call(videoInfo.getName());
        }
        vg.setName(nav_name);
        vg.setItems(videoInfos);
        return vg;
    }

    @Override
    public ParseResult parse(String episode_tag, String video_name, String video_alias_name,String resource_tag, String resource_url, Callback callback) throws Exception {
        VideoGroup vg = new VideoGroup();
        vg.setName(video_name+"-parse");
        vg.setItems(((VideoGroup) Constants.home_datas.get(new Random().nextInt(Constants.home_datas.size()))).getItems());
        Constants.ParseCallback.call(vg);
        try {
            callback.call(60, resource_url);
            String playerContent = this.playerContent(resource_tag, resource_url, null);
            //{"parse":1,"jx":0,"url":"https://vip.lz-cdn12.com/20230818/9327_99a880c2/index.m3u8"}
            String player_url = JSONObject.parseObject(playerContent).getString("url");
            ParseResult pr = new ParseResult(URLDecoder.decode(player_url,"UTF-8"), resource_url,null);
            //清空广告
            AdsUtils.removeAds(config, pr);
            return pr;
        } catch (Exception e) {
            throw new StopException("视频源解析失败,赶紧联系JO(有时重启软件也有用)...");
        }
    }

    @Override
    public String composeTagUrl(String current_url, String tag_url, String tag_key) {
        Map<String, String> selected_filters = new HashMap<String, String>();
        String category_id = current_url.replace("category://", "").split("\\?")[0];
        try {
            String exts = current_url.replace("category://", "").split("\\?")[1];
            for(String ext : exts.split("&")){
                if(Utils.isEmpty(ext)){
                    continue;
                }
                selected_filters.put(ext.split("=")[0], ext.split("=")[1]);
            }
        } catch (Exception ex){}

        try {
            String exts = tag_url.replace("category://", "").split("\\?")[1];
            for(String ext : exts.split("&")){
                if(Utils.isEmpty(ext)){
                    continue;
                }
                selected_filters.put(ext.split("=")[0], ext.split("=")[1]);
            }
        } catch (Exception ex){}

        StringBuffer url_query_params = new StringBuffer();
        for (String filter : selected_filters.keySet()) {
            if(url_query_params.length() > 0){
                url_query_params.append("&");
            } else {
                url_query_params.append("?");
            }
            url_query_params.append(filter);
            url_query_params.append("=");
            url_query_params.append(selected_filters.get(filter));
        }
        return "category://"+category_id + url_query_params.toString();
    }

    @Override
    public LinkedHashMap<String, String> findResourceList(String vod_url, VideoRelateInfo vri) throws Exception {
        //{"list":[{"vod_id":"http://dyxs29.com/show-257112/","vod_name":"疗愈心伤剧场版","vod_pic":"https://pic1.bdzyimg.com/upload/vod/20230819-1/c0ff7634efca0d6eb47a713b5216740d.jpg","type_name":"电影2021剧情日本","vod_year":"","vod_area":"",
        // "vod_remarks":"剧情： ","vod_actor":"","vod_director":"/桑原亮子",
        // "vod_content":"",
        // "vod_play_from":"BF节点$$$LZ节点$$$HD节点$$$WJ节点$$$SD节点$$$UK节点$$$DB节点$$$LZ节点1",
        // "vod_play_url":"中字$http://dyxs29.com/paly-257112-6-1/$$$HD中字$http://dyxs29.com/paly-257112-3-1/$$$HD中字$http://dyxs29.com/paly-257112-1-1/$$$中字$http://dyxs29.com/paly-257112-4-1/$$$中字$http://dyxs29.com/paly-257112-5-1/$$$1080官方中字$http://dyxs29.com/paly-257112-2-1/$$$中字$http://dyxs29.com/paly-257112-7-1/$$$"}]}

        //{"list":[{"vod_id":"http://dyxs6.xyz/show-208145/","vod_name":"超能一家人","vod_pic":"https://pic5.iqiyipic.com/image/20230818/0b/5d/v_154455866_m_601_m8_579_772.jpg","type_name":"电影2022喜剧/奇幻/科幻中国大陆","vod_year":"","vod_area":"","vod_remarks":"上映： 2022-02-01(中国大陆)","vod_actor":"/艾伦/沈腾/陶慧/张琪/韩彦博/白丽娜/草莓/安德烈·拉泽夫/张子墨/马丽","vod_director":"/宋阳","vod_content":"主人公郑前（艾伦饰）离家出走漂泊多年，开发了一款“理财神器”APP，不料却被家乡喀西契克市邪恶狡猾的市长乞乞科夫（沈腾饰）盯上。而此时郑前一家人竟遇到天降陨石获得了超能力，但只要有人离开，超能力便会消失。郑前被迫和不靠谱的家人团结起来，共同抵抗乞乞科夫，上演一场超能力VS钞能力的爆笑故事……","vod_play_from":"FF节点$$$BF节点$$$YK节点$$$QY节点$$$LZ节点$$$HD节点$$$KK节点$$$HN节点$$$TK节点$$$WJ节点$$$SD节点$$$BJ节点$$$UK节点$$$DB节点$$$LZ节点1","vod_play_url":"HD国语版$http://dyxs6.xyz/paly-208145-8-1/$$$HD$http://dyxs6.xyz/paly-208145-3-1/$$$高清正片$http://dyxs6.xyz/paly-208145-13-1/$$$高清正片$http://dyxs6.xyz/paly-208145-12-1/$$$HD中字$http://dyxs6.xyz/paly-208145-9-1/$$$HD国语$http://dyxs6.xyz/paly-208145-6-1/$$$1080P$http://dyxs6.xyz/paly-208145-11-1/$$$正片$http://dyxs6.xyz/paly-208145-4-1/$$$HD$http://dyxs6.xyz/paly-208145-10-1/$$$HD$http://dyxs6.xyz/paly-208145-2-1/$$$HD$http://dyxs6.xyz/paly-208145-1-1/$$$HD$http://dyxs6.xyz/paly-208145-14-1/$$$720P$http://dyxs6.xyz/paly-208145-5-1/$$$HD$http://dyxs6.xyz/paly-208145-7-1/$$$"}]}
        LinkedHashMap<String, String> resourcelist = new LinkedHashMap<>();
        try {
            String detailContent = postFunc("detail", vod_url);
            JSONObject vod_detail = JSONObject.parseObject(detailContent).getJSONArray("list").getJSONObject(0);
            vri.setName(vod_detail.getString("vod_name"));
            vri.setRelease_time(vod_detail.getString("vod_year"));
            vri.setRegion(vod_detail.getString("vod_area"));
            vri.setActor(vod_detail.getString("vod_actor") + "("+vod_detail.getString("vod_director")+")");
            vri.setLast_updatetime(vod_detail.getString("vod_year"));//vod_time_add
            vri.setLength("");
            vri.setDetail(Jsoup.parse(vod_detail.getString("vod_content")).text().trim());
            if(vod_detail.containsKey("type_name")){
                vri.setCategory(vod_detail.getString("type_name"));
                vri.setIs_movie(vod_detail.getString("type_name").startsWith("电影"));
            } else {
                vri.setCategory("");
            }
            vri.setLength("");


            //解析剧集 提取出m3u8数据
            String vod_play_note = vod_detail.getString("vod_play_note");
            if(vod_detail.getString("vod_play_from").contains("$$$")){
                vod_play_note = "$$$";
            }
            String[] vod_play_froms = null;
            String[] vod_play_urls = null;
            if(Utils.isEmpty(vod_play_note)){
                vod_play_urls = new String[]{vod_detail.getString("vod_play_url")};
                vod_play_froms = new String[]{vod_detail.getString("vod_play_from")};
            } else {
                vod_play_note = vod_play_note.replace("$", "\\$");
                vod_play_urls = vod_detail.getString("vod_play_url").split(vod_play_note);
                vod_play_froms = vod_detail.getString("vod_play_from").split(vod_play_note);
            }

            for(int i = 0; i < vod_play_urls.length; i++){
                String vod_play_url = vod_play_urls[i];
                if(Utils.isEmpty(vri.getCategory()) && vod_play_url.split("#").length == 1){
                    vri.setCategory("电影");
                    vri.setIs_movie(true);
                }
                if (vri.isIs_movie()) {
                    for(String play_url : vod_play_url.split("#")){
                        resourcelist.put(vod_play_froms[i] + "[" + play_url.split("\\$")[0] + "]", play_url.split("\\$")[1]);
                    }
                } else {
                    resourcelist.put(vod_play_froms[i], vod_play_url);
                }
            }
        } catch (Exception e){}

        if (Constants.Limit_Tags.contains(vri.getCategory())) {
            throw new StopException("当前影视资源被软件识别为敏感资源,无法正常播放！");
        }
        return resourcelist;
    }

    //{"page":1,"pagecount":10,"limit":20,"total":100,"list":[{"vod_id":"http://dyxs29.com/show-256645/","vod_name":"孤注一掷","vod_pic":"http://dyxs29.com/upload/vod/20230811-1/48700b18d32f0a2930eeb0997806c8bd.jpg","vod_remarks":"TC抢先版","vod_content":""},{"vod_id":"http://dyxs29.com/show-234379/","vod_name":"孤注一掷","vod_pic":"http://dyxs29.com/upload/vod/20220921-3/4a213669c0948531521294535875c9f8.jpg","vod_remarks":"HD","vod_content":""},{"vod_id":"http://dyxs29.com/show-59786/","vod_name":"孤注一掷","vod_pic":"http://dyxs29.com/upload/vod/20210111-43/a5a021bd0a601cc0ae85224280df5d67.jpg","vod_remarks":"","vod_content":""},{"vod_id":"http://dyxs29.com/show-256792/","vod_name":"孤注一掷真实背景揭秘","vod_pic":"http://dyxs29.com/upload/vod/20230811-1/0d58136ddae7f6289155a3a6f7b572e0.jpg","vod_remarks":"第03集","vod_content":""},{"vod_id":"http://dyxs29.com/show-229403/","vod_name":"孤注一掷：阿森纳","vod_pic":"http://dyxs29.com/upload/vod/20220805-1/d18167440008040e65f4eab161b4d074.jpg","vod_remarks":"已完结","vod_content":""},{"vod_id":"http://dyxs29.com/show-233510/","vod_name":"孤注一掷：尤文图斯","vod_pic":"http://dyxs29.com/upload/vod/20220921-4/e992a992b0c28b3a704b63e0c9b964a9.jpg","vod_remarks":"更新至8集","vod_content":""},{"vod_id":"http://dyxs29.com/show-228752/","vod_name":"孤注一掷：托特纳姆热刺","vod_pic":"http://dyxs29.com/upload/vod/20220728-1/bc678fa95131a48d9e79a3b244a9b2a7.jpg","vod_remarks":"完结","vod_content":""},{"vod_id":"http://dyxs29.com/show-186892/","vod_name":"辛纳特拉：孤注一掷","vod_pic":"http://dyxs29.com/upload/vod/20210322-1/83d0047e9f22e9eadcdca8429fa68144.jpg","vod_remarks":"已完结","vod_content":""},{"vod_id":"http://dyxs29.com/show-229327/","vod_name":"孤注一掷：巴西国家队","vod_pic":"http://dyxs29.com/upload/vod/20220804-1/4a480fb909166d43d65b9e149ba2b2ef.jpg","vod_remarks":"完结","vod_content":""},{"vod_id":"http://dyxs29.com/show-98697/","vod_name":"孤注一掷：曼彻斯特城","vod_pic":"http://dyxs29.com/upload/vod/20210111-20/a477dcb15cbccc1b5e9c2a062ea89a77.jpg","vod_remarks":"更新至8集","vod_content":""},{"vod_id":"http://dyxs29.com/show-204432/","vod_name":"族长老爹的葬礼2：孤注一掷","vod_pic":"http://dyxs29.com/upload/vod/20220106-1/8c334f115497eaea171079fb92894107.jpg","vod_remarks":"已完结","vod_content":""}]}
    @Override
    public VideoGroup doSearch(String keyword) {
        if (Utils.isEmpty(keyword)) {
            return new VideoGroup();
        }
        String searchContent = postFunc("search", keyword, false);
        return this.parse(searchContent, "search", -1);
    }

    @Override
    public String getPageUrl(String page_url, int page){
        if(!page_url.contains("&pg=") && !page_url.contains("?pg=")){
            if(page_url.contains("?")){
                page_url += "&pg=" + page;
            } else {
                page_url += "?pg=" + page;
            }
        } else {
            page_url = page_url.replaceAll("([&\\?]*)pg=\\d*", "$1pg="+page);
        }
        return page_url;
    }

    private LinkedHashMap<String, LinkedHashMap<String, String>> getTags(String category_id, Map<String, String> selected_filters) {
        LinkedHashMap<String, LinkedHashMap<String, String>> all_tags = new LinkedHashMap<>();
        if(this.clazzs.containsKey("filters")){
            JSONArray filters = this.clazzs.getJSONObject("filters").getJSONArray(category_id);
            for (int i = 0; i < filters.size(); i++) {
                String filter_key  = filters.getJSONObject(i).getString("key");
                String filter_name = filters.getJSONObject(i).getString("name");
                JSONArray values   = filters.getJSONObject(i).getJSONArray("value");
                LinkedHashMap<String, String> tags = new LinkedHashMap<>();
                for (int j = 0; j < values.size(); j++) {
                    JSONObject value = values.getJSONObject(j);
                    if ((selected_filters != null && selected_filters.containsKey(filter_key) && selected_filters.get(filter_key).equals(value.getString("v")))) {
                        tags.put(value.getString("n").equals("全部") ? ("全部"+filter_name) : value.getString("n"), "category://"+category_id + "?"+filter_key+"="+value.getString("v")+"&?active");
                    } else {
                        tags.put(value.getString("n").equals("全部") ? ("全部"+filter_name) : value.getString("n"), "category://"+category_id + "?"+filter_key+"="+value.getString("v"));
                    }
                }
                all_tags.put(filter_name, tags);
            }
        } else if(!this.clazzs.containsKey("filters") && this.clazzs.containsKey("class") && this.clazzs.getJSONArray("class").size() > 0){
            JSONArray filters = this.clazzs.getJSONArray("class");
            LinkedHashMap<String, String> tags = new LinkedHashMap<>();
            for (int i = 1; i < filters.size()-1; i++) {
                String type_id  = filters.getJSONObject(i).getString("type_id");
                String type_name = filters.getJSONObject(i).getString("type_name");
                if (category_id.equals(type_id)) {
                    tags.put(type_name.equals("全部") ? ("全部"+type_name) : type_name, "category://"+type_id+"?active");
                } else {
                    tags.put(type_name.equals("全部") ? ("全部"+type_name) : type_name, "category://"+type_id);
                }
            }
            all_tags.put("分类", tags);
        }
        return all_tags;
    }

    private void checkLoaderJS() {
        if (jsThread == null) {
            jsThread = JSEngine.getInstance().getJSThread(this.hsu);
        }
        if (jsObject == null && jsThread != null) {
            try {
                jsThread.postVoid(new JSEngine.Event<Void>() {
                    @Override
                    public Void run(QuickJSContext ctx, JSObject globalThis) {
                        String moduleKey = "__" + UUID.randomUUID().toString().replace("-", "") + "__";
                        String jsContent = JSEngine.getInstance().loadModule(hsu, config.getString("api"));
                        try {
                            if (config.getString("api").contains(".js?")) {
                                int spIdx   = config.getString("api").indexOf(".js?");
                                String[] query = config.getString("api").substring(spIdx + 4).split("&|=");
                                String api  = config.getString("api").substring(0, spIdx);
                                for (int i = 0; i < query.length; i += 2) {
                                    String key = query[i];
                                    String val = query[i + 1];
                                    String sub = JSModule.convertModuleName(api, val);
                                    String content = JSEngine.getInstance().loadModule(hsu, sub);
                                    jsContent = jsContent.replace("__" + key.toUpperCase() + "__", content);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (jsContent.contains("export default{")) {
                            jsContent = jsContent.replace("export default{", "globalThis." + moduleKey + " ={");
                        } else if (jsContent.contains("export default {")) {
                            jsContent = jsContent.replace("export default {", "globalThis." + moduleKey + " ={");
                        } else {
                            jsContent = jsContent.replace("__JS_SPIDER__", "globalThis." + moduleKey);
                        }
                        ctx.evaluateModule(jsContent, config.getString("api"));
                        jsObject = (JSObject) ctx.getProperty(globalThis, moduleKey);
                        jsObject.getJSFunction("init").call(ext_config);
                        return null;
                    }
                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private String postFunc(final String func, final Object... args) {
        checkLoaderJS();
        if (jsObject != null) {
            try {
                return jsThread.post(new JSEngine.Event<String>() {
                    @Override
                    public String run(QuickJSContext ctx, JSObject globalThis) {
                        return (String) jsObject.getJSFunction(func).call(args);
                    }
                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return "";
    }

    private String categoryContent(String tid, String pg, boolean filter, final Map<String, String> selected_filters) {
        try {
            JSObject obj = jsThread.post(new JSEngine.Event<JSObject>() {
                @Override
                public JSObject run(QuickJSContext ctx, JSObject globalThis) {
                    JSObject o = ctx.createNewJSObject();
                    if (selected_filters != null) {
                        for (String s : selected_filters.keySet()) {
                            o.setProperty(s, selected_filters.get(s));
                        }
                    }
                    return o;
                }
            });
            return postFunc("category", tid, pg, filter, obj);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return "";

    }

    private String playerContent(String flag, String id, final List<String> vipFlags) {
        try {
            JSArray array = jsThread.post(new JSEngine.Event<JSArray>() {
                @Override
                public JSArray run(QuickJSContext ctx, JSObject globalThis) {
                    JSArray arr = ctx.createNewJSArray();
                    if (vipFlags != null) {
                        for (int i = 0; i < vipFlags.size(); i++) {
                            arr.set(vipFlags.get(i), i);
                        }
                    }
                    return arr;
                }
            });
            return postFunc("play", flag, id, array);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return "";
    }
}
