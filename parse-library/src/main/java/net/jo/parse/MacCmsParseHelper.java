package net.jo.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.jo.common.Constants;
import net.jo.common.StopException;
import net.jo.common.Utils;
import net.jo.http.HttpResult;
import net.jo.http.HttpSimpleUtils;
import net.jo.model.NavItem;
import net.jo.model.ParseResult;
import net.jo.model.VideoGroup;
import net.jo.model.VideoInfo;
import net.jo.model.VideoRelateInfo;

import net.jo.bean.Callback;
import net.jo.iparse.IParseHelper;

import org.jsoup.Jsoup;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
 *
 *  $param['page'] = intval($input['page']) <1 ? 1 : intval($input['page']);
 *     $param['ajax'] = intval($input['ajax']);
 *     $param['tid'] = intval($input['tid']);
 *     $param['mid'] = intval($input['mid']);
 *     $param['rid'] = intval($input['rid']);
 *     $param['pid'] = intval($input['pid']);
 *     $param['sid'] = intval($input['sid']);
 *     $param['nid'] = intval($input['nid']);
 *     $param['uid'] = intval($input['uid']);
 *     $param['level'] = intval($input['level']);
 *     $param['score'] = intval($input['score']);
 *     $param['limit'] = intval($input['limit']);
 *
 *     $param['id'] = htmlspecialchars(urldecode(trim($input['id'])));
 *     $param['ids'] = htmlspecialchars(urldecode(trim($input['ids'])));
 *     $param['wd'] = htmlspecialchars(urldecode(trim($input['wd'])));
 *     $param['en'] = htmlspecialchars(urldecode(trim($input['en'])));
 *     $param['state'] = htmlspecialchars(urldecode(trim($input['state'])));
 *     $param['area'] = htmlspecialchars(urldecode(trim($input['area'])));
 *     $param['year'] = htmlspecialchars(urldecode(trim($input['year'])));
 *     $param['lang'] = htmlspecialchars(urldecode(trim($input['lang'])));
 *     $param['letter'] = htmlspecialchars(trim($input['letter']));
 *     $param['actor'] = htmlspecialchars(urldecode(trim($input['actor'])));
 *     $param['director'] = htmlspecialchars(urldecode(trim($input['director'])));
 *     $param['tag'] = htmlspecialchars(urldecode(trim($input['tag'])));
 *     $param['class'] = htmlspecialchars(urldecode(trim($input['class'])));
 *     $param['order'] = htmlspecialchars(urldecode(trim($input['order'])));
 *     $param['by'] = htmlspecialchars(urldecode(trim($input['by'])));
 *     $param['file'] = htmlspecialchars(urldecode(trim($input['file'])));
 *     $param['name'] = htmlspecialchars(urldecode(trim($input['name'])));
 *     $param['url'] = htmlspecialchars(urldecode(trim($input['url'])));
 *     $param['type'] = htmlspecialchars(urldecode(trim($input['type'])));
 *     $param['sex'] = htmlspecialchars(urldecode(trim($input['sex'])));
 *     $param['version'] = htmlspecialchars(urldecode(trim($input['version'])));
 *     $param['blood'] = htmlspecialchars(urldecode(trim($input['blood'])));
 *     $param['starsign'] = htmlspecialchars(urldecode(trim($input['starsign'])));
 *     $param['domain'] = htmlspecialchars(urldecode(trim($input['domain'])));
 * AES/CBC/PKCS5Padding
 * https://api.yakangyl.com/TomorrowMovies/search?version=9&keys=HZ&page=0&pageSize=30
 * #EXT-X-DISCONTINUITY\s+#EXTINF:\d+.\d+,\s+\w+.ts\s+#EXTINF:\d+.\d+,\s+\w+.ts\s+#EXTINF:\d+.\d+,\s+\w+.ts\s+#EXTINF:\d+.\d+,\s+\w+.ts\s+#EXT-X-ENDLIST
 * https://taopianapi.com/cjapi/mc/vod/json/m3u8.html?&ac=list&wd=%E9%95%BF%E7%9B%B8%E6%80%9D
 */
public class MacCmsParseHelper implements IParseHelper {
    private JSONObject config;
    private HttpSimpleUtils hsu;
    private NavItem nav_template;
    private Map<String, String> headers = new HashMap();
    private List<String> all_serverUrls = new ArrayList<String>();
    private String serverUrl;
    private String ac = "detail";
    private String path = "/api.php/provide/vod/";

