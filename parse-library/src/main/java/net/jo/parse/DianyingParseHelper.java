package net.jo.parse;

import com.alibaba.fastjson.JSONObject;
import net.jo.common.Constants;
import net.jo.common.StopException;
import net.jo.common.base64.Base64;
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

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DianyingParseHelper implements IParseHelper {
    private JSONObject config;
    private HttpSimpleUtils hsu;
    private NavItem nav_template;
    private Map<String, String> headers = new HashMap();
    private List<String> all_serverUrls = new ArrayList<String>();
    private String serverUrl = "https://dianying.xianshe.ng/";

    public String getServerUrl(){
        return this.serverUrl;
    }

    public HttpSimpleUtils getHsu() {
        return hsu;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public DianyingParseHelper(JSONObject config, NavItem nav_template){
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
    public LinkedHashMap<String, String> parseHomePage(ArrayList<VideoGroup> vgs) {
        int rollCount = 0;
        HttpResult result = null;
        while (result == null || !result.isSign()) {
            this.serverUrl = this.all_serverUrls.get(rollCount % this.all_serverUrls.size());
            rollCount++;
            result = this.hsu.doGetBody(this.getServerUrl(), null, headers, "UTF-8");
            if(result == null || !result.isSign()){
                try {
                    Thread.sleep(2000);
                } catch(Exception ex){}
            }
        }
        Document doc = Jsoup.parse(result.getResult());

        Elements modules = doc.getElementById("index-main").getElementsByClass("module");
        for(int i=0;i<modules.size();i++){
            Elements module_items = modules.get(i).getElementsByClass("module-item");
            ArrayList<VideoInfo> videoInfos = new ArrayList<>();
            for (int j = 0; j < module_items.size(); j++) {
                Element module_item = module_items.get(j);
                String href = module_item.getElementsByTag("a").get(0).attr("href");
                String imageUrl = module_item.getElementsByTag("img").get(0).attr("data-src");
                if(Utils.isEmpty(imageUrl)){
                    imageUrl = module_item.getElementsByTag("a").get(0).attr("src");
                }
                VideoInfo videoInfo = new VideoInfo();
                try {
                    videoInfo.setImage(Utils.resolveURI(this.getServerUrl(), imageUrl));
                } catch(Exception e){}
                videoInfo.setUrl(Utils.resolveURI(this.getServerUrl(), href));
                videoInfo.setTag(module_item.getElementsByClass("module-item-text").text());
                try {
                    videoInfo.setName(module_item.getElementsByClass("module-item-titlebox").text().trim());
                    videoInfo.setDescription(module_item.getElementsByClass("module-item-titlebox").text().trim());
                } catch (Exception ex){
                    videoInfo.setName(module_item.getElementsByTag("a").get(0).attr("title"));
                    videoInfo.setDescription("");
                }
                videoInfos.add(videoInfo);
                Constants.HotKeywordDBUtils.call(videoInfo.getName());
            }

            String module_title = modules.get(i).getElementsByClass("module-title").get(0).text().trim();
            if(modules.get(i).html().contains("module-main")){
                module_title = modules.get(i).getElementsByClass("module-main").get(0).getElementsByClass("module-title").get(0).text().trim();
            }
            if("正在热播,电影,电视剧,动漫,综艺".contains(module_title)){
                VideoGroup vg = new VideoGroup();
                vg.setItems(videoInfos);
                vg.setName(module_title);
                if("电影,电视剧,动漫,综艺".contains(module_title)){
                    String video_catalog = "dianying";
                    if(module_title.contains("电视剧")){
                        video_catalog = "dianshiju";
                    } else if(module_title.contains("动漫")){
                        video_catalog = "dongman";
                    } else if(module_title.contains("综艺")){
                        video_catalog = "zongyi";
                    }
                    ArrayList<NavItem> navItems = new ArrayList<>();
                    navItems.add(NavItem.newByTemplate(nav_template,"全部剧情", this.getServerUrl() + "pianku-"+video_catalog+"--------1---/"));
                    navItems.add(NavItem.newByTemplate(nav_template,"剧情", this.getServerUrl() + "pianku-"+video_catalog+"---剧情-----1---/"));
                    navItems.add(NavItem.newByTemplate(nav_template,"喜剧", this.getServerUrl() + "pianku-"+video_catalog+"---喜剧-----1---/"));
                    navItems.add(NavItem.newByTemplate(nav_template,"动作", this.getServerUrl() + "pianku-"+video_catalog+"---动作-----1---/"));
                    navItems.add(NavItem.newByTemplate(nav_template,"爱情", this.getServerUrl() + "pianku-"+video_catalog+"---爱情-----1---/"));
                    navItems.add(NavItem.newByTemplate(nav_template,"科幻", this.getServerUrl() + "pianku-"+video_catalog+"---科幻-----1---/"));
                    navItems.add(NavItem.newByTemplate(nav_template,"都市", this.getServerUrl() + "pianku-"+video_catalog+"---都市-----1---/"));
                    navItems.add(NavItem.newByTemplate(nav_template,"其他", this.getServerUrl() + "pianku-"+video_catalog+"---其他-----1---/"));
                    vg.setNavs(navItems);
                }
                vgs.add(vg);
            }
        }
        vgs.get(0).setSlide_items(new ArrayList<VideoInfo>());

        this.trainHotkeys(doc);

        LinkedHashMap<String, String> tabs = new LinkedHashMap<>();
        tabs.put("电影",   this.getServerUrl() + "pianku-dianying--------1---/");//https://www.youku.com/category/data?c=97&type=show&p=2
        tabs.put("电视剧", this.getServerUrl() + "pianku-dianshiju--------1---/");
        tabs.put("动漫",   this.getServerUrl() + "pianku-dongman--------1---/");
        tabs.put("综艺",   this.getServerUrl() + "pianku-zongyi--------1---/");
        return tabs;
    }

    @Override
    public VideoGroup parse(String html_content, String nav_name, int limit) {
        VideoGroup vg = new VideoGroup();
        //如果html_content是https://开头说明只是一个链接，需要动态获取
        if(html_content.toLowerCase().startsWith("https://") || html_content.toLowerCase().startsWith("http://")) {
            HttpResult result = null;
            while (result == null || !result.isSign()) {
                result = this.hsu.doGetBody(html_content, this.getServerUrl(), headers, "UTF-8");
                if (result == null || !result.isSign()) {
                    try {
                        Thread.sleep(2000);
                    } catch (Exception ex) {
                    }
                }
            }
            html_content = result.getResult().trim();
            result = null;
            vg.setAll_tags(getTags(html_content));
            if (limit > -1) {
                String tag = ((String[]) vg.getAll_tags().keySet().toArray(new String[0]))[0];
                if (vg.getAll_tags().containsKey("所有剧情")) {
                    tag = "所有剧情";
                } else if (vg.getAll_tags().containsKey("所有类型")) {
                    tag = "所有类型";
                } else if (vg.getAll_tags().containsKey("所有年代")) {
                    tag = "所有年代";
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
        vg.setName(nav_name);
        ArrayList<VideoInfo> videoInfos = new ArrayList<>();

        Elements hot_module_items = Jsoup.parse(html_content).getElementsByClass("module-item");
        for (int i = 0; i < hot_module_items.size(); i++) {
            Element hot_module_item = hot_module_items.get(i);
            String href = hot_module_item.getElementsByTag("a").get(0).attr("href");
            String imageUrl = hot_module_item.getElementsByTag("img").get(0).attr("data-src");
            if(Utils.isEmpty(imageUrl)){
                imageUrl = hot_module_item.getElementsByTag("a").get(0).attr("src");
            }
            VideoInfo videoInfo = new VideoInfo();
            try {
                videoInfo.setImage(Utils.resolveURI(this.getServerUrl(), imageUrl));
            } catch(Exception e){}
            videoInfo.setUrl(Utils.resolveURI(this.getServerUrl(), href));
            videoInfo.setTag(hot_module_item.getElementsByClass("module-item-text").text());
            try {
                videoInfo.setName(hot_module_item.getElementsByClass("module-item-titlebox").text().trim());
                videoInfo.setDescription(hot_module_item.getElementsByClass("module-item-titlebox").text().trim());
            } catch (Exception ex){
                videoInfo.setName(hot_module_item.getElementsByTag("a").get(0).attr("title"));
                videoInfo.setDescription("");
            }
            if (limit < 0 || videoInfos.size() < limit) {
                videoInfos.add(videoInfo);
            }
            Constants.HotKeywordDBUtils.call(videoInfo.getName());
        }
        try {
            int total_page = Integer.parseInt(Utils.getString(html_content, "-(\\d+)---\\.html\"\\>尾页"));
            vg.setPagecount(total_page);
        } catch (Exception e3){
            vg.setPagecount(99999);
        }
        vg.setItems(videoInfos);
        return vg;
    }

    @Override
    public ParseResult parse(String episode_tag, String video_name, String video_alias_name, String resource_tag, String resource_url, Callback callback) throws Exception {
        callback.call(30, resource_url);
        HttpResult result = null;
        while (result == null || !result.isSign()) {
            result = this.hsu.doGetBody(resource_url, resource_url, this.getHeaders(), "UTF-8");
            if(result.indexOf(true,"title>检测中","location.href")){
                result = this.hsu.doGetBody(this.getLocation(result), resource_url, this.getHeaders(), "UTF-8");
            }
        }
        if (result.indexOf(false, "由于版权原因，影片已屏蔽")) {
            throw new StopException("由于版权原因，影片已屏蔽");
        }

        VideoGroup vg = this.parse(result.getResult(), "guess_list", 10);
        Constants.ParseCallback.call(vg);
        try {
            Element player_box_main = Jsoup.parse(result.getResult()).getElementsByClass("player-box-main").get(0);
            JSONObject player_data = JSONObject.parseObject(Utils.getString(player_box_main.html(), "var\\s*player_\\w+\\s*=\\s*(.*?)</script"));
            String player_url = player_data.getString("url");
            if(player_data.getInteger("encrypt") == 2){
                player_url = URLDecoder.decode(Base64.decode(player_url),"UTF-8");
            } else if(player_data.getInteger("encrypt") == 1){
                player_url = URLDecoder.decode(player_url,"UTF-8");
            }
            if (!Utils.isEmpty(player_url)) {
                if(player_data.getString("from").contains("m3u8") || player_url.endsWith(".m3u8") || player_url.endsWith(".mp4")){
                    if(player_url.contains(".m3u8&")){
                        player_url = player_url.substring(0, player_url.indexOf(".m3u8&") + 5);
                    } else if(player_url.contains(".mp4&")){
                        player_url = player_url.substring(0, player_url.indexOf(".mp4&") + 4);
                    }
                    ParseResult pr = new ParseResult(URLDecoder.decode(player_url,"UTF-8"), result.getFinal_URL(),null);
                    //清空广告
                    AdsUtils.removeAds(config, pr);
                    return pr;
                } else {
                    callback.call(40, "/jx/?url="+player_url);
                    return VIPParseHelper.parse(episode_tag, video_name, video_alias_name,"777解析", player_url, callback);
                }
            } else {
                throw new StopException("视频源解析失败,赶紧联系JO(有时重启软件也有用)...");
            }
        } catch (Exception e) {
            throw new StopException("视频源解析失败,赶紧联系JO(有时重启软件也有用)...");
        }
    }

    private LinkedHashMap<String, LinkedHashMap<String, String>> getTags(String html_content) {
        LinkedHashMap<String, LinkedHashMap<String, String>> all_tags = new LinkedHashMap<>();
        Elements library_boxs = Jsoup.parse(html_content).getElementsByClass("library-box");
        for (int i = 1; i < library_boxs.size()-1; i++) {
            Elements library_items = library_boxs.get(i).getElementsByClass("library-item");
            String tag_name = library_items.get(0).text().trim();
            LinkedHashMap<String, String> tags = new LinkedHashMap<>();
            for (int j = 0; j < library_items.size(); j++) {
                Element library_item = library_items.get(j);
                if(library_item.text().contains("0-9")){
                    continue;
                }
                String resolveURI = Utils.resolveURI(this.getServerUrl(), library_item.attr("href"));
                if (library_item.text().equals("全部") || !library_item.className().contains("selected")) {
                    tags.put(library_item.text().equals("全部") ? tag_name : library_item.text(), resolveURI);
                } else {
                    tags.put(library_item.text().equals("全部") ? tag_name : library_item.text(), resolveURI + "?active");
                }
            }
            all_tags.put(tag_name, tags);
        }
        return all_tags;
    }

    private int trainHotkeys(Element element) {
        int total = 0;
        Elements as = element.getElementsByTag("a");
        for (int i = 0; i < as.size(); i++) {
            try {
                Element h3 = as.get(i).getElementsByTag("h3").get(0);
                if (!Utils.isEmpty(h3.ownText())) {
                    total++;
                    String ownText = h3.ownText();
                    Constants.HotKeywordDBUtils.call(ownText);
                }
            } catch (Exception e){}
        }
        return total;
    }

    @Override
    public String composeTagUrl(String url, String tag_url, String tag_key) {
        String[] catalogs_params = url.split("/pianku-")[1].split("-", -1);
        String[] catalogs_fill = tag_url.split("/pianku-")[1].split("-", -1);

        if (tag_key.contains("类型")) {
            catalogs_params[0] = catalogs_fill[0];
        } else if (tag_key.contains("地区")) {
            catalogs_params[1] = catalogs_fill[1];
        } else if (tag_key.contains("排序")) {
            catalogs_params[2] = catalogs_fill[2];
        } else if (tag_key.contains("剧情")) {
            catalogs_params[3] = catalogs_fill[3];
        } else if (tag_key.contains("语言")) {
            catalogs_params[4] = catalogs_fill[4];
        } else if (tag_key.contains("字母")) {
            catalogs_params[5] = catalogs_fill[5];
        } else if (tag_key.contains("年份") || tag_key.contains("时间")) {
            catalogs_params[11] = catalogs_fill[11];
        }
        StringBuffer new_url = new StringBuffer(url.split("/pianku-")[0]);
        new_url.append("/pianku-");
        for (String catalog : catalogs_params) {
            new_url.append(catalog);
            if(catalog.endsWith("/") || catalog.endsWith(".html")){
                continue;
            }
            new_url.append("-");
        }
        return new_url.toString();
    }

    @Override
    public VideoGroup doSearch(String keyword) {
        VideoGroup vg = new VideoGroup();
        if (Utils.isEmpty(keyword)) {
            return vg;
        }
        HttpResult result = null;
        while (result == null || !result.isSign()) {
            try {
                result = this.hsu.doGetBody("https://soupian.one/movie/" + URLEncoder.encode(keyword, "UTF-8"), this.getServerUrl(), null, "UTF-8");
            } catch (Exception e) {}
        }
        vg.setName("search");
        ArrayList<VideoInfo> arrayList = new ArrayList<>();
        Elements searchList = Jsoup.parse(result.getResult()).getElementsByClass("list-row-info");
        if (!Utils.isEmpty(searchList)) {
            for (int i = 0; i < searchList.size(); i++) {
                Element element = searchList.get(i);
                if(element.getElementsByClass("list-row-siteinfo").html().contains("电影先生")){
                    VideoInfo videoInfo = new VideoInfo();
                    Element e_title = element.getElementsByClass("list-row-title").get(0).getElementsByTag("a").get(0);
                    videoInfo.setName(e_title.text());
                    videoInfo.setUrl(Utils.resolveURI(this.getServerUrl(), URI.create(e_title.attr("href")).getPath()));
                    videoInfo.setImage(Utils.resolveURI("https://soupian.one/", element.getElementsByTag("img").attr("data-url")));
                    videoInfo.setTag(element.getElementsByClass("list-row-title").get(0).getElementsByTag("p").get(0).text());
                    videoInfo.setDescription("");
                    arrayList.add(videoInfo);
                    Constants.HotKeywordDBUtils.call(videoInfo.getName());
                }
            }
        }
        vg.setItems(arrayList);
        return vg;
    }

    @Override
    public LinkedHashMap<String, String> findResourceList(String vod_url, VideoRelateInfo vri) throws Exception {
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

        Document doc = Jsoup.parse(result.getResult());
        try {
            String video_name = doc.getElementsByClass("movie-title").text();
            if (Utils.isEmpty(video_name)) {
                video_name = doc.getElementsByClass("player_title").text();
                if(Utils.isEmpty(video_name)){
                    video_name = doc.getElementsByTag("page-title").text();
                    if(Utils.isEmpty(video_name)){
                        video_name = doc.getElementsByTag("h1").text();
                    }
                }
            }
            vri.setName(video_name);

            vri.setRelease_time(doc.getElementsByClass("tag-link").get(1).text());
            vri.setCategory(doc.getElementsByClass("tag-link").get(2).text());
            vri.setRegion(doc.getElementsByClass("tag-link").get(3).text());

            Elements video_info_items = doc.getElementsByClass("video-info-items");
            for (int i=0;i < video_info_items.size();i++) {
                Element video_info_item = video_info_items.get(i);
                String itemtitle = video_info_item.getElementsByClass("video-info-itemtitle").text().trim();
                String item_value = video_info_item.getElementsByClass("video-info-item").text().trim();
                if (itemtitle.contains("导演")) {
                    vri.setActor(item_value.trim() + vri.getActor());
                } else if (itemtitle.contains("主演")) {
                    vri.setActor( vri.getActor() + "(" + item_value.trim() + ")");
                } else if (itemtitle.contains("上映")) {
                    vri.setLast_updatetime(item_value.trim());
                } else if (itemtitle.contains("片长")) {
                    vri.setLength(item_value.trim());
                } else if (itemtitle.contains("剧情")) {
                    vri.setDetail(item_value.trim());
                }
                continue;
            }

            vri.setIs_movie(doc.getElementsByClass("tag-link").text().contains("电影"));
        } catch (Exception e){}

        if (Constants.Limit_Tags.contains(vri.getCategory())) {
            throw new StopException("当前影视资源被软件识别为敏感资源,无法正常播放！");
        }

        Elements module_tabs = doc.getElementsByClass("module-tab-item");//module-tab-item
        Elements module_playlists = doc.getElementsByClass("module-player-list");//module-tab-item

        LinkedHashMap<String, String> resourcelist = new LinkedHashMap<>();

        for (int i = 0; i < module_tabs.size(); i++) {
            String module_tab_name = module_tabs.get(i).child(0).text();
            if(module_tabs.get(i).hasClass("tab-item")){
                Elements playlists = module_playlists.get(i).getElementsByClass("scroll-content").get(0).getElementsByTag("a");
                if (vri.isIs_movie()) {
                    for (int j = 0; j < playlists.size(); j++) {
                        Element btn = playlists.get(j);
                        resourcelist.put(module_tab_name + "[" + btn.text().trim() + "]", btn.attr("href"));
                    }
                } else {
                    StringBuffer sb = new StringBuffer();
                    for (int j = 0; j < playlists.size(); j++) {
                        Element btn = playlists.get(j);
                        if (sb.length() > 0) {
                            sb.append("#");
                        }
                        sb.append(btn.text());
                        sb.append("$");
                        sb.append(btn.attr("href"));
                    }
                    resourcelist.put(module_tab_name, sb.toString());
                }
            }
        }
        return resourcelist;
    }

    @Override
    public String getPageUrl(String page_url, int page){
        if (page_url.contains("show/")) {
            page_url = page_url.replaceAll("-\\d*---.html", "-"+page+"---.html");
        } else {
            if(page_url.endsWith(".html")){
                page_url = page_url.replaceAll("-\\d*---.html", "-"+page+"---.html");
            } else {
                page_url = page_url.replaceAll("-\\d*---/", "-"+page+"---/");
            }
        }
        return page_url;
    }

    private String getLocation(HttpResult result){
        String location_href = Utils.getString(result.getResult(),"location.href\\s*=\\s*\"(.*?)\";");
        location_href = Utils.resolveURI(result.getFinal_URL(),location_href);
        return location_href;
    }
}
