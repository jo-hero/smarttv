package net.jo.parse;

import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.jo.common.aes.AESUtils;
import net.jo.common.AuthCodeUtil;
import net.jo.common.Constants;
import net.jo.common.HexUtils;
import net.jo.common.MD5Util;
import net.jo.common.StopException;
import net.jo.common.WYUtils;
import net.jo.http.HttpResult;
import net.jo.model.ParseResult;

import net.jo.bean.Callback;
import net.jo.common.Utils;
import net.jo.common.XXTEA;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URLEncoder;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class VIPParseHelper {
    private static String video_parse_exception_tips = "解析异常,请联系JO(%s)";
    private static final ExecutorService spThreadPool = Executors.newSingleThreadExecutor();
/*


    public static void main(String[] args)throws Exception{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        String data = "2ME8ETW7MNboiun04Dy6iAtQwCCW2CdjIM4+A3F1oRczEZiAEX6Wx5tdIc1wnOEo4hDK+/Vf5Bi6CLmm3FMPqB9tCt469slyAFwO9Sf7zk8mA44yLkt/E6JXmFLusIpin6U0YCkWw9QHISJxJ/tmOKLWUnDFPQ2eelKG8dbY/8aJKg4kjfWixDDuhws0wtnEy7Mq6STMxt0Bt8V8o32zfybCBObk2/m4B8daN/rsIcI=";
        byte[] en_bytes = org.bouncycastle.util.encoders.Base64.decode(data);
        byte[] xor_keys = new byte[]{-44,-95,51,52,85,-77,52,56,57,48,97,98,99,100,101,102};
        byte[] keys = "abcdef1234567890".getBytes("UTF-8");
        byte[] iv = new byte[]{(byte)229,(byte)147,0,0,96,(byte)133,3,0,0,0,0,0,0,0,0,0};
        en_bytes = AESUtils.decrypt(en_bytes, keys, iv, "AES/CBC/PKCS7Padding");
        for(int i=0;i<xor_keys.length;i++){
            en_bytes[i] ^= xor_keys[i];
        }
        System.out.println(new String(en_bytes,"UTF-8"));
    }
*/

    public static ParseResult parse(final String episode_tag,final String video_name, final String video_alias_name, final String parse_service,final String resource_url,final Callback callback) throws Exception {
        callback.call(30,"/jx/?url="+resource_url);
        HashMap headers = new HashMap();
        headers.put("sec-ch-ua", "\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("upgrade-insecure-requests", "1");
        headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.put("sec-fetch-site", "cross-site");
        headers.put("sec-fetch-mode", "navigate");
        headers.put("sec-fetch-dest", "iframe");
        headers.put("accept-encoding", "gzip, deflate");
        headers.put("accept-language", "zh-CN,zh;q=0.9");

      if(parse_service.startsWith("777解析")){
          //https://jx.777jiexi.com/player/?url=https://v.qq.com/x/cover/mzc00200fph94nw/j0044otmtgt.html
          //https://jx.jsonplayer.com/player/?url=https://v.qq.com/x/cover/mzc00200fph94nw/j0044otmtgt.html
          headers.put("origin","https://jx.777jiexi.com");
          headers.put("accept", "*/*");

          //d4a1333455b334383930616263646566
          byte[] xor_keys = new byte[]{-44,-95,51,52,85,-77,52,56,57,48,97,98,99,100,101,102};
          byte[] keys = "abcdef1234567890".getBytes("UTF-8");
          byte[] iv = new byte[]{(byte)229,(byte)147,0,0,96,(byte)133,3,0,0,0,0,0,0,0,0,0};
          byte[] en_bytes = ("{\"domain\":\"jx.777jiexi.com\",\"url\":\""+resource_url+"\",\"referrer\":\"jx.777jiexi.com\",\"timeout\":"+(System.currentTimeMillis()+2000)+",\"fingerprint\":\"d5036ce6\"}").getBytes("UTF-8");

          //"2ME8ETW7MNboiun04Dy6iAtQwCCW2CdjIM4+A3F1oRczEZiAEX6Wx5tdIc1wnOEoJS/qhIJW2WC6mxEs964CrezC76LudrPbVVG/f5YNi2arPuoxrvnViXYJBsiiYv1p9sofWqHG8AW7aCpXB4lpXqTOZStxi/p7XuiSHsoqiX7iRf/lhKTNlmGc4Pub7JRnvN4Nn3Rktecea5Afyxvu/8S8zZ1zTinAT6dd8lU3GHs="
          for(int i=0;i<xor_keys.length;i++){
              en_bytes[i] ^= xor_keys[i];
          }
          JSONObject params = new JSONObject();
          params.put("params", URLEncoder.encode(new String(org.bouncycastle.util.encoders.Base64.encode(AESUtils.encrypt(en_bytes, keys, iv, "AES/CBC/PKCS7Padding")),"UTF-8"), "UTF-8"));

          callback.call(50,"/jx/?url="+resource_url);
          HttpResult jx_result = null;
          int req_count = 0;
          while(jx_result == null || !jx_result.isSign()) {
              if (req_count++ > 3) break;
              jx_result = Constants.ParseHelper.getHsu().doPostBody_Json("https://110.42.2.247:9090/xplayer/api.php", params.toString(), null, headers, "UTF-8");
          }
          try {
              JSONObject parseObject = JSONObject.parseObject(jx_result.getResult());
              if(parseObject.getString("code").equals("200")){
                  String url = parseObject.getString("url");
                  byte[] url_dec_data = AESUtils.decrypt(org.bouncycastle.util.encoders.Base64.decode(url), keys, iv, "AES/CBC/PKCS7Padding");
                  for(int i=0;i<xor_keys.length;i++){
                      url_dec_data[i] ^= xor_keys[i];
                  }
                  String decode_url = new String(url_dec_data, "UTF-8");
                  callback.call(80,"/jx/?url="+resource_url);
                  Map<String,String> play_headers = new HashMap<String, String>();
                  play_headers.put("Referer", "");
                  return new ParseResult(decode_url, decode_url,play_headers);
              } else {
                  throw new StopException(String.format(video_parse_exception_tips, parseObject.getString("message")));
              }
          } catch (Exception e){
              if(e instanceof StopException)throw e;
              throw new StopException(String.format(video_parse_exception_tips, jx_result.getFinal_URL() + "\r\n网络异常中断，请重试！！\r\n"));
          }
        } else if(parse_service.startsWith("江湖云解析")){
            Map<String,String> jx_headers = new HashMap<String,String>();
            jx_headers.put("user-agent","Mozilla/5.0 (Linux; U; Android 7.1.1; zh-cn; MI MAX 2 Build/NMF26F) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.116 Mobile Safari/537.36 XiaoMi/MiuiBrowser/15.8.6");
            jx_headers.put("cache-control","max-age=0");
            jx_headers.put("upgrade-insecure-requests","1");
            jx_headers.put("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            jx_headers.put("x-miorigin","b");
            jx_headers.put("sec-fetch-site","none");
            jx_headers.put("sec-fetch-mode","navigate");
            jx_headers.put("sec-fetch-user","?1");
            jx_headers.put("sec-fetch-dest","document");
            jx_headers.put("accept-encoding","gzip, deflate, br");
            jx_headers.put("accept-language","zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");

            callback.call(50, "http://api.jhdyw.vip/?url=" + resource_url);
            HttpResult jx_result = null;
            int req_count = 0;
            while(jx_result == null || !jx_result.isSign()) {
                if (req_count++ > 3) break;
                jx_result = Constants.ParseHelper.getHsu().doGetBody("http://api.jhdyw.vip/?url=" + resource_url, null, jx_headers, "UTF-8");
            }
            String iparams = Utils.getString(jx_result.getResult(), "function\\(\\)\\s*\\{\\s*i\\('\\w+',\\s*'(.*?)'\\);");
            if(Utils.isEmpty(iparams)){
                throw new StopException(String.format(video_parse_exception_tips, "解析失败，请重试！"));
            } else {
                Map<String,String> resource_headers = new HashMap<String,String>();
                resource_headers.put("user-agent","Mozilla/5.0 (Linux; U; Android 7.1.1; zh-cn; MI MAX 2 Build/NMF26F) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.116 Mobile Safari/537.36 XiaoMi/MiuiBrowser/15.8.6");
                resource_headers.put("accept","application/json, text/plain, */*");
                resource_headers.put("content-type","application/x-www-form-urlencoded");
                resource_headers.put("origin","https://api.jhdyw.vip");
                resource_headers.put("sec-fetch-site","same-origin");
                resource_headers.put("sec-fetch-mode","cors");
                resource_headers.put("sec-fetch-dest","empty");
                resource_headers.put("accept-encoding","gzip, deflate, br");
                resource_headers.put("accept-language","zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");

                callback.call(60, "http://api.jhdyw.vip/?resource");
                jx_result = null;
                req_count = 0;
                while(jx_result == null || !jx_result.isSign()) {
                    if (req_count++ > 3) break;
                    jx_result = Constants.ParseHelper.getHsu().doPostBody("http://api.jhdyw.vip/?resource", iparams, null, resource_headers, "UTF-8");
                }
                if(Utils.isEmpty(jx_result.getResult().trim())){
                    throw new StopException(String.format(video_parse_exception_tips, "解析失败，请重试！！"));
                } else {
                    String cipher_key = "89ffc596602d677a";
                    String cipher_iv = "d9cd43a3003d480d";
                    byte[] resource_bytes = org.bouncycastle.util.encoders.Base64.decode(jx_result.getResult());
                    String resource_data = new String(AESUtils.decrypt(resource_bytes, cipher_key.getBytes("UTF-8"),cipher_iv.getBytes("UTF-8"), "AES/CBC/PKCS7Padding"));
                    String url = Utils.getString(resource_data, "video\\s*:\\s*\\{\\s*url\\s*:\\s*'(.*?)'\\s*");
                    if(url.contains("api.jhdyw.vip")){
                        throw new StopException(String.format(video_parse_exception_tips, "重定向错误！"));
                    } else {
                        return new ParseResult(url, jx_result.getFinal_URL(), null);
                    }
                }
            }
        } else if(parse_service.startsWith("诺讯解析")){
            //https://www.mtosz.com/m3u8.php?url=https://v.qq.com/x/cover/mzc00200eacw05k/v0036502bcl.html
            Map<String,String> params = new HashMap<String,String>();
            params.put("url", resource_url);
            params.put("wap", "");
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            byte[] vkey =  AESUtils.encrypt(resource_url.getBytes("UTF-8"), "nuoxun@nxflv@com".getBytes("UTF-8"), "AES/ECB/PKCS7Padding");
            params.put("vkey", new String(org.bouncycastle.util.encoders.Base64.encode(vkey)));

            callback.call(60, "https://www.nxflv.com/Api.php?url="+resource_url);
            HttpResult jx_result = null;
            int req_count = 0;
            while(jx_result == null || !jx_result.isSign()) {
                if (req_count++ > 3) break;
                jx_result = Constants.ParseHelper.getHsu().doPostBody("https://www.nxflv.com/Api.php", params, "https://www.nxflv.com/?url="+resource_url, headers, "UTF-8");
            }

            JSONObject parseObject = JSONObject.parseObject(jx_result.getResult());
            if(parseObject.getString("code").equals("200")){
                String decode_url = parseObject.getString("url");
                try {
                    byte[] url_bytes = org.bouncycastle.util.encoders.Base64.decode(parseObject.getString("url").substring(4));
                    decode_url = new String(AESUtils.decrypt(url_bytes, "loveme@nxflv@com".getBytes("UTF-8"), "AES/ECB/PKCS7Padding"));
                } catch (Exception ex){}
                return new ParseResult(Utils.URLDecode(decode_url),jx_result.getFinal_URL(),null);
            } else {
                throw new StopException(String.format(video_parse_exception_tips, parseObject.toString()));
            }
        } else if(parse_service.startsWith("ckmov解析")){
            Map<String,String> jx_headers = new HashMap<String,String>();
            jx_headers.put("user-agent","Mozilla/5.0 (Linux; U; Android 7.1.1; zh-cn; MI MAX 2 Build/NMF26F) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.116 Mobile Safari/537.36 XiaoMi/MiuiBrowser/15.8.6");
            jx_headers.put("cache-control","max-age=0");
            jx_headers.put("upgrade-insecure-requests","1");
            jx_headers.put("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            jx_headers.put("x-miorigin","b");
            jx_headers.put("sec-fetch-site","none");
            jx_headers.put("sec-fetch-mode","navigate");
            jx_headers.put("origin","https://ckmov.ccyjjd.com");
            jx_headers.put("sec-fetch-user","?1");
            jx_headers.put("sec-fetch-dest","document");
            jx_headers.put("accept-encoding","gzip, deflate");
            jx_headers.put("accept-language","zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");

            callback.call(50, "https://ckmov.ccyjjd.com/..ckmov/?url="+resource_url);
            HttpResult jx_result = null;
            int req_count = 0;
            while(jx_result == null || !jx_result.isSign()) {
                if (req_count++ > 3) break;
                jx_result = Constants.ParseHelper.getHsu().doGetBody("https://ckmov.ccyjjd.com/..ckmov/?url=" + resource_url, "https://www.ckmov.vip/", jx_headers, "UTF-8");
            }
            if (jx_result.indexOf(false,"y.encode(other_l)")) {
                callback.call(60, "https://ckmov.ccyjjd.com/..ckmov/?url="+resource_url);
                String reffer_url = jx_result.getFinal_URL();
                JSONObject parse_result = WYUtils.parse(jx_result);
                jx_result = null;
                req_count = 0;
                while(jx_result == null || !jx_result.isSign()) {
                    if (req_count++ > 3) break;
                    jx_result = Constants.ParseHelper.getHsu().doPostBody(Utils.resolveURI(reffer_url,parse_result.getString("api")), (Map)parse_result.getJSONObject("params"), null, jx_headers, "UTF-8");
                }
                JSONObject parseObject = JSONObject.parseObject(jx_result.getResult());
                if(parseObject.getInteger("code") == 200){
                    return new ParseResult(parseObject.getString("url"),jx_result.getFinal_URL(),null);
                } else {
                    throw new StopException(String.format(video_parse_exception_tips, "解析失败:"+jx_result.getResult().trim()));
                }
            } else {
                String urls = Utils.getString(jx_result.getResult(),"urls\\s*=\\s*\"(.*?)\";\\s+");
                if(Utils.isEmpty(urls)){
                    throw new StopException(String.format(video_parse_exception_tips, "解析失败"));
                } else {
                    return new ParseResult(urls,jx_result.getFinal_URL(),null);
                }
            }
        } else if(parse_service.startsWith("1907解析")){
            callback.call(50, "https://a1.m1907.top/api/v/?url="+resource_url);
            String z = MD5Util.getStringMD5(MD5Util.getStringMD5((new Date().getDate() +9+9^10)+"").substring(0,10));
            Map<String,String> jx_headers = new HashMap<String,String>();
            jx_headers.put("user-agent","Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
            jx_headers.put("cache-control","max-age=0");
            jx_headers.put("upgrade-insecure-requests","1");
            jx_headers.put("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            jx_headers.put("sec-fetch-site","none");
            jx_headers.put("sec-fetch-mode","navigate");
            jx_headers.put("sec-fetch-user","?1");
            jx_headers.put("sec-fetch-dest","document");
            jx_headers.put("accept-encoding","gzip, deflate");
            jx_headers.put("accept-language","zh-CN,zh;q=0.9");

            HttpResult jx_result = null;
            int req_count = 0;
            while(jx_result == null || !jx_result.isSign()) {
                if (req_count++ > 3) break;
                //https://m1-a1.cloud.vving.vip:2223/api/v/?z=e8e56ecaca35c6229baa93884b6b7323&jx=https://v.qq.com/x/cover/ww18u675tfmhas6/l0046q18p2i.html&s1ig=11400&g=
                //https://m1-z2.cloud.vving.vip:2223/?jx=https://v.qq.com/x/cover/ww18u675tfmhas6/l0046q18p2i.html
                jx_result = Constants.ParseHelper.getHsu().doGetBody("https://m1-a1.cloud.vving.vip:2223/api/v/?z=" + z + "&jx=" + resource_url + "&s1ig=" + (new Date().getDay() + 11397) + "&g=", "https://z1.m1907.top/", jx_headers, "UTF-8");
            }
            try {
                JSONObject data = JSONObject.parseObject(jx_result.getResult());
                if(data.getString("type").equals("home")){
                    throw new StopException(String.format(video_parse_exception_tips, "解析失败"));
                } else {
                    int ep = data.getInteger("ep");
                    JSONArray eps = data.getJSONArray("data").getJSONObject(0).getJSONObject("source").getJSONArray("eps");
                    String video_url = eps.getJSONObject(ep-1).getString("url");
                    return new ParseResult(video_url,jx_result.getFinal_URL(),null);
                }
            } catch (Exception ex){
                throw new StopException(String.format(video_parse_exception_tips, "解析失败:"+jx_result.getResult().trim()));
            }
        }  else if(parse_service.startsWith("OK解析")){
            //https://api.okjx.cc:3389/jx.php?url=https://v.qq.com/x/cover/mzc00200eacw05k/v0036502bcl.html
            callback.call(50, "https://api.okjx.cc:3389/jx.php?url="+resource_url);
            HttpResult jx_result = null;
            int req_count = 0;
            while(jx_result == null || !jx_result.isSign()){
                if(req_count++>3)break;
                jx_result = Constants.ParseHelper.getHsu().doGetBody("https://api.okjx.cc:3389/jx.php?url="+resource_url, "https://okjx.cc/", headers, "UTF-8");
                if(jx_result.indexOf(false, "myiframe")){
                    String jx_location_url = Utils.resolveURI(jx_result.getFinal_URL(), Jsoup.parse(jx_result.getResult()).getElementById("myiframe").attr("src"));
                    jx_result = Constants.ParseHelper.getHsu().doGetBody(jx_location_url, jx_result.getFinal_URL(), headers, "UTF-8");
                }
            }
            try {
                callback.call(60, "https://api.okjx.cc:3389/jx.php?url="+resource_url);
                if(jx_result.indexOf(false, "indexOf\\(\"v\\.qq\\.com\"\\)")){
                    String jx_url = Utils.getString(jx_result.getResult(), "indexOf\\(\"v\\.qq\\.com\"\\)\\s*\\>\\s*0\\)\\{\\s*play\\('(.*?)'\\);");
                    String referer_url = jx_result.getFinal_URL();
                    jx_result = null;
                    req_count = 0;
                    while(jx_result == null || !jx_result.isSign()) {
                        if (req_count++ > 3) break;
                        jx_result = Constants.ParseHelper.getHsu().doGetBody(Utils.resolveURI(referer_url, jx_url), referer_url, Constants.ParseHelper.getHeaders(), "UTF-8");
                    }
                    while(jx_result.indexOf(false,"iframe")){
                        referer_url = jx_result.getFinal_URL();
                        jx_url = Jsoup.parse(jx_result.getResult()).getElementsByTag("iframe").attr("src");
                        callback.call(80, jx_url);
                        jx_result = null;
                        req_count = 0;
                        while(jx_result == null || !jx_result.isSign()) {
                            if (req_count++ > 3) break;
                            jx_result = Constants.ParseHelper.getHsu().doGetBody(Utils.resolveURI(referer_url, jx_url), referer_url, Constants.ParseHelper.getHeaders(), "UTF-8");
                        }
                    }
                }
                String cipher_key = "4835EB08C22498E1";
                String play_js_url = "https://alywykqrxb-1310340178228433-static.oss-cn-zhangjiakou.aliyuncs.com/64f8a4f8-ff59-44ef-ac45-447fb69deea8/mao/js/okjxplay.js";
                try {
                    play_js_url = Utils.getString(jx_result.getResult(), "\\s+src=\"([^\"\\s]+)play.js")+"play.js";
                } catch (Exception ex){}
                if(play_js_url.endsWith("/js/play.js")){
                    cipher_key = "82057C138FC49A84";
                }
                //HttpResult play_result = Constants.ParseHelper.getHsu().doGetBody(play_js_url, referer_url, Constants.ParseHelper.getMapHeaders(), "UTF-8");
                //cipher_key = Utils.decode_ox(Utils.getString(play_result.getResult(), "\"([\\\\x\\d]{64})\""));
                String referer_url = jx_result.getFinal_URL();
                String url =  Utils.getString(jx_result.getResult(), "OKJX\\(\"(.*?)\"\\),");
                String bt_token =  Utils.getString(jx_result.getResult(), "le_token\\s*=\\s*\"(\\w+)\";");

                byte[] url_bytes = org.bouncycastle.util.encoders.Base64.decode(url);
                String decode_url = new String(AESUtils.decrypt(url_bytes, cipher_key.getBytes("UTF-8"), bt_token.getBytes("UTF-8"), "AES/CBC/PKCS7Padding"));
                if(decode_url.contains("okjx.cc")){
                    throw new StopException(String.format(video_parse_exception_tips, "重定向错误"));
                } else {
                    return new ParseResult(decode_url, referer_url,null);
                }
            } catch (Exception e){
                throw new StopException(String.format(video_parse_exception_tips, jx_result.getFinal_URL() + "\r\n网络异常中断，请重试！！\r\n"));
            }
        } else if(parse_service.startsWith("BL解析")){
            //https://jx.playerjy.com/?url=https://v.qq.com/x/cover/ww18u675tfmhas6.html
            //https://json.vipjx.cnow.eu.org/?url=https://v.qq.com/x/cover/ww18u675tfmhas6.html
            //http://j.zz22x.com/jx/?url=https://v.qq.com/x/cover/ww18u675tfmhas6/n00469vvmx6.html
            //https://vip.bljiex.com/api.php?out=jsonp&url=https://v.qq.com/x/cover/ww18u675tfmhas6/n00469vvmx6.html&cb=jQuery182033904855389117206_1690337960640&_=1690337963062
            callback.call(50, "https://svip.bljiex.cc/api.php?out=&tp=local&url="+resource_url);
            HttpResult jx_result = null;
            int req_count = 0;
            while(jx_result == null || !jx_result.isSign()) {
                if (req_count++ > 3) break;
                jx_result = Constants.ParseHelper.getHsu().doGetBody("https://svip.bljiex.cc/api.php?url=" + resource_url, "https://svip.bljiex.cc/?v=", headers, "UTF-8");
            }
            JSONObject parseObject = JSONObject.parseObject(jx_result.getResult());
            if(parseObject.getInteger("code") != 200){
                throw new StopException(String.format(video_parse_exception_tips, jx_result.getResult().trim()));
            } else {
                return new ParseResult(Utils.URLDecode(parseObject.getString("url")),jx_result.getFinal_URL(),null);
            }
        } else if(parse_service.startsWith("虾米解析")){
            callback.call(50, "https://jx.xmflv.com/?url=?"+resource_url);
            HttpResult jx_result = null;
            int req_count = 0;
            while(jx_result == null || !jx_result.isSign()) {
                if (req_count++ > 3) break;
                jx_result = Constants.ParseHelper.getHsu().doGetBody("https://jx.xmflv.com/?url=" + resource_url, "https://jx.xmflv.com/", headers, "UTF-8");
            }
            /*
            String time =  Utils.getString(jx_result.getResult(), "\\s*\"time\"\\s*:\\s*\"(\\d+)\"");
            String ua = Utils.getString(jx_result.getResult(), "var\\s*ua\\s*=\\s*'(\\w+)';");
            String key = Utils.getString(jx_result.getResult(), "\\s*\"key\"\\s*:\\s*\"([a-zA-Z0-9_\\-\\/=\\+]+)\",");
            String cip = Utils.getString(jx_result.getResult(), "var\\s*cip\\s*=\\s*'([0-9\\.]+)';");
            String fvkey = Utils.getString(jx_result.getResult(), "var\\s*fvkey\\s*=\\s*'(\\w+)';");

            if(Utils.isEmpty(key)){
                throw new StopException(String.format(video_parse_exception_tips, "key解析异常！！"));
            }*/
            Map<String,String> jx_headers = new HashMap<String,String>();
            jx_headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
            jx_headers.put("cache-control","max-age=0");
            jx_headers.put("sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"102\", \"Google Chrome\";v=\"102\"");
            jx_headers.put("accept","application/json, text/javascript, */*; q=0.01");
            jx_headers.put("content-type","application/x-www-form-urlencoded; charset=UTF-8");
            jx_headers.put("x-requested-with","XMLHttpRequest");
            jx_headers.put("sec-ch-ua-mobile","?0");
            jx_headers.put("origin","https://jx.xmflv.com");
            jx_headers.put("sec-ch-ua-platform","\"Windows\"");
            jx_headers.put("sec-fetch-site","same-origin");
            jx_headers.put("sec-fetch-mode","cors");
            jx_headers.put("sec-fetch-dest","empty");

            long time = System.currentTimeMillis();
            String url = URLEncoder.encode(resource_url, "UTF-8");
            String sign_key = MD5Util.getStringMD5(time + url);
            sign_key = new String(org.bouncycastle.util.encoders.Base64.encode(AESUtils.encrypt(sign_key.getBytes("UTF-8"), MD5Util.getStringMD5(sign_key).getBytes("UTF-8"), "3cccf88181408f19".getBytes("UTF-8"), "AES/CBC/NoPadding")), "UTF-8");

            Map<String, String> params = new HashMap<String, String>();
            params.put("wap", "1");
            params.put("url", url);
            params.put("time", time+"");
            params.put("key", sign_key);

            jx_result = null;
            req_count = 0;
            while(jx_result == null || !jx_result.isSign()) {
                if (req_count++ > 3) break;
                //https://cache.m3u8.pw/xmflv.js
                jx_result = Constants.ParseHelper.getHsu().doPostBody("https://122.228.8.29:4433/xmflv.js", params, null, jx_headers, "UTF-8");
            }

            JSONObject parseObject = JSONObject.parseObject(jx_result.getResult());
            if(parseObject.getInteger("code") != 200){
                throw new StopException(String.format(video_parse_exception_tips, jx_result.getResult().trim()));
            } else {
                //Ptbtptpbcptdptpt key
                //ptbtptpbcptdptpT iv
                byte[] de_url_bytes = AESUtils.decrypt(org.bouncycastle.util.encoders.Base64.decode(parseObject.getString("url")), parseObject.getString("aes_key").getBytes("UTF-8"), parseObject.getString("aes_iv").getBytes("UTF-8"), "AES/CBC/PKCS7Padding");
                if(Utils.isEmpty(de_url_bytes)){
                    throw new StopException(String.format(video_parse_exception_tips, "["+parseObject.getString("url")+"]解密失败"));
                } else {
                    jx_headers = new HashMap<String,String>();
                    jx_headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
                    jx_headers.put("sec-ch-ua","\"Chromium\";v=\"116\", \"Not)A;Brand\";v=\"24\", \"Google Chrome\";v=\"116\"");
                    jx_headers.put("accept","*/*");
                    jx_headers.put("sec-ch-ua-mobile","?0");
                    jx_headers.put("origin","https://jx.xmflv.com");
                    jx_headers.put("sec-ch-ua-platform","\"Windows\"");
                    jx_headers.put("sec-fetch-site","cross-site");
                    jx_headers.put("sec-fetch-mode","cors");
                    jx_headers.put("sec-fetch-dest","empty");
                    jx_headers.put("accept-encoding","gzip, deflate, br");
                    jx_headers.put("accept-language","zh-CN,zh;q=0.9");

                    String decode_url = Utils.URLDecode(new String(de_url_bytes, "UTF-8"));
                    if(decode_url.contains("Cache/qq/3101f5f42cd437f6e97ad4fcf90c968a.m3u8")){
                        throw new StopException(String.format(video_parse_exception_tips, "["+resource_url+"]解析失败，请切换其它解析接口再试"));
                        //https://122.228.8.29:4433/Cache/qq/3101f5f42cd437f6e97ad4fcf90c968a.m3u8?vkey=3335376443414a555567414a55676b45423152575851685242773048426759495646745341415948564151474341494c416c4a53
                    }
                    return new ParseResult(decode_url,jx_result.getFinal_URL(), jx_headers);
                }
            }
        } else if(parse_service.startsWith("醉仙解析")){
          callback.call(50, "https://jx.zui.cm/?url="+resource_url);
          HttpResult jx_result = null;
          int req_count = 0;
          while(jx_result == null || !jx_result.isSign()) {
              if (req_count++ > 3) break;
              jx_result = Constants.ParseHelper.getHsu().doGetBody("https://jx.zui.cm/?url=" + resource_url, "https://jx.zui.cm/", headers, "UTF-8");
          }

          String time =  Utils.getString(jx_result.getResult(), "\\s*\"time\"\\s*:\\s*\"(\\d+)\"");
          String key = Utils.getString(jx_result.getResult(), "\\s*\"key\"\\s*:\\s*\"([a-zA-Z0-9_\\-\\/=\\+]+)\",");
          String vkey = Utils.getString(jx_result.getResult(), "\\s*\"vkey\"\\s*:\\s*\"(\\w+)\",");

          if(Utils.isEmpty(key)){
              throw new StopException(String.format(video_parse_exception_tips, "key解析异常！！"));
          }

          Map<String,String> jx_headers = new HashMap<String,String>();
          jx_headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
          jx_headers.put("cache-control","max-age=0");
          jx_headers.put("sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"102\", \"Google Chrome\";v=\"102\"");
          jx_headers.put("accept","application/json, text/javascript, */*; q=0.01");
          jx_headers.put("content-type","application/x-www-form-urlencoded; charset=UTF-8");
          jx_headers.put("x-requested-with","XMLHttpRequest");
          jx_headers.put("sec-ch-ua-mobile","?0");
          jx_headers.put("origin","https://jx.zui.cm");
          jx_headers.put("sec-ch-ua-platform","\"Windows\"");
          jx_headers.put("sec-fetch-site","same-origin");
          jx_headers.put("sec-fetch-mode","cors");
          jx_headers.put("sec-fetch-dest","empty");

          Map<String, String> params = new HashMap<String, String>();
          params.put("url", resource_url);
          params.put("time", time+"");
          params.put("key", key);

          jx_result = null;
          req_count = 0;
          while(jx_result == null || !jx_result.isSign()) {
              if (req_count++ > 3) break;
              jx_result = Constants.ParseHelper.getHsu().doPostBody("https://jx.zui.cm/api.php", params, null, jx_headers, "UTF-8");
          }

          JSONObject parseObject = JSONObject.parseObject(jx_result.getResult());
          if(parseObject.getInteger("code") != 200){
              throw new StopException(String.format(video_parse_exception_tips, jx_result.getResult().trim()));
          } else {
              //ZUIARTPLAYER2023 key
              //ZUIArtplayer2023 iv
              byte[] de_url_bytes = AESUtils.decrypt(org.bouncycastle.util.encoders.Base64.decode(parseObject.getString("url")), "ZUIARTPLAYER2023".getBytes("UTF-8"), "ZUIArtplayer2023".getBytes("UTF-8"), "AES/CBC/PKCS7Padding");
              if(Utils.isEmpty(de_url_bytes)){
                  throw new StopException(String.format(video_parse_exception_tips, "["+parseObject.getString("url")+"]解密失败"));
              } else {
                  jx_headers = new HashMap<String,String>();
                  jx_headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
                  jx_headers.put("sec-ch-ua","\"Chromium\";v=\"116\", \"Not)A;Brand\";v=\"24\", \"Google Chrome\";v=\"116\"");
                  jx_headers.put("accept","*/*");
                  jx_headers.put("sec-ch-ua-mobile","?0");
                  jx_headers.put("sec-ch-ua-platform","\"Windows\"");
                  jx_headers.put("sec-fetch-site","cross-site");
                  jx_headers.put("sec-fetch-mode","cors");
                  jx_headers.put("sec-fetch-dest","empty");
                  jx_headers.put("accept-encoding","gzip, deflate, br");
                  jx_headers.put("accept-language","zh-CN,zh;q=0.9");
                  jx_headers.put("Referer", "");
                  return new ParseResult(Utils.URLDecode(new String(de_url_bytes, "UTF-8")),jx_result.getFinal_URL(), jx_headers);
              }
          }
      } else if(parse_service.startsWith("JY解析")){
          //https://jx.playerjy.com/?url=https://v.qq.com/x/cover/mzc0020027yzd9e/u00465uzz7k.html
          callback.call(50, "https://jx.playerjy.com/?url="+resource_url);
          HttpResult jx_result = null;
          int req_count = 0;
          while(jx_result == null || !jx_result.isSign()){
              if(req_count++>3)break;
              jx_result = Constants.ParseHelper.getHsu().doGetBody("https://jy3.we-vip.com/?url="+resource_url, "https://jx.playerjy.com/?url="+resource_url, headers, "UTF-8");;
          }
          try {
              Document doc = Jsoup.parse(jx_result.getResult());
              String urldata = doc.getElementsByAttributeValue("name", "urldata").attr("content");
              String viewdata = "https://jy3.we-vip.com/?url="+resource_url+"###"+doc.getElementsByAttributeValue("name", "viewdata").attr("content");

              callback.call(60, "https://jx.playerjy.com/?url="+resource_url);
              Map<String, String> params = new HashMap<String, String>();
              params.put("url_data", new String(org.bouncycastle.util.encoders.Base64.encode(urldata.getBytes("UTF-8")), "UTF-8"));
              params.put("view_data", new String(org.bouncycastle.util.encoders.Base64.encode(viewdata.getBytes("UTF-8")), "UTF-8"));
              jx_result = null;
              req_count = 0;
              while(jx_result == null || !jx_result.isSign()){
                  if(req_count++>3)break;
                  jx_result = Constants.ParseHelper.getHsu().doPostBody("https://jy3.we-vip.com/data.php", params,"https://jy3.we-vip.com/?url="+resource_url, headers, "UTF-8");
              }
              doc = Jsoup.parse(jx_result.getResult());
              String url_id = doc.getElementsByAttributeValue("name", "url_id").attr("content");
              String player_id = doc.getElementsByAttributeValue("name", "player_id").attr("content");

              callback.call(80, "https://jx.playerjy.com/?url="+resource_url);
              params = new HashMap<String, String>();
              params.put("url_id", new String(org.bouncycastle.util.encoders.Base64.encode(url_id.getBytes("UTF-8")), "UTF-8"));
              params.put("player_id", new String(org.bouncycastle.util.encoders.Base64.encode(player_id.getBytes("UTF-8")), "UTF-8"));
              jx_result = null;
              req_count = 0;
              while(jx_result == null || !jx_result.isSign()){
                  if(req_count++>3)break;
                  jx_result = Constants.ParseHelper.getHsu().doPostBody("https://jy3.we-vip.com/api.php", params,"https://jy3.we-vip.com/?url="+resource_url, headers, "UTF-8");
              }
              String config = Utils.getString(jx_result.getResult(), "var\\s+config\\s+=\\s+(.*?)\\s+player\\(config\\)");
              JSONObject parseObject = JSONObject.parseObject(config);

              callback.call(90, "https://jx.playerjy.com/?url="+resource_url);
              jx_result = null;
              req_count = 0;
              while(jx_result == null || !jx_result.isSign()){
                  if(req_count++>3)break;
                  jx_result = Constants.ParseHelper.getHsu().doGetBody("https://jy3.we-vip.com/api.php?url="+new String(org.bouncycastle.util.encoders.Base64.encode(parseObject.getString("url").getBytes("UTF-8"))), "https://jy3.we-vip.com/?url="+resource_url , headers, "UTF-8");
              }
              if(jx_result.indexOf(false, "数据异常")){
                  throw new StopException(String.format(video_parse_exception_tips, "数据异常,解析失败！"));
              }
              return new ParseResult(jx_result.getFinal_URL(), "https://jy3.we-vip.com/?url="+resource_url, null);
          } catch (Exception ex){
              throw new StopException(String.format(video_parse_exception_tips, jx_result.getResult()));
          }
      } else if(parse_service.startsWith("万能解析")){
          //http://cdn.apii.top/jx/analysis.php?v=https://v.qq.com/x/cover/ww18u675tfmhas6/l0046q18p2i.html
          //http://cdn.apii.top/jiexi/analysis.php?v=https://v.qq.com/x/cover/ww18u675tfmhas6/l0046q18p2i.html
          callback.call(50, "https://cdn.apii.top/jx/?url="+resource_url);
          HttpResult jx_result = null;
          int req_count = 0;
          while(jx_result == null || !jx_result.isSign()){
              if(req_count++>3)break;
              jx_result = Constants.ParseHelper.getHsu().doGetBody("https://cdn.apii.top/jx/?url="+resource_url, "https://cdn.apii.top", null, "UTF-8");
              if(jx_result.indexOf(false, "myiframe")){
                  String jx_location_url = Utils.resolveURI(jx_result.getFinal_URL(), Jsoup.parse(jx_result.getResult()).getElementById("myiframe").attr("src"));
                  jx_result = Constants.ParseHelper.getHsu().doGetBody(jx_location_url, jx_result.getFinal_URL(), headers, "UTF-8");
              }
          }
          try {
              if(jx_result.indexOf(false, "window.location.href")){
                  throw new StopException("解析失败（当前解析库不支持该视频解析，请切换解析库如777解析）！");
              }
              String config = Utils.getString(jx_result.getResult(), "var\\s+config\\s+=\\s+(.*?)\\s+player\\(config\\)");
              JSONObject parseObject = JSONObject.parseObject(config);
              return new ParseResult(parseObject.getString("url"), jx_result.getFinal_URL(), null);
          } catch (Exception ex){
              if(ex instanceof  StopException) throw ex;
              throw new StopException(String.format(video_parse_exception_tips, jx_result.getResult()));
          }
      } else if(parse_service.startsWith("全民解析")){
          //https://jx.playerjy.com/?url=https://v.qq.com/x/cover/mzc0020027yzd9e/u00465uzz7k.html
          //https://bd.jx.cn/api.php?url=https%3A%2F%2Fv.qq.com%2Fx%2Fcover%2Fmzc0020027yzd9e%2Fu00465uzz7k.html //冰豆解析（非凡资源，优质资源）
          //https://bd.jx.cn/api.php?url=https://v.qq.com/x/cover/mzc00200tl9nvn9/p0047ihva05.html
          //https://svip.znjson.top/?url=https://v.qq.com/x/cover/ww18u675tfmhas6.html
          //https://2.08bk.com/api.php?url=https%3A%2F%2Fv.qq.com%2Fx%2Fcover%2Fww18u675tfmhas6%2Fn00469vvmx6.html&cb=jQuery182003835576729453716_1690337845038&_=1690337868779
          //http://api.apii.top/?v=https://v.qq.com/x/cover/ww18u675tfmhas6/l0046q18p2i.html
          //http://api.apii.top/api.php?url=https://v.qq.com/x/cover/ww18u675tfmhas6/l0046q18p2i.html
          //http://vip.api.apii.top/api.php?url=https://v.qq.com/x/cover/ww18u675tfmhas6/l0046q18p2i.html
          //https://player.cmov.cn/API.php url=https%3A%2F%2Fv.qq.com%2Fx%2Fcover%2Fww18u675tfmhas6.html&time=1690358681&key=
          //https://dmplay.xyz/api/v1.0/dmplay/decrypt post url=https%3A%2F%2Fv.qq.com%2Fx%2Fcover%2F324olz7ilvo2j5f.html
          callback.call(50, "https://jx.quanmingjiexi.com/?url="+resource_url);
          HttpResult jx_result = null;
          int req_count = 0;
          while(jx_result == null || !jx_result.isSign()) {
              if (req_count++ > 3) break;
              jx_result = Constants.ParseHelper.getHsu().doGetBody("https://api.quanminjiexi.com/api.php?url="+resource_url, "http://quanminjiexi.com/", headers, "UTF-8");
              if(jx_result == null || !jx_result.isSign()){
                  jx_result = Constants.ParseHelper.getHsu().doGetBody("http://api.apii.top/api.php?url="+resource_url, "http://api.apii.top/", headers, "UTF-8");
              }
          }
          try {
              JSONObject parseObject = JSONObject.parseObject(jx_result.getResult());
              if(parseObject.getInteger("code") == 200){
                  return new ParseResult(parseObject.getString("url"), jx_result.getFinal_URL(), null);
              } else if(parseObject.getInteger("m") == 404){
                  throw new StopException("解析失败（当前解析库不支持该视频解析，请切换解析库）！");
              } else {
                  throw new StopException("解析失败，解析通道需要重新分析，请联系JO！");
              }
          } catch (Exception ex){
              if(ex instanceof  StopException) throw ex;
              throw new StopException(String.format(video_parse_exception_tips, jx_result.getResult()));
          }
        } else if(parse_service.startsWith("DM解析")){
          //https://dmplay.xyz/api/v1.0/dmplay/decrypt post url=https%3A%2F%2Fv.qq.com%2Fx%2Fcover%2F324olz7ilvo2j5f.html
          callback.call(50, "https://jx.quanmingjiexi.com/?url="+resource_url);
          HttpResult jx_result = null;
          int req_count = 0;
          while(jx_result == null || !jx_result.isSign()) {
              if (req_count++ > 3) break;
              jx_result = Constants.ParseHelper.getHsu().doPostBody("https://dmplay.xyz/api/v1.0/dmplay/decrypt", "url="+URLEncoder.encode(resource_url, "UTF-8"), "https://dmplay.xyz/play/", headers, "UTF-8");
          }
          try {
              JSONObject parseObject = JSONObject.parseObject(jx_result.getResult());
              if(parseObject.getInteger("state") == 1){
                  JSONArray urls = parseObject.getJSONObject("data").getJSONArray("urls");
                  return new ParseResult(urls.getString(new Random().nextInt(urls.size())), jx_result.getFinal_URL(), null);
              } else {
                  throw new StopException("解析失败，解析通道需要重新分析，请联系JO！");
              }
          } catch (Exception ex){
              if(ex instanceof  StopException) throw ex;
              throw new StopException(String.format(video_parse_exception_tips, jx_result.getResult()));
          }
      } else if(parse_service.startsWith("yparse云解析")){
          //https://yparse.ik9.cc/index.php?url=https://v.qq.com/x/cover/ww18u675tfmhas6/n00469vvmx6.html
          callback.call(50, "https://yparse.ik9.cc/index.php?url="+resource_url);
          HashMap pri_headers = new HashMap();
          pri_headers.put("sec-ch-ua", "\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"");
          pri_headers.put("sec-ch-ua-mobile", "?0");
          pri_headers.put("accept", "application/json, text/javascript, */*; q=0.01");
          pri_headers.put("x-requested-with", "XMLHttpRequest");
          pri_headers.put("sec-fetch-site", "same-origin");
          pri_headers.put("accept-encoding", "gzip, deflate");
          pri_headers.put("accept-language", "zh-CN,zh;q=0.9");
          pri_headers.put("sec-ch-ua-platform", "Windows");
          pri_headers.put("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
          pri_headers.put("origin", "https://jx.xxza.top:7788");

          //获取最终解析地址
          String jx_url = "https://jx.xxza.top:7788/index.php?url=" + resource_url;
          HttpResult jx_result = null;
          int req_count = 0;
          while(jx_result == null || !jx_result.isSign()) {
              if (req_count++ > 3) break;
              jx_result = Constants.ParseHelper.getHsu().doGetBody("https://yparse.ik9.cc/index.php?url="+resource_url, "https://yparse.ik9.cc", headers, "UTF-8");
          }

          String eval_js = Utils.getString(jx_result.getResult(), "script\\>\\s*eval\\('(.*?)'\\);");
          eval_js = Utils.decode_ox(eval_js);
          String key = Utils.getString(eval_js, "var\\s+key\\s+=\\s+'(.*?)';");

          eval_js = Utils.getString(jx_result.getResult(), "'use strict';\\s*eval\\('(.*?)'\\);");
          eval_js = Utils.decode_ox(eval_js);
          eval_js = Utils.getString(eval_js, "eval\\(strdecode\\(\"([A-Za-z0-9\\+/_\\-=]{4000,8000})\"\\)\\);");
          eval_js = yparse_strdecode(key, eval_js);
          String url  = Utils.getString(eval_js, "url\\s*:\\s*'([A-Za-z0-9\\+/_\\-=]+)',");
          String ip      = Utils.getString(eval_js, "ip\\s*:\\s*'([0-9\\.]+)',");

          LinkedHashMap<String,String> params = new LinkedHashMap<String,String>();
          params.put("ip", ip);
          params.put("url", url);
          params.put("ly", "https://yparse.ik9.cc/");
          params.put("ua", Constants.ParseHelper.getHsu().getUSER_AGENT());

          jx_result = null;
          while (jx_result == null || !jx_result.isSign()) {
              jx_result = Constants.ParseHelper.getHsu().doPostBody("https://yparse.ik9.cc/api.php", params, "https://yparse.ik9.cc/index.php?url="+resource_url, pri_headers, "UTF-8");
          }

          JSONObject parseObject = JSONObject.parseObject(jx_result.getResult());
          if(parseObject.getInteger("code") == 200){
              String decode_url = yparse_strdecode(key, parseObject.getString("sign"));
              if(decode_url.trim().startsWith("{")){
                  JSONObject decode_url_object = JSONObject.parseObject(decode_url);
                  if(decode_url_object.getInteger("code") == 200){
                      if(decode_url_object.getString("url").startsWith("//") || decode_url_object.getString("url").contains(resource_url)){
                          jx_url = decode_url_object.getString("url");
                      } else {
                          jx_url = yparse_strdecode(key, decode_url_object.getString("url"));
                      }
                      if(jx_url.startsWith("//")){
                          jx_url = "https:" +jx_url;
                      }
                      callback.call(60, jx_url);
                  }
              }
          }

          //解析
          jx_result = null;
          req_count = 0;
          while(jx_result == null || !jx_result.isSign()) {
              if (req_count++ > 3) break;
              jx_result = Constants.ParseHelper.getHsu().doGetBody(jx_url, "https://yparse.ik9.cc", headers, "UTF-8");
          }
          try {
              String styles_js = Utils.getString(jx_result.getResult(), "\\s+src=\"(.*?)\"\\>\\</script\\>");
              if(styles_js == null){
                  throw new StopException("解析失败，解析通道需要重新分析，请联系JO！");
              }
              callback.call(70, jx_url);
              HttpResult js_result = null;
              req_count = 0;
              while(js_result == null || !js_result.isSign()) {
                  if (req_count++ > 3) break;
                  js_result = Constants.ParseHelper.getHsu().doGetBody("https:"+styles_js, jx_result.getFinal_URL(), headers, "UTF-8");
              }
              styles_js = Utils.getString(js_result.getResult(), "eval\\('(.*?)'\\);");
              styles_js = Utils.decode_ox(styles_js.trim());
              //var key = 'PCYDIGKPOO00OWES4SOO00OK1RB56J8WIHCJIEOO00OHD7MZKDWX1UHN7FWWTTO3FEHLOCCQWKLRLHCZKOPC7YAHO5L0BT2OSAGJVKHMQQEMEDZK5WJJBFVXXRHTR5I6S7YO0O0O';
              key = Utils.getString(styles_js, "var\\s+key\\s+=\\s+'(.*?)';");
              if(Utils.isEmpty(key)){
                  throw new StopException("解析失败，解析通道需要重新分析，请联系JO！");
              }

                     eval_js = Utils.getString(jx_result.getResult(), ";eval\\('(.*?)'\\);\\<");
                     eval_js = Utils.decode_ox(eval_js.trim());
                     eval_js = Utils.getString(eval_js, "strdecode\\(\"(.*?)\"\\)");
                     eval_js = yparse_strdecode(key, eval_js);
                     eval_js = Utils.getString(eval_js, "eval\\(strdecode\\(\"([A-Za-z0-9\\+/_\\-=]{4000,8000})\"\\)\\);");
                     eval_js = yparse_strdecode(key, eval_js);
              String sign_data = Utils.getString(eval_js, "sign\\('(\\w+)'\\),");
              String referer   = Utils.getString(eval_js, "other\\s*:\\s*'([A-Za-z0-9\\+/_\\-=]+)',");
                     url       = Utils.getString(eval_js, "url\\s*:\\s*'([A-Za-z0-9\\+/_\\-=]+)',");
              String time      = Utils.getString(eval_js, "time\\s*:\\s*'(\\d+)',");

              params = new LinkedHashMap<String,String>();
              params.put("referer", referer);
              params.put("time", time);
              params.put("key", yparse_sign(key, sign_data));
              params.put("url", yparse_strdecode(key, url));
              params.put("type", "0");
              params.put("up", "0");

              pri_headers.put("origin", Utils.getString(jx_url, "(.*?)/index.php\\?url="));
              String api_url = Utils.getString(jx_url, "(.*?)index.php\\?url=") + "api.php";
              callback.call(80, api_url);

              jx_result = null;
              while (jx_result == null || !jx_result.isSign()) {
                  jx_result = Constants.ParseHelper.getHsu().doPostBody(api_url, params, null, pri_headers, "UTF-8");
              }

              parseObject = JSONObject.parseObject(jx_result.getResult());
              if(parseObject.getInteger("code") == 200){
                  String decode_url = yparse_strdecode(key, parseObject.getString("sign"));
                  if(decode_url.trim().startsWith("{")){
                      JSONObject decode_url_object = JSONObject.parseObject(decode_url);
                      if(decode_url_object.getInteger("code") == 200){
                          decode_url = yparse_strdecode(key, decode_url_object.getString("url"));
                          if(decode_url.startsWith("//")){
                              decode_url = "https:" +decode_url;
                          }
                      } else {
                          throw new StopException("解析失败("+decode_url+")！");
                      }
                  }
                  if(decode_url.contains("/player/404_")){
                      throw new StopException("解析失败（当前解析库不支持该视频解析，请切换解析库）！");
                  }
                  return new ParseResult(decode_url, jx_result.getFinal_URL(), null);
              } else {
                  throw new StopException("解析失败，解析通道需要重新分析，请联系JO！");
              }
          } catch (Exception ex){
              if(ex instanceof StopException){
                  throw ex;
              }
              throw new StopException(String.format(video_parse_exception_tips, jx_result.getResult()));
          }
      } else if(video_name != null && parse_service.startsWith("采集站搜索")){
          List<String> mac_lists = new ArrayList<String>();
          Set<String> parse_keys = Utils.getParseLibsModuleList().keySet();
          for(String key : parse_keys){
              if(Utils.getParseLibsModuleList().getJSONObject(key).containsKey("vsearch") && Utils.getParseLibsModuleList().getJSONObject(key).getInteger("vsearch") == 1){
                  mac_lists.add(key);
              }
          }
          if(Utils.isEmpty(mac_lists)){
              throw new StopException("解析失败，无可用采集站资源可用，请联系JO！");
          }
          //打乱顺序
          Collections.shuffle(mac_lists);
          callback.call(50, "jx/?url="+resource_url);
          ExecutorService executor = Executors.newFixedThreadPool(3);
          Map<String, Future<ParseResult>> futures = new HashMap<String, Future<ParseResult>>();
          for(String mac_parse_key : mac_lists){
              final JSONObject mac_parse_config = Utils.getParseLibsModuleList().getJSONObject(mac_parse_key);
              futures.put(mac_parse_key, executor.submit(new Callable<ParseResult>() {
                  @Override
                  public ParseResult call() throws Exception {
                      String ac = "detail";
                      if(mac_parse_config.containsKey("ac")){
                          ac = mac_parse_config.getString("ac");
                      }
                      String path = "/api.php/provide/vod/";
                      if(mac_parse_config.containsKey("path") && !Utils.isEmpty(mac_parse_config.getString("path"))){
                          path = mac_parse_config.getString("path");
                      }
                      List<String> all_serverUrls = Utils.parseUrls(mac_parse_config.getJSONArray("urls"));
                      HttpResult jx_result = null;
                      for(String serverUrl : all_serverUrls) {
                          jx_result = Constants.ParseHelper.getHsu().doGetBody(serverUrl + path + "?ac="+ac+"&wd="+URLEncoder.encode(video_name.replace(" ", "%"), "UTF-8"), null, null, "UTF-8");
                          if(jx_result != null || jx_result.indexOf(false, "pagecount\":")){
                              break;
                          }
                      }
                      if(jx_result != null || jx_result.indexOf(false, "pagecount\":")){
                          JSONObject vods = JSONObject.parseObject(jx_result.getResult().trim());
                          if(vods.getInteger("code") == 1 && vods.getInteger("total") > 0){
                              String episode = Utils.getString(episode_tag, "(\\d+)");
                              JSONArray vod_list = vods.getJSONArray("list");
                              for(int i=0;i<vod_list.size();i++){
                                  String vod_play_note = vod_list.getJSONObject(i).getString("vod_play_note");
                                  vod_play_note = vod_play_note.replace("$", "\\$");
                                  String[] vod_play_urls = vod_list.getJSONObject(i).getString("vod_play_url").split(vod_play_note);
                                  String vod_play_from = vod_list.getJSONObject(i).getString("vod_play_from");

                                  for(String vod_play_url : vod_play_urls){
                                      if(vod_play_url.contains("m3u8")){
                                          String[] episode_urls = vod_play_url.split("#");
                                          for(int j=0;j<episode_urls.length;j++){
                                              String episode_url = episode_urls[j];
                                              if(Utils.isEmpty(episode_tag) || video_alias_name.contains(episode_url.split("\\$")[0]) || episode_url.startsWith(video_alias_name.split("：")[0]) || episode_url.startsWith(video_alias_name) || episode_url.startsWith("第0"+episode+"集") || episode_url.startsWith("第"+episode+"集")){
                                                  return new ParseResult(episode_url.split("\\$")[1], jx_result.getFinal_URL(), null);
                                              }
                                          }
                                      }
                                  }
                              }
                          }
                      }
                      return null;
                  }
              }));
          }

          Map<String, ParseResult> completed_pr = new HashMap<String, ParseResult>();
          for(String mac_parse_key : mac_lists){
              try {
                  callback.call(80, "["+mac_parse_key+"]jx/?url="+resource_url);
                  ParseResult pr = futures.get(mac_parse_key).get(1, TimeUnit.SECONDS);
                  if(!Utils.isEmpty(pr)){
                      completed_pr.put(mac_parse_key, pr);
                      try {
                          executor.shutdown();
                      } catch (Throwable th) {
                          th.printStackTrace();
                      }
                      callback.call(100, "["+mac_parse_key+"]jx/?url="+resource_url);
                      return pr;
                  }
              } catch (Exception e) {}
          }
          throw new StopException("解析失败（当前解析库无该视频资源，请切换解析库）！");
      } else {
            return parse_xyflv(resource_url, callback);
        }
    }

    public static ParseResult parse_xyflv(String resource_url, Callback callback) throws Exception {
        callback.call(50, "https://jx.77flv.cc/?url="+resource_url);
        Map<String,String> jx_headers = new HashMap<String,String>();
        jx_headers.put("cache-control","max-age=0");
        jx_headers.put("upgrade-insecure-requests","1");
        jx_headers.put("sec-ch-ua-mobile","?0");
        jx_headers.put("sec-ch-ua","\"Not/A)Brand\";v=\"8\", \"Chromium\";v=\"126\", \"Google Chrome\";v=\"126\"");
        jx_headers.put("sec-ch-ua-platform","\"Windows\"");
        jx_headers.put("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        jx_headers.put("sec-fetch-site","cross-site");
        jx_headers.put("sec-fetch-mode","navigate");
        jx_headers.put("sec-fetch-dest","iframe");
        jx_headers.put("priority","u=0, i");
        jx_headers.put("accept-encoding","gzip, deflate, br, zstd");
        jx_headers.put("accept-language","zh-CN,zh;q=0.9");
        jx_headers.put("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36");

        long rand_seed = (long) ((Math.random() * 9 + 1) * 1000000000);
        String play_vid = rand_seed + "|" + Utils.btoa(resource_url, VIPParseHelper.hxm_randstr) + "|" + System.currentTimeMillis()/1000;
               play_vid = hxm_encrypt("ni po jie ge j bA", Utils.btoa(play_vid, VIPParseHelper.hxm_randstr));
        HttpResult jx_result = null;
        int req_count = 0;
        while(jx_result == null || !jx_result.isSign()) {
            if (req_count++ > 3) break;
            jx_result = Constants.ParseHelper.getHsu().doGetBody("https://api.xymp4.cc:4433/Index.php?play_vid="+play_vid+"&ref="+resource_url, "https://jx.77flv.cc/", jx_headers, "UTF-8");
        }

        String hxm_salt = "ni po jie ge  jbA";
        byte[] aes_key = "ni po jie ge  jb".getBytes("UTF-8");
        byte[] aes_iv = "ni po jie ge j b".getBytes("UTF-8");
        //计算XOR的加密密钥
        byte[] xor_keys = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < xor_keys.length; i++) {
            xor_keys[i] = (byte)(aes_key[i] ^ aes_iv[i]);
        }

        String Domain = Utils.getString(jx_result.getResult(), "var\\s+Domain\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Api = Utils.getString(jx_result.getResult(), "var\\s+Api\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Host = Domain;
        Document doc = Jsoup.parse(jx_result.getResult());
        String content_id = doc.getElementsByAttributeValue("http-equiv", "Content-Type").attr("id");
        String viewport_id = doc.getElementsByAttributeValue("name", "viewport").attr("id").substring("viewport".length());
        String Time = Utils.getString(jx_result.getResult(), "var\\s+Time\\s*=\\s*\\s*[\"|']+(\\d+)\\s*[\"|']+;");
        String Version = Utils.getString(jx_result.getResult(), "var\\s+Version\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Vurl = Utils.getString(jx_result.getResult(), "var\\s+Vurl\\s*=\\s*[\"|']+([\\w-_/+=.]+)[\"|']+;");
        String Ref = Utils.getString(jx_result.getResult(), "var\\s+Ref\\s*=\\s*[\"|']+([\\w-_/+=.]*)[\"|']+;");

        try {
            rand_seed = (long) ((Math.random() * 9 + 1) * 1000000000);
            Vurl = rand_seed + "|" + Vurl + "|" + System.currentTimeMillis() / 1000;
            Vurl = hxm_encrypt(hxm_salt, Utils.btoa(Vurl, VIPParseHelper.hxm_randstr));

            callback.call(60, jx_result.getFinal_URL());
            LinkedHashMap<String,String> en_params = new LinkedHashMap<String,String>();
            en_params.put("url", Vurl);
            en_params.put("wap", "0");
            en_params.put("ios", "0");
            en_params.put("host", Host);
            en_params.put("referer", Ref);
            en_params.put("time", Time);

            //将前16个字节与XOR密钥进行XOR操作
            byte[] en_bytes = JSON.toJSONString(en_params).getBytes("UTF-8");
            for (int i = 0; i < xor_keys.length; i++) {
                en_bytes[i] ^= xor_keys[i];
            }
            Map<String, String> post_params = new HashMap<String, String>();
            post_params.put("Params", HexUtils.byte2hex(AESUtils.encrypt(en_bytes, aes_key, aes_iv, "AES/CBC/PKCS7Padding")).toUpperCase());

            HashMap headers = new HashMap();
            headers.put("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36");
            headers.put("sec-ch-ua","\"Not/A)Brand\";v=\"8\", \"Chromium\";v=\"126\", \"Google Chrome\";v=\"126\"");
            headers.put("sec-ch-ua-mobile", "?0");
            headers.put("upgrade-insecure-requests", "1");
            headers.put("accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("x-requested-with", "XMLHttpRequest");
            headers.put("sec-fetch-site", "cross-site");
            headers.put("sec-fetch-mode", "navigate");
            headers.put("sec-fetch-dest", "iframe");
            headers.put("accept-encoding", "gzip, deflate, br");
            headers.put("accept-language", "zh-CN,zh;q=0.9");
            headers.put("sec-ch-ua-platform", "Windows");
            headers.put("content-type", "application/x-www-form-urlencoded");
            headers.put("connection", "keep-alive");

            String video_parse_uuid = UUID.randomUUID().toString().toUpperCase();
            headers.put("video-parse-uuid", video_parse_uuid);
            headers.put("video-parse-time", Time);
            headers.put("video-parse-version", Version);

            String video_parse_sign_strs = Host + " | " + video_parse_uuid + " | " + Time + " | " + Version + " | " + post_params.get("Params");
            //将前16个字节与XOR密钥进行XOR操作
            byte[] video_paras_sign_bytes = video_parse_sign_strs.getBytes("UTF-8");
            for (int i = 0; i < xor_keys.length; i++) {
                video_paras_sign_bytes[i] ^= xor_keys[i];
            }
            headers.put("video-parse-sign", HexUtils.byte2hex(AESUtils.encrypt(video_paras_sign_bytes, aes_key, aes_iv, "AES/CBC/PKCS7Padding")).toUpperCase());

            String jx_url = Utils.resolveURI(jx_result.getFinal_URL().split("\\?")[0], Api + "/Api.php");
            callback.call(80, jx_url);

            URI jx_uri = URI.create(jx_url);
            if (jx_uri.getPort() == 443 || jx_uri.getPort() == 80) {
                headers.put("Origin", jx_uri.getScheme() + "://" + jx_uri.getHost());
            } else {
                headers.put("Origin", jx_uri.getScheme() + "://" + jx_uri.getHost() + ":" + jx_uri.getPort());
            }

            HttpResult result = null;
            while (result == null || !result.isSign()) {
                result = Constants.ParseHelper.getHsu().doPostBody(jx_url, post_params, jx_result.getFinal_URL(), headers, "UTF-8");
                if (result.indexOf(true, "title>检测中", "location.href")) {
                    String location_href = Utils.getString(result.getResult(), "location.href\\s*=\\s*\"(.*?)\";");
                    location_href = Utils.resolveURI(result.getFinal_URL(), location_href);
                    callback.call(90, location_href);
                    result = Constants.ParseHelper.getHsu().doPostBody(location_href, post_params, jx_result.getFinal_URL(), headers, "UTF-8");
                }
                if (!result.isSign()) {
                    if (result.indexOf(false, "403")) {
                        throw new StopException("解析播放地址失败【403】");
                    }
                }
            }
            try {
                JSONObject parseObject = JSONObject.parseObject(result.getResult());
                if (Utils.isEmpty(parseObject.getString("Data"))) {
                    throw new StopException("视频已失效(返回空url)，建议更换资源");
                } else if (parseObject.getString("Data").endsWith(".html")) {
                    throw new StopException("视频地址已失效，新地址暂无法解析");
                } else {
                    if (parseObject.getInteger("Code") == 10) {
                        //10 2724379861 zyxwvutsrqponmlkjsihgfedcba65842585331 b117ddf546db83c7b371245bd7db2812 V3.2
                        String decode_key = MD5Util.getStringMD5(parseObject.getString("Code") + content_id + viewport_id + parseObject.getString("Appkey") + parseObject.getString("Version"));
                        byte[] decode_bytes = AESUtils.decrypt(HexUtils.hex2byte(parseObject.getString("Data").getBytes("UTF-8")), decode_key.substring(0, 16).getBytes("ISO-8859-1"), decode_key.substring(16).getBytes("ISO-8859-1"), "AES/CBC/PKCS7Padding");

                        //System.out.println(new String(decode_bytes, "UTF-8"));
                        //执行二层解密
                        decode_bytes = AESUtils.decrypt(HexUtils.hex2byte(decode_bytes), aes_key, aes_iv, "AES/CBC/PKCS7Padding");
                        for (int i = 0; i < xor_keys.length; i++) {
                            decode_bytes[i] ^= xor_keys[i];
                        }
                        //System.out.println(new String(decode_bytes, "UTF-8"));
                        JSONObject jmdata = JSONObject.parseObject(new String(decode_bytes, "UTF-8"));
                        if (Utils.isEmpty(jmdata.getString("url"))) {
                            throw new StopException("视频已失效(返回空url)，建议更换资源");
                        } else if (jmdata.getString("url").endsWith(".html")) {
                            throw new StopException("视频地址已失效，新地址暂无法解析");
                        } else {
                            String decode_url = hxm_decrypt(hxm_salt, jmdata.getString("url").substring(1));
                            if(decode_url.startsWith("aHR0cH")){
                                byte[] base64_url_bytes = android.util.Base64.decode(decode_url.getBytes("UTF-8"), Base64.DEFAULT);
                                //剔除结尾\x00字节
                                for(int i=1;i<3;i++){
                                    if(base64_url_bytes[base64_url_bytes.length - i] == 0){
                                        base64_url_bytes[base64_url_bytes.length - i] = 32;
                                    }
                                }
                                decode_url = new String(base64_url_bytes, "UTF-8").trim();
                            }
                            System.out.println(decode_url);
                            if (decode_url.contains("%3A%2F%")) {
                                decode_url = Utils.URLDecode(decode_url);
                            }
                            System.out.println(decode_url);
                            if (decode_url.contains("/playurl/")) {
                                String referer = result.getFinal_URL();
                                result = null;
                                while (result == null || !result.isSign()) {
                                    result = Constants.ParseHelper.getHsu().doGetBody(decode_url, referer, headers, "UTF-8");
                                }
                                return new ParseResult("jo_string://" + result.getResult().trim(), referer, headers);
                            } else {
                                return new ParseResult(decode_url, result.getFinal_URL(), null);
                            }
                        }
                    } else if (parseObject.getInteger("Code") == 9) {
                        throw new StopException("解析失败，操作过于频繁或需要人机校验，请稍后重试！");/*
                        if (jx_result.getFilepath().equals("repeated")) {

                        } else {
                            jx_result.setFilepath("repeated");
                            try {
                                Thread.sleep(2000);
                            } catch (Exception ex) {}
                            return VIPParseHelper.parse_xyflv(resource_url, callback);
                        }*/
                    } else {
                        throw new StopException("解析失败，解析通道需要重新分析，请联系JO！");
                    }
                }
            } catch (Exception ex) {
                throw new StopException(String.format(video_parse_exception_tips, result.getResult()));
            }
        } catch (Throwable ex) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(bos));
            throw new StopException(String.format(video_parse_exception_tips, bos.toString()));
        }
    }

    public static String yparse_sign(String key, String data){
        try {
            String sign_iv = key.substring(15, 31);
            String sign_key = MD5Util.getStringMD5(data);
            byte[] sign_data = AESUtils.encrypt(data.getBytes("UTF-8"), sign_key.getBytes("UTF-8"), sign_iv.getBytes("UTF-8"), "AES/CBC/NoPadding");
            return new String(org.bouncycastle.util.encoders.Base64.encode(sign_data), "UTF-8");
        } catch (Exception ex){
            return null;
        }
    }

    public static String yparse_strdecode(String key, String data){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:00");
            long time = sdf.parse(sdf.format(new Date())).getTime()/1000;
            key = key.substring(0xa, 0x14);
            key = MD5Util.getStringMD5(MD5Util.getStringMD5(time + key));

            data = new String(org.bouncycastle.util.encoders.Base64.decode(data));
            StringBuffer result = new StringBuffer();
            for (int j = 0; j < data.length(); j++) {
                int i = j % 32;
                char c = (char)(data.charAt(j) ^ key.charAt(i));
                result.append(c);
            }
            return new String(org.bouncycastle.util.encoders.Base64.decode(result.toString()));
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static ParseResult parse_quanmingjiexi2(HttpResult jx_result, Callback callback) throws Exception {
        String Domain = Utils.getString(jx_result.getResult(), "var\\s+Domain\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Api = Utils.getString(jx_result.getResult(), "var\\s+Api\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Host = Domain;
        Document doc = Jsoup.parse(jx_result.getResult());
        String content_id = doc.getElementsByAttributeValue("http-equiv", "Content-Type").attr("id");
        String viewport_id = doc.getElementsByAttributeValue("name", "viewport").attr("id").substring("viewport".length());
        String Time = Utils.getString(jx_result.getResult(), "var\\s+Time\\s*=\\s*\\s*[\"|']+(\\d+)\\s*[\"|']+;");
        String Version = Utils.getString(jx_result.getResult(), "var\\s+Version\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Vurl = Utils.getString(jx_result.getResult(), "var\\s+Vurl\\s*=\\s*[\"|']+([\\w-_/+=.]+)[\"|']+;");
        String Ref = Utils.getString(jx_result.getResult(), "var\\s+Ref\\s*=\\s*[\"|']+([\\w-_/+=.]*)[\"|']+;");

        try {
            callback.call(60, jx_result.getFinal_URL());

            LinkedHashMap<String,String> en_params = new LinkedHashMap<String,String>();
            en_params.put("url", Vurl);
            en_params.put("wap", "0");
            en_params.put("ios", "0");
            en_params.put("host", Host);
            en_params.put("referer", Ref);
            en_params.put("time", Time);

            Map<String, String> post_params = new HashMap<String, String>();
            post_params.put("Params", HexUtils.byte2hex(AESUtils.encrypt(JSON.toJSONString(en_params).getBytes("UTF-8"), "ni po jie ge jb  ".getBytes("UTF-8"), "ni po jie ge j b".getBytes("UTF-8"), "AES/CBC/PKCS7Padding")).toUpperCase());

            HashMap headers = new HashMap();
            headers.put("sec-ch-ua", "\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"");
            headers.put("sec-ch-ua-mobile", "?0");
            headers.put("upgrade-insecure-requests", "1");
            headers.put("accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("x-requested-with", "XMLHttpRequest");
            headers.put("sec-fetch-site", "cross-site");
            headers.put("sec-fetch-mode", "navigate");
            headers.put("sec-fetch-dest", "iframe");
            headers.put("accept-encoding", "gzip, deflate, br");
            headers.put("accept-language", "zh-CN,zh;q=0.9");
            headers.put("sec-ch-ua-platform", "Windows");
            headers.put("accept-encoding", "gzip, deflate, br");
            headers.put("accept-language", "zh-CN,zh;q=0.9");
            headers.put("content-type", "application/x-www-form-urlencoded");
            headers.put("connection", "keep-alive");

            String video_parse_uuid = UUID.randomUUID().toString().toUpperCase();
            headers.put("Video-Parse-Uuid", video_parse_uuid);
            headers.put("Video-Parse-Time", Time);
            headers.put("Video-Parse-Version", Version);
            String video_parse_sign_strs = Host + " | " + video_parse_uuid + " | " + Time + " | " + Version + " | " + post_params.get("Params");
            headers.put("Video-Parse-Sign", HexUtils.byte2hex(AESUtils.encrypt(video_parse_sign_strs.getBytes("UTF-8"), "ni po jie ge jb ".getBytes("UTF-8"), "ni po jie ge j b".getBytes("UTF-8"), "AES/CBC/PKCS7Padding")).toUpperCase());

            String jx_url = Utils.resolveURI(jx_result.getFinal_URL().split("\\?")[0], Api + "/Api.php");
            callback.call(80, jx_url);

            URI jx_uri = URI.create(jx_url);
            if(jx_uri.getPort() == 443 || jx_uri.getPort() == 80){
                headers.put("Origin", jx_uri.getScheme() + "://" + jx_uri.getHost());
            } else {
                headers.put("Origin", jx_uri.getScheme() + "://" + jx_uri.getHost()+":"+jx_uri.getPort());
            }

            HttpResult result = null;
            while (result == null || !result.isSign()) {
                result = Constants.ParseHelper.getHsu().doPostBody(jx_url, post_params, jx_result.getFinal_URL(), headers, "UTF-8");
                if(result.indexOf(true,"title>检测中", "location.href")){
                    String location_href = Utils.getString(result.getResult(),"location.href\\s*=\\s*\"(.*?)\";");
                    location_href = Utils.resolveURI(result.getFinal_URL(),location_href);
                    callback.call(90, location_href);
                    result = Constants.ParseHelper.getHsu().doPostBody(location_href, post_params, jx_result.getFinal_URL(), headers, "UTF-8");
                }
                if (!result.isSign()) {
                    if (result.indexOf(false, "403")) {
                        throw new StopException("解析播放地址失败【403】");
                    }
                }
            }
            try {
                JSONObject parseObject = JSONObject.parseObject(result.getResult());
                if (Utils.isEmpty(parseObject.getString("Data"))) {
                    throw new StopException("视频已失效(返回空url)，建议更换资源");
                } else if (parseObject.getString("Data").endsWith(".html")) {
                    throw new StopException("视频地址已失效，新地址暂无法解析");
                } else {
                    if(parseObject.getInteger("Code") == 10){
                        //10 2724379861 zyxwvutsrqponmlkjsihgfedcba65842585331 b117ddf546db83c7b371245bd7db2812 V3.2
                        String decode_key = MD5Util.getStringMD5(parseObject.getString("Code") + content_id + viewport_id + parseObject.getString("Appkey") + parseObject.getString("Version"));
                        byte[] decode_bytes = AESUtils.decrypt(HexUtils.hex2byte(parseObject.getString("Data").getBytes("UTF-8")), decode_key.substring(0,16).getBytes("ISO-8859-1"), decode_key.substring(16).getBytes("ISO-8859-1"), "AES/CBC/PKCS7Padding");
                        JSONObject jmdata = JSONObject.parseObject(new String(decode_bytes,"UTF-8"));
                        if(Utils.isEmpty(jmdata.getString("url"))){
                            throw new StopException("视频已失效(返回空url)，建议更换资源");
                        } else if (jmdata.getString("url").endsWith(".html")) {
                            throw new StopException("视频地址已失效，新地址暂无法解析");
                        } else {
                            byte[] decode_url_bytes = AESUtils.decrypt(HexUtils.hex2byte(jmdata.getString("url").getBytes("UTF-8")), "ni po jie ni nb ".getBytes("UTF-8"), "ni po jie ni nb ".getBytes("UTF-8"), "AES/CBC/PKCS7Padding");
                            if(decode_url_bytes == null){
                                throw new StopException("解析失败，密钥失效，请稍后重试！");
                            }
                            String decode_url = new String(decode_url_bytes, "UTF-8");
                            if(decode_url.contains("%3A%2F%")){
                                decode_url = Utils.URLDecode(decode_url);
                            }
                            if(decode_url.contains("/playurl/")){
                                String referer = result.getFinal_URL();
                                result = null;
                                while (result == null || !result.isSign()) {
                                    result = Constants.ParseHelper.getHsu().doGetBody(decode_url, referer, headers, "UTF-8");
                                }
                                return new ParseResult("jo_string://"+result.getResult().trim(), referer, headers);
                            } else {
                                return new ParseResult(decode_url, result.getFinal_URL(), null);
                            }
                        }
                    } else if(parseObject.getInteger("Code") == 9){
                        if(jx_result.getFilepath().equals("repeated")){
                            throw new StopException("解析失败，操作过于频繁或需要人机校验，请稍后重试！");
                        } else {
                            jx_result.setFilepath("repeated");
                            try {
                                Thread.sleep(2000);
                            } catch(Exception ex){}
                            return VIPParseHelper.parse_quanmingjiexi(jx_result, callback);
                        }
                    } else {
                        throw new StopException("解析失败，解析通道需要重新分析，请联系JO！");
                    }
                }
            } catch (Exception ex){
                throw new StopException(String.format(video_parse_exception_tips, result.getResult()));
            }
        } catch (Throwable ex) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(bos));
            throw new StopException(String.format(video_parse_exception_tips, bos.toString()));
        }
    }

    public static ParseResult parse_quanmingjiexi(HttpResult jx_result, Callback callback) throws Exception {
        String Domain = Utils.getString(jx_result.getResult(), "var\\s+Domain\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Api = Utils.getString(jx_result.getResult(), "var\\s+Api\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Host = Domain;
        Document doc = Jsoup.parse(jx_result.getResult());
        String content_id = doc.getElementsByAttributeValue("http-equiv", "Content-Type").attr("id");
        String viewport_id = doc.getElementsByAttributeValue("name", "viewport").attr("id").substring("viewport".length());
        /*
        String Type = Utils.getString(jx_result.getResult(), "var\\s+Type\\s*=\\s*[\"|']+([\\w-_/+=.]*)[\"|']+;");
        String Url = Utils.getString(jx_result.getResult(), "var\\s+Url\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Online = Utils.getString(jx_result.getResult(), "var\\s+Online\\s*=\\s*(\\d+);");
        String Ather = Utils.getString(jx_result.getResult(), "var\\s+Ather\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Dplayer = Utils.getString(jx_result.getResult(), "var\\s+Dplayer\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Autoh5 = Utils.getString(jx_result.getResult(), "var\\s+Autoh5\\s*=\\s*[\"|']+(.*?)[\"|']+;");*/
        String Time = Utils.getString(jx_result.getResult(), "var\\s+Time\\s*=\\s*\\s*[\"|']+(\\d+)\\s*[\"|']+;");
        String Version = Utils.getString(jx_result.getResult(), "var\\s+Version\\s*=\\s*[\"|']+(.*?)[\"|']+;");
        String Vurl = Utils.getString(jx_result.getResult(), "var\\s+Vurl\\s*=\\s*[\"|']+([\\w-_/+=.]+)[\"|']+;");
        String Vkey = Utils.getString(jx_result.getResult(), "var\\s+Vkey\\s*=\\s*[\"|']+([\\w-_/+=.]+)[\"|']+;");
        String Key = Utils.getString(jx_result.getResult(), "var\\s+Key\\s*=\\s*[\"|']+([\\w-_/+=.]+)[\"|']+;");
        String Ref = Utils.getString(jx_result.getResult(), "var\\s+Ref\\s*=\\s*[\"|']+([\\w-_/+=.]*)[\"|']+;");

        String Sign_Key = Utils.getString(jx_result.getResult(), "var\\s+Sign\\s*=.*?,'(.*?)'\\);");
        //String Token_Key = Utils.getString(jx_result.getResult(), "var\\s+Token\\s*=.*?,Time\\+'(.*?)'\\);");

        //var Sign = encode_url(lc($.md5(Host+Time+Vurl+Key)),'7498a23c161cac597eab715591d4cf15');
        //var Token = encode_url(lc($.md5(Domain+Time+Vurl+Sign)),Time+'e20aad2750b17ebdc4719ac49913295d');
        if(!Utils.isEmpty(Sign_Key)){
            try {
                callback.call(60, jx_result.getFinal_URL());
                String lc_key = "17325841932717338791732584194271733878";
                String Sign_Md5_Key = MD5Util.getStringMD5(Host+Domain+Api+Ref+"false"+"false"+Time+Version+Vurl+Vkey+Key);//Domain+Host+Time+Vurl+Key+Key1
                String Sign_lc_Key = MD5Util.getStringMD5(lc_key + Sign_Md5_Key);
                String Sign = AuthCodeUtil.authcode(Sign_lc_Key, Sign_Key, AuthCodeUtil.AuthcodeMode.Encode, 0);

                byte[] genToken = AESUtils.encrypt(Sign.getBytes("UTF-8"), MD5Util.getStringMD5(Vkey).getBytes("UTF-8"), 256, 128, "AES/ECB/PKCS7Padding");
                String Token = XXTEA.encryptToBase64String(Sign, HexUtils.byte2hex(Arrays.copyOfRange(genToken, 16, genToken.length)).toLowerCase());

                String Access_Token0 = Vkey + "-" + Key + "-" + Sign + "-" + Token;
                String Access_Token1 = XXTEA.encryptToBase64String(Access_Token0, Host+Domain+Time);

                Constants.ParseHelper.getHsu().setCookie("uuid", Access_Token0, Host);
                Constants.ParseHelper.getHsu().setCookie("bt_cookie", Vkey, Host);

                String url = Api + "/Api.php";

                LinkedHashMap<String,String> post_params = new LinkedHashMap<String,String>();
                post_params.put("url", Vurl);
                post_params.put("wap", "0");
                post_params.put("ios", "0");
                post_params.put("host", Host);
                post_params.put("referer", Ref);
                post_params.put("time", Time);
                post_params.put("key", Key);
                post_params.put("sign", Sign);
                post_params.put("token", Token);

                String md5_AccessToken1 = MD5Util.getStringMD5(Access_Token1);
                String cdata = new String(org.bouncycastle.util.encoders.Base64.encode(JSON.toJSONString(post_params).getBytes("UTF-8")),"UTF-8");
                byte[] ckey = AESUtils.encrypt(cdata.getBytes("UTF-8"), md5_AccessToken1.substring(0,16).getBytes("ISO-8859-1"), md5_AccessToken1.substring(16).getBytes("ISO-8859-1"), "AES/CBC/PKCS7Padding");
                post_params.put("ckey", "110#"+new String(org.bouncycastle.util.encoders.Base64.encode(org.bouncycastle.util.encoders.Base64.encode(ckey))));
                String Access_Token2 = xx_encode(Time + MD5Util.getStringMD5(Key) + MD5Util.getStringMD5(Sign) + MD5Util.getStringMD5(Token) + MD5Util.getStringMD5(post_params.get("ckey")));

                String jx_url = Utils.resolveURI(jx_result.getFinal_URL().split("\\?")[0], url);
                callback.call(80, jx_url);
                HashMap headers = new HashMap();
                headers.put("sec-ch-ua", "\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"");
                headers.put("sec-ch-ua-mobile", "?0");
                headers.put("upgrade-insecure-requests", "1");
                headers.put("accept", "application/json, text/javascript, */*; q=0.01");
                headers.put("x-requested-with", "XMLHttpRequest");
                headers.put("sec-fetch-site", "cross-site");
                headers.put("sec-fetch-mode", "navigate");
                headers.put("sec-fetch-dest", "iframe");
                headers.put("accept-encoding", "gzip, deflate, br");
                headers.put("accept-language", "zh-CN,zh;q=0.9");
                headers.put("sec-ch-ua-platform", "Windows");

                URI jx_uri = URI.create(jx_url);
                if(jx_uri.getPort() == 443 || jx_uri.getPort() == 80){
                    headers.put("Origin", jx_uri.getScheme() + "://" + jx_uri.getHost());
                } else {
                    headers.put("Origin", jx_uri.getScheme() + "://" + jx_uri.getHost()+":"+jx_uri.getPort());
                }
                headers.put("accept-encoding", "gzip, deflate, br");
                headers.put("accept-language", "zh-CN,zh;q=0.9");
                headers.put("content-type", "application/x-www-form-urlencoded");
                headers.put("connection", "keep-alive");
                headers.put("md5", MD5Util.getStringMD5(Access_Token0 + Access_Token1 + Access_Token2));
                headers.put("access-token0", Access_Token0);
                headers.put("access-token1", Access_Token1);
                headers.put("access-token2", Access_Token2);
                headers.put("vkey", Vkey);
                headers.put("version", Version);

                long timestamp = (Long.parseLong(Time) * 0x400);
                String appkey = MD5Util.getStringMD5(Domain + timestamp + Version);
                HttpResult result = null;
                while (result == null || !result.isSign()) {
                    result = Constants.ParseHelper.getHsu().doPostBody(jx_url+"?ver="+ Version +"&timestamp="+ timestamp +"&appkey=" + appkey, post_params, jx_result.getFinal_URL(), headers, "UTF-8");
                    if(result.indexOf(true,"title>检测中","location.href")){
                        String location_href = Utils.getString(result.getResult(),"location.href\\s*=\\s*\"(.*?)\";");
                               location_href = Utils.resolveURI(result.getFinal_URL(),location_href);
                        callback.call(90, location_href);
                        result = Constants.ParseHelper.getHsu().doPostBody(location_href, post_params, jx_result.getFinal_URL(), headers, "UTF-8");
                    }
                    if (!result.isSign()) {
                        if (result.indexOf(false, "403")) {
                            throw new StopException("解析播放地址失败【403】");
                        }
                    }
                }
                try {
                    JSONObject parseObject = JSONObject.parseObject(result.getResult());
                    if (Utils.isEmpty(parseObject.getString("Data"))) {
                        throw new StopException("视频已失效(返回空url)，建议更换资源");
                    } else if (parseObject.getString("Data").endsWith(".html")) {
                        throw new StopException("视频地址已失效，新地址暂无法解析");
                    } else {
                        if(parseObject.getInteger("Code") == 10){
                            String decode_key = MD5Util.getStringMD5(parseObject.getString("Code") + content_id + viewport_id + parseObject.getString("Appkey") + parseObject.getString("Version"));
                            byte[] decode_bytes = AESUtils.decrypt(org.bouncycastle.util.encoders.Base64.decode(parseObject.getString("Data")), decode_key.substring(0,16).getBytes("ISO-8859-1"), decode_key.substring(16).getBytes("ISO-8859-1"), "AES/CBC/PKCS7Padding");
                            JSONObject jmdata = JSONObject.parseObject(new String(decode_bytes,"UTF-8"));
                            if(Utils.isEmpty(jmdata.getString("url"))){
                                throw new StopException("视频已失效(返回空url)，建议更换资源");
                            } else if (jmdata.getString("url").endsWith(".html")) {
                                throw new StopException("视频地址已失效，新地址暂无法解析");
                            } else {
                                String url_decode_key = MD5Util.getStringMD5(Host + Token);
                                String encode_url = jmdata.getString("url").replace("-", "+").replace("_", "/").replace(".", "=");
                                String decode_url = AuthCodeUtil.authcode(encode_url,url_decode_key, AuthCodeUtil.AuthcodeMode.Decode, 0);
                                if(decode_url.contains("%3A%2F%")){
                                    decode_url = Utils.URLDecode(decode_url);
                                }
                                if(decode_url.contains("/playurl/")){
                                    String referer = result.getFinal_URL();
                                    result = null;
                                    while (result == null || !result.isSign()) {
                                        result = Constants.ParseHelper.getHsu().doGetBody(decode_url, referer, headers, "UTF-8");
                                    }
                                    return new ParseResult("jo_string://"+result.getResult().trim(), referer, headers);
                                } else {
                                    return new ParseResult(decode_url, result.getFinal_URL(), null);
                                }
                            }
                        } else if(parseObject.getInteger("Code") == 9){
                            if(jx_result.getFilepath().equals("repeated")){
                                throw new StopException("解析失败，操作过于频繁或需要人机校验，请稍后重试！");
                            } else {
                                jx_result.setFilepath("repeated");
                                try {
                                    Thread.sleep(2000);
                                } catch(Exception ex){}
                                return VIPParseHelper.parse_quanmingjiexi(jx_result, callback);
                            }
                        } else {
                            throw new StopException("解析失败，解析通道需要重新分析，请联系JO！");
                        }
                    }
                } catch (Exception ex){
                    throw new StopException(String.format(video_parse_exception_tips, result.getResult()));
                }
            } catch (Throwable ex) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ex.printStackTrace(new PrintStream(bos));
                throw new StopException(String.format(video_parse_exception_tips, bos.toString()));
            }
        } else {
            throw new StopException(String.format(video_parse_exception_tips, jx_result.getResult()));
        }
    }

    private static String xx_encode(String data){
        StringBuffer result = new StringBuffer();
        //String xx_keys = "PXhw7UT1B0a9kQDKZsjIASmOezxYG5CHo5Jyfg2b8FLpEvRr3WtVnlqMidu6cN";
        String xx_keys = "PXhw7UT1B0a9kQDKZsjIASmOezxYG4CHo5Jyfg2b8FLpEvRr3WtVnlqMidu6cN";
        for (int i = 0; i < data.length(); i++){
            int IllI1i = xx_keys.indexOf(data.charAt(i)+"");
            char i11I1l = data.charAt(i);
            if (IllI1i > -1) {
                i11I1l = xx_keys.charAt((IllI1i + 3) % 0x3e);
            }
            result.append(xx_keys.charAt((int)Math.floor(Math.random()*0x3e)));
            result.append(i11I1l);
            result.append(xx_keys.charAt((int)Math.floor(Math.random()*0x3e)));
        }
        return result.toString();
    }

    private static String TSTKC(String time,String Key,String key1,String sign1,String token1){
        char _0x2b4fcf = time.charAt(2);
        char _0x41bf5d = Key.charAt(6);
        char _0x39e717 = key1.charAt(2);
        char _0x493354 = sign1.charAt(sign1.length()-4);

        StringBuffer result = new StringBuffer();
        for (int i = 0x0; 0x2*i < token1.length(); i++) {
            result.append(token1.charAt(token1.length()-i - 1));
            if(i < token1.length()-i-1){
                result.append(token1.charAt(i));
            }
            switch (i) {
                case 0x1:
                    result.append(_0x2b4fcf);
                    break;
                case 0x2:
                    result.append(_0x41bf5d);
                    break;
                case 0x3:
                    result.append(_0x39e717);
                    break;
                case 0x4:
                    result.append(_0x493354);
            }
        }
        return result.toString();
    }

    public static String getRealIp() {
        HttpResult result = null;
        while (result == null || !result.isSign()) {
            result = Constants.ParseHelper.getHsu().doGetBody("https://pv.sohu.com/cityjson?ie=utf-8", null, null, "GB2312");
        }
        String string = Utils.getString(result.getResult(), "(\\d+\\.\\d+\\.\\d+\\.\\d+)");
        return Utils.isEmpty(string) ? "175.10.000.000" : string.split("\\.")[0] + "." + string.split("\\.")[1] + ".000.000";
    }

    public static String hxm_randstr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    public static String hxm_encrypt(String hxm_salt, String plainText){
        hxm_salt = MD5Util.getStringMD5(hxm_salt).toLowerCase();
        StringBuffer result = new StringBuffer("A");
        int salt_m = 7;
        int salt_i = 0;
        for(int i = 0; i < plainText.length(); i++){
            salt_i %= salt_m;
            char s = hxm_salt.charAt(salt_i);
            int rindex = hxm_randstr.indexOf(plainText.charAt(i));
            result.append(hxm_randstr.charAt((s + rindex) % 64));
            salt_i++;

        }
        return result.toString();
    }

    public static String hxm_decrypt(String hxm_salt, String cipherText){
        hxm_salt = MD5Util.getStringMD5(hxm_salt).toLowerCase();
        StringBuffer result = new StringBuffer();
        int salt_m = 7;
        int salt_i = 0;
        for(int i = 0; i < cipherText.length(); i++){
            salt_i %= salt_m;
            char s = hxm_salt.charAt(salt_i);
            int dc = hxm_randstr.indexOf(cipherText.charAt(i)) - s;
            while(dc < 0){
                dc += 64;
            }
            result.append(hxm_randstr.charAt(dc));
            salt_i++;

        }
        return result.toString();
    }
}