    public String getServerUrl(){
        return this.serverUrl + this.path;
    }

    public HttpSimpleUtils getHsu() {
        return hsu;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public MacCmsParseHelper(JSONObject config, NavItem nav_template){
        this.config = config;
        if(config.containsKey("limits")){
            for(String tag : config.getString("limits").split("\\$")){
                Constants.Limit_Tags.add(tag);
            }
        }
        if(this.config.containsKey("ac") && !Utils.isEmpty(this.config.getString("ac"))){
            this.ac = this.config.getString("ac");
        }
        if(this.config.containsKey("path") && !Utils.isEmpty(this.config.getString("path"))){
            this.path = this.config.getString("path");
        }
        this.all_serverUrls = Utils.parseUrls(config.getJSONArray("urls"));
        this.nav_template = nav_template;
        this.hsu = new HttpSimpleUtils();
        this.headers.put("Connection", "keep-alive");
        this.headers.put("Accept", "text/text(),application/xtext()+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        this.headers.put("Accept-Encoding", "gzip, deflate");
        this.headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        this.headers.put("Upgrade-Insecure-Requests", "1");
    }

    @Override
    public LinkedHashMap<String, String> parseHomePage(ArrayList<VideoGroup> vgs) {
        int rollCount = 0;
        HttpResult result = null;
        while (result == null || !result.isSign() || !result.indexOf(false, "pagecount\":")) {
            this.serverUrl = this.all_serverUrls.get(rollCount % this.all_serverUrls.size());
            rollCount++;
            result = this.hsu.doGetBody(this.getServerUrl()+"?ac="+this.ac, null, headers, "UTF-8");
            if(result == null || !result.isSign() || !result.indexOf(false, "pagecount\":")){
                try {
                    Thread.sleep(5000);
                } catch(Exception ex){}
            }
        }
        VideoGroup vg = this.parse(result.getResult(), "推荐", 20);
        vg.getNavs().clear();
        vgs.add(vg);

        //加入广告部分
        vg.setSlide_items(new ArrayList<VideoInfo>());

        JSONArray vods_clazzs = this.getVodClass();
        LinkedHashMap<String, String> tabs = new LinkedHashMap<>();
        for(int i=0; i<vods_clazzs.size();i++){
            JSONObject vod_clazz = vods_clazzs.getJSONObject(i);
            String type_id = vod_clazz.getString("type_id");
            if(Constants.Limit_Tags.contains(vod_clazz.getString("type_name"))){
                continue;
            }
            if(vod_clazz.getString("type_name").contains("解说") || vod_clazz.getString("type_name").contains("预告")){
                continue;
            }
            if(vod_clazz.getInteger("type_pid") == 0 && (vod_clazz.getString("type_name").contains("电影") || vod_clazz.getString("type_name").contains("连续剧") || vod_clazz.getString("type_name").contains("电视剧") || vod_clazz.getString("type_name").contains("动漫") || vod_clazz.getString("type_name").contains("综艺") || vod_clazz.getString("type_name").contains("体育"))){
                //提前把电影类别的ID提取出来
                if(vod_clazz.getString("type_name").contains("电影")){
                    dy_type_id = vod_clazz.getInteger("type_id");
                }
                for(int j=0; j<vods_clazzs.size();j++){
                    JSONObject tmp_vod_clazz = vods_clazzs.getJSONObject(j);
                    if(tmp_vod_clazz.getInteger("type_pid") == vod_clazz.getInteger("type_id")){
                        if(tmp_vod_clazz.getString("type_name").contains("国产") || tmp_vod_clazz.getString("type_name").contains("科幻") || tmp_vod_clazz.getString("type_name").contains("大陆") || tmp_vod_clazz.getString("type_name").contains("足球")){
                            type_id = tmp_vod_clazz.getString("type_id");
                        }
                    }
                }
                tabs.put(vod_clazz.getString("type_name").replace("片", ""),   this.getServerUrl() + "?ac=" + this.ac + "&t="+type_id);
            }
        }
        return tabs;
    }

    @Override
    public VideoGroup parse(String html_content, String nav_name, int limit) {
        VideoGroup vg = new VideoGroup();
        //如果html_content是https://开头说明只是一个链接，需要动态获取
        if(html_content.toLowerCase().startsWith("https://") || html_content.toLowerCase().startsWith("http://")){
            HttpResult result = null;
            while (result == null || !result.isSign()) {
                result = this.hsu.doGetBody(html_content, this.getServerUrl(), headers, "UTF-8");
                if(result == null || !result.isSign()){
                    try {
                        Thread.sleep(5000);
                    } catch(Exception ex){}
                }
            }
            html_content = result.getResult().trim();
            result = null;
            vg.setAll_tags(getTags(html_content));
            if(limit > -1){
                ArrayList<NavItem> navItems = new ArrayList<>();
                if(!Utils.isEmpty(vg.getAll_tags())){
                    LinkedHashMap<String, String> tag = vg.getAll_tags().entrySet().iterator().next().getValue();
                    int i = 0;
                    for (String catalog : tag.keySet()) {
                        navItems.add(NavItem.newByTemplate(nav_template, catalog,  tag.get(catalog)));
                        i++;
                        if (i >= 8) {
                            break;
                        }
                    }
                }
                vg.setNavs(navItems);
            }
        }
        String content = html_content.replaceAll("([A-Za-z0-9]+)\"([A-Za-z0-9]+)", "$1$2");
        JSONObject vods_list_data = JSONObject.parseObject(content);
        vg.setName(nav_name);
        ArrayList<VideoInfo> videoInfos = new ArrayList<>();
        JSONArray vods_list = vods_list_data.getJSONArray("list");
        for (int i = 0; i < vods_list.size(); i++) {
            JSONObject vod_data = vods_list.getJSONObject(i);
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setUrl(this.getServerUrl() + "?ac=" + this.ac + "&ids=" + vod_data.getString("vod_id"));
            if(nav_name.equals("searc") && !vod_data.containsKey("vod_pic")){
                VideoGroup tvg = parse(videoInfo.getUrl(), "detail", -1);
                if(!Utils.isEmpty(tvg.getItems())){
                    videoInfo = tvg.getItems().get(0);
                }
            } else {
                videoInfo.setImage(vod_data.getString("vod_pic"));
                if(!vod_data.containsKey("vod_version") || Utils.isEmpty(vod_data.getString("vod_version"))){
                    videoInfo.setTag(vod_data.getString("vod_remarks"));
                } else {
                    videoInfo.setTag(vod_data.getString("vod_version") + "-"+vod_data.getString("vod_remarks"));
                }
                if(videoInfo.getTag() == null && vod_data.containsKey("vod_hits_month")){
                    videoInfo.setTag(vod_data.getString("vod_hits_month"));
                }
                if(videoInfo.getTag() == null && vod_data.containsKey("vod_time")){
                    videoInfo.setTag(vod_data.getString("vod_time"));
                }
                videoInfo.setName(vod_data.getString("vod_name"));
                if(vod_data.containsKey("vod_content")){
                    videoInfo.setDescription(Jsoup.parse(vod_data.getString("vod_content")).text().trim());//vod_blurb vod_content
                } else {
                    videoInfo.setDescription(vod_data.getString("vod_remarks").trim());//vod_blurb vod_content
                }
            }

            if (limit < 0 || videoInfos.size() < limit) {
                videoInfos.add(videoInfo);
            }

            Constants.HotKeywordDBUtils.call(videoInfo.getName());
        }
        if(vods_list_data.containsKey("pagecount")){
            vg.setPagecount(vods_list_data.getInteger("pagecount"));
        }
        vg.setItems(videoInfos);
        return vg;
    }

    @Override
    public ParseResult parse(String episode_tag, String video_name, String video_alias_name, String resource_tag, String resource_url, Callback callback) throws Exception {
        VideoGroup vg = new VideoGroup();
        vg.setName(video_name+"-parse");
        vg.setItems(((VideoGroup) Constants.home_datas.get(new Random().nextInt(Constants.home_datas.size()))).getItems());
        Constants.ParseCallback.call(vg);
        ParseResult pr = new ParseResult(resource_url, this.getServerUrl(), null);
        //清空广告
        AdsUtils.removeAds(config, pr);
        return pr;
    }

    private LinkedHashMap<String, LinkedHashMap<String, String>> getTags(String html_content) {
        LinkedHashMap<String, LinkedHashMap<String, String>> all_tags = new LinkedHashMap<>();
        JSONArray vods_clazzs = this.getVodClass();

        String type_id = Utils.getString(html_content.trim(), "\"type_id\":[\"']*(\\d+)[\"']*,");
        if(Utils.isEmpty(type_id)){
            return all_tags;
        }
        int type_pid = Integer.parseInt(type_id);
        for(int i=0; i<vods_clazzs.size();i++){
            JSONObject vod_clazz = vods_clazzs.getJSONObject(i);
            if(vod_clazz.getInteger("type_pid") != 0 && vod_clazz.getString("type_id").equals(type_id) ){
                type_pid = vod_clazz.getInteger("type_pid");
                break;
            }
        }

        for(int i=0; i<vods_clazzs.size();i++){
            JSONObject vod_clazz = vods_clazzs.getJSONObject(i);
            if(vod_clazz.getInteger("type_pid") == 0 && vod_clazz.getInteger("type_id") == type_pid){
                LinkedHashMap<String, String> parent_tags = new LinkedHashMap<>();
                for(int j=0; j<vods_clazzs.size();j++){
                    JSONObject sub_vod_clazz = vods_clazzs.getJSONObject(j);
                    if(sub_vod_clazz.getInteger("type_pid") == vod_clazz.getInteger("type_id")){
                        parent_tags.put(sub_vod_clazz.getString("type_name") , this.getServerUrl() + "?ac=" + this.ac + "&t="+sub_vod_clazz.getInteger("type_id"));
                    }
                }
                parent_tags.put("其它" + vod_clazz.getString("type_name").replace("片", ""), this.getServerUrl() + "?ac=" + this.ac + "&t=" + vod_clazz.getString("type_id"));
                all_tags.put(vod_clazz.getString("type_name").replace("片", "") + "分类", parent_tags);
            }
        }

        Set<String> all_tags_keys = all_tags.keySet();
        for(String all_tags_key : all_tags_keys){
            LinkedHashMap<String, String> tags = all_tags.get(all_tags_key);
            Set<String> tags_keys = tags.keySet();
            for(String tags_key : tags_keys){
                if(tags.get(tags_key).contains("&t="+type_id)){
                    tags.put(tags_key, tags.get(tags_key)+"&&?active");
                }
            }
        }
        return all_tags;
    }

    @Override
    public String composeTagUrl(String url, String tag_url, String tag_key) {
        String pg = Utils.getString(url, "&pg=(\\d*)");
        if(Utils.isEmpty(pg)){
            return tag_url;
        }
        if(tag_url.contains("&pg=")){
            return tag_url.replaceAll("&pg=\\d*", "&pg="+pg);
        }
        return tag_url + "&pg="+pg;
    }

    @Override
    public LinkedHashMap<String, String> findResourceList(String vod_url, VideoRelateInfo vri)  throws Exception {
        HttpResult result = null;
        while (result == null || !result.isSign()) {
            result = Constants.ParseHelper.getHsu().doGetBody(vod_url, null, Constants.ParseHelper.getHeaders(), "UTF-8");
            if(result.indexOf(false,"NCTOKENSTR","NCAPPKEY")){
                result = null;
                Thread.sleep(1000);
            }
        }
        if(result.indexOf(false, "伦理片", "限制级")) {
            throw new StopException("Warning!限制级资源，禁止播放！");
        }
        LinkedHashMap<String, String> resourcelist = new LinkedHashMap<>();
        try {
            JSONObject vods_list_data = JSONObject.parseObject(result.getResult().trim());
            JSONObject vod_data = vods_list_data.getJSONArray("list").getJSONObject(0);
            vri.setName(vod_data.getString("vod_name"));
            vri.setRelease_time(vod_data.getString("vod_pubdate"));
            vri.setRegion(vod_data.getString("vod_area"));
            vri.setActor(vod_data.getString("vod_actor") + "("+vod_data.getString("vod_director")+")");
            vri.setLast_updatetime(vod_data.getString("vod_time"));//vod_time_add
            vri.setLength("");
            vri.setDetail(Jsoup.parse(vod_data.getString("vod_content")).text().trim());
            vri.setCategory(vod_data.getString("type_name"));
            vri.setIs_movie(isMovie(vod_data.getInteger("type_id")));
            vri.setLength(vod_data.getString("vod_duration"));

            //解析剧集 提取出m3u8数据
            String vod_play_note = vod_data.getString("vod_play_note");
            String[] vod_play_froms = null;
            String[] vod_play_urls = null;
            if(Utils.isEmpty(vod_play_note)){
                vod_play_urls = new String[]{vod_data.getString("vod_play_url")};
                vod_play_froms = new String[]{vod_data.getString("vod_play_from")};
            } else {
                if(vod_play_note.contains("$$$")){
                    vod_play_note = "$$$";
                }
                vod_play_note = vod_play_note.replace("$", "\\$");
                vod_play_urls = vod_data.getString("vod_play_url").split(vod_play_note);
                vod_play_froms = vod_data.getString("vod_play_from").split(vod_play_note);
            }

            for(int i = 0; i < vod_play_urls.length; i++){
                String vod_play_url = vod_play_urls[i];
                if(vod_play_url.contains(".m3u8")){
                    if (vri.isIs_movie()) {
                        for(String play_url : vod_play_url.split("#")){
                            resourcelist.put(vod_play_froms[i] + "[" + play_url.split("\\$")[0] + "]", play_url.split("\\$")[1]);
                        }
                    } else {
                        resourcelist.put(vod_play_froms[i], vod_play_url);
                    }
                }
            }
        } catch (Exception e){
        }

        if (Constants.Limit_Tags.contains(vri.getCategory())) {
            throw new StopException("当前影视资源被软件识别为敏感资源,无法正常播放！");
        }
        return resourcelist;
    }

    @Override
    public VideoGroup doSearch(String keyword) {
        if (Utils.isEmpty(keyword)) {
            return new VideoGroup();
        }
        HttpResult result = null;
        while (result == null || !result.isSign()) {
            try {
                result = this.hsu.doGetBody(this.getServerUrl() + "?ac=" + (this.config.containsKey("sp")?"list":this.ac) + "&wd="+URLEncoder.encode(keyword, "UTF-8"), null, null, "UTF-8");
                if(result == null || !result.isSign()){
                    try {
                        Thread.sleep(3000);
                    } catch(Exception ex){}
                }
            } catch (Exception e) {}
        }
        return this.parse(result.getResult(), "searc", -1);
    }

    @Override
    public String getPageUrl(String page_url, int page){
        if(Thread.currentThread().getStackTrace()[2].getClassName().contains("TrainButtonAdapter") ||
           Thread.currentThread().getStackTrace()[3].getClassName().contains("TrainButtonAdapter")){
            page_url = page_url.replaceAll("([&\\?]*)t=\\d*", "$1");
        }
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

    private boolean isMovie(int type_id){
        if(dy_type_id == type_id) return true;
        JSONArray vod_clazzs = this.getVodClass();
        for(int i=0; i<vod_clazzs.size();i++){
            JSONObject vod_clazz = vod_clazzs.getJSONObject(i);
            if(vod_clazz.getInteger("type_pid") == dy_type_id){
                if(vod_clazz.getInteger("type_id") == type_id){
                    return true;
                }
            }
        }
        return false;
    }

    private static JSONArray VOD_CLAZZS = null;
    private int dy_type_id = 0;
    private JSONArray getVodClass(){
        if(MacCmsParseHelper.VOD_CLAZZS != null){
            return MacCmsParseHelper.VOD_CLAZZS;
        }
        HttpResult result = null;
        while (result == null || !result.isSign()) {
            result = this.hsu.doGetBody(this.getServerUrl()+"?ac=list", null, headers, "UTF-8");
            if(result == null || !result.isSign()){
                try {
                    Thread.sleep(5000);
                } catch(Exception ex){}
            }
        }

        MacCmsParseHelper.VOD_CLAZZS = JSONObject.parseObject(result.getResult()).getJSONArray("class");
        if(!MacCmsParseHelper.VOD_CLAZZS.toString().contains("type_pid") || this.config.containsKey("pclass")){
            if(this.config.containsKey("pclass")){
                if(this.config.getJSONArray("pclass").size() == 1){
                    for(int i=0;i<MacCmsParseHelper.VOD_CLAZZS.size();i++){
                        MacCmsParseHelper.VOD_CLAZZS.getJSONObject(i).put("type_pid", 1);
                    }
                }
                for(int i=0;i<this.config.getJSONArray("pclass").size();i++){
                    MacCmsParseHelper.VOD_CLAZZS.add(JSONObject.parseObject("{\"type_name\":\""+this.config.getJSONArray("pclass").getString(i)+"\",\"type_id\":"+(1+i)+",\"type_pid\":0}"));
                }
            } else {
                boolean flag = false;
                for(int i=0;i<MacCmsParseHelper.VOD_CLAZZS.size();i++){
                    JSONObject vod_clazz = MacCmsParseHelper.VOD_CLAZZS.getJSONObject(i);
                    if(vod_clazz.getString("type_name").equals("电影") || vod_clazz.getString("type_name").equals("电影片")){
                        flag = true;
                    } else if(vod_clazz.getString("type_name").equals("电视剧") || vod_clazz.getString("type_name").equals("连续剧")){
                        flag = true;
                    } else if(vod_clazz.getString("type_name").equals("综艺") || vod_clazz.getString("type_name").equals("综艺片")){
                        flag = true;
                    } else if(vod_clazz.getString("type_name").equals("动漫") || vod_clazz.getString("type_name").equals("动漫片")){
                        flag = true;
                    }
                }
                if(!flag){
                    MacCmsParseHelper.VOD_CLAZZS.add(JSONObject.parseObject("{\"type_name\":\"电影\",\"type_id\":1,\"type_pid\":0}"));
                    MacCmsParseHelper.VOD_CLAZZS.add(JSONObject.parseObject("{\"type_name\":\"连续剧\",\"type_id\":2,\"type_pid\":0}"));
                    MacCmsParseHelper.VOD_CLAZZS.add(JSONObject.parseObject("{\"type_name\":\"动漫\",\"type_id\":3,\"type_pid\":0}"));
                    MacCmsParseHelper.VOD_CLAZZS.add(JSONObject.parseObject("{\"type_name\":\"综艺\",\"type_id\":4,\"type_pid\":0}"));
                }
            }

            for(int i=0;i<MacCmsParseHelper.VOD_CLAZZS.size();i++){
                JSONObject vod_clazz = MacCmsParseHelper.VOD_CLAZZS.getJSONObject(i);
                if(vod_clazz.getString("type_name").equals("电影") || vod_clazz.getString("type_name").equals("电影片")){
                    vod_clazz.put("type_pid", "0");
                    setCatalog(MacCmsParseHelper.VOD_CLAZZS, vod_clazz.getInteger("type_id"), "片");
                } else if(vod_clazz.getString("type_name").equals("电视剧") || vod_clazz.getString("type_name").equals("连续剧")){
                    vod_clazz.put("type_pid", "0");
                    setCatalog(MacCmsParseHelper.VOD_CLAZZS, vod_clazz.getInteger("type_id"), "剧");
                } else if(vod_clazz.getString("type_name").equals("综艺") || vod_clazz.getString("type_name").equals("综艺片")){
                    vod_clazz.put("type_pid", "0");
                    setCatalog(MacCmsParseHelper.VOD_CLAZZS, vod_clazz.getInteger("type_id"), "综艺");
                } else if(vod_clazz.getString("type_name").equals("动漫") || vod_clazz.getString("type_name").equals("动漫片")){
                    vod_clazz.put("type_pid", "0");
                    setCatalog(MacCmsParseHelper.VOD_CLAZZS, vod_clazz.getInteger("type_id"), "动漫");
                }
            }
            for(int i=0;i<MacCmsParseHelper.VOD_CLAZZS.size();i++){
                JSONObject vod_clazz = MacCmsParseHelper.VOD_CLAZZS.getJSONObject(i);
                if(!vod_clazz.containsKey("type_pid")){
                    vod_clazz.put("type_pid", "0");
                }
            }
        }
        return MacCmsParseHelper.VOD_CLAZZS;
    }

    private static void setCatalog(JSONArray vod_clazzs, int type_pid, String endWith){
        for(int i=0;i<vod_clazzs.size();i++){
            JSONObject vod_clazz = vod_clazzs.getJSONObject(i);
            if(!vod_clazz.containsKey("type_pid") && vod_clazz.getString("type_name").endsWith(endWith)){
                vod_clazz.put("type_pid", type_pid);
            }
        }
    }
}
