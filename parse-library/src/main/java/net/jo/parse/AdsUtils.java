package net.jo.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.jo.common.Constants;
import net.jo.common.HawkConfig;
import net.jo.common.HtmlServiceUtils;
import net.jo.common.Utils;
import net.jo.http.HttpResult;
import net.jo.model.ParseResult;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class AdsUtils {
    public static void removeAds(JSONObject config, ParseResult pr){
        if(HawkConfig.isAutoRemoveAd() && pr.getPlay_url().contains(".m3u8") && config.containsKey("ads")){
            AdsHtmlHandler handler = new AdsHtmlHandler();
            HtmlServiceUtils.start(handler);

            if(config.getString("ads").toLowerCase().startsWith("http")){
                HttpResult result = Constants.ParseHelper.getHsu().doGetBody(config.getString("ads"), config.getString("ads"), null, "UTF-8");
                if(result.eqURL("youdao.com")) {
                    JSONObject parseObject = JSONObject.parseObject(result.getResult());
                    parseObject.put("content", (Object) Jsoup.parse(parseObject.getString("content")).text());
                    result.setResult(parseObject.getString("content").trim());
                }
                try {
                    config.put("ads", JSONArray.parse(result.getResult()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            HttpResult result = Constants.ParseHelper.getHsu().doGetBody(pr.getPlay_url(), pr.getReferer(), pr.getHeaders(),  "UTF-8");
            if (result.getResult().trim().startsWith("#EXTM3U")) {
                String[] lines = null;
                if (result.getResult().contains("\r\n")) {
                    lines = result.getResult().split("\r\n", 10);
                } else {
                    lines = result.getResult().split("\n", 10);
                }
                String forwardurl = "";
                boolean dealedFirst = false;
                for (String line : lines) {
                    if (!"".equals(line) && line.charAt(0) != '#') {
                        if (dealedFirst) {
                            //跳转行后还有内容，说明不需要跳转
                            forwardurl = "";
                            break;
                        }
                        if (line.endsWith(".m3u8") || line.contains(".m3u8?")) {
                            if (line.startsWith("http://") || line.startsWith("https://")) {
                                forwardurl = line;
                            } else if (line.charAt(0)=='/' ) {
                                int ifirst = pr.getPlay_url().indexOf('/', 9);//skip https://, http://
                                forwardurl = pr.getPlay_url().substring(0, ifirst) + line;
                            } else {
                                int ilast = pr.getPlay_url().lastIndexOf('/');
                                forwardurl = pr.getPlay_url().substring(0, ilast + 1) + line;
                            }
                        }
                        dealedFirst = true;
                    }
                }
                if(!Utils.isEmpty(forwardurl)) {
                    result = Constants.ParseHelper.getHsu().doGetBody(forwardurl, pr.getReferer(), pr.getHeaders(), "UTF-8");
                    pr.setPlay_url(forwardurl);
                }

                int ilast = pr.getPlay_url().lastIndexOf('/');
                String m3u8Content = removeAds(pr.getPlay_url().substring(0, ilast + 1), result.getResult(), config.getJSONArray("ads"));
                if (m3u8Content != null) {
                    handler.setM3u8Content(m3u8Content);
                    pr.setPlay_url("http://127.0.0.1:"+HtmlServiceUtils.getPort()+"/index.m3u8");
                }
            }
        }
    }

    private static String removeAds(String tsUrlPre, String m3u8_content, JSONArray ads_rules){
        if (!m3u8_content.startsWith("#EXTM3U")) {
            return null;
        }

        //清空广告类ts
        List<String> matched_rules = getMatchedRules(tsUrlPre, ads_rules);
        for(int i = 0; i < matched_rules.size(); i++){
            String regex = matched_rules.get(i);
            m3u8_content = m3u8_content.replaceAll(regex, "");
        }

        //把相对地址url全替换成绝对地址url
        String linesplit = "\n";
        if (m3u8_content.contains("\r\n"))
            linesplit = "\r\n";
        String[] lines = m3u8_content.split(linesplit);

        boolean dealedExtXKey = false;
        for (int i = 0; i < lines.length; ++i) {
            if (!dealedExtXKey && lines[i].startsWith("#EXT-X-KEY")) {
                String keyUrl = Utils.getString(lines[i], "URI=\"(.*?)\"");
                if (keyUrl != null && !keyUrl.startsWith("http://") && !keyUrl.startsWith("https://")) {
                    String newKeyUrl;
                    if (keyUrl.charAt(0) == '/') {
                        int ifirst = tsUrlPre.indexOf('/', 9);//skip https://, http://
                        newKeyUrl = tsUrlPre.substring(0, ifirst) + keyUrl;
                    } else
                        newKeyUrl = tsUrlPre + keyUrl;
                    lines[i] = lines[i].replace("URI=\"" + keyUrl + "\"", "URI=\"" + newKeyUrl + "\"");
                }
                dealedExtXKey = true;
            }
            if (lines[i].length() == 0 || lines[i].charAt(0) == '#') {
                continue;
            }
            if (!lines[i].startsWith("http://") && !lines[i].startsWith("https://")) {
                if (lines[i].charAt(0) == '/') {
                    int ifirst = tsUrlPre.indexOf('/', 9);//skip https://, http://
                    lines[i] = tsUrlPre.substring(0, ifirst) + lines[i];
                } else
                    lines[i] = tsUrlPre + lines[i];
            }
        }
        return   StringUtil.join(lines, linesplit);
    }

    private static List<String> getMatchedRules(String url, JSONArray ads_rules){
        List<String> matched_rules = new ArrayList<>();
        for(int i = 0; i < ads_rules.size(); i++){
            JSONObject ads_rule = ads_rules.getJSONObject(i);
            for(int j = 0; j < ads_rule.getJSONArray("hosts").size(); j++){
                String host = ads_rule.getJSONArray("hosts").getString(j);
                if(url.contains(host)){
                    for(int l = 0; l < ads_rule.getJSONArray("regex").size(); l++){
                        if(!matched_rules.contains(ads_rule.getJSONArray("regex").getString(l))){
                            matched_rules.add(ads_rule.getJSONArray("regex").getString(l));
                        }
                    }
                }
            }
        }
        return matched_rules;
    }
}
