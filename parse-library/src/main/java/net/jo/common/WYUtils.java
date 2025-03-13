package net.jo.common;

import com.alibaba.fastjson.JSONObject;
import net.jo.http.HttpResult;
import net.jo.http.HttpSimpleUtils;

import net.jo.common.MD5Util;
import net.jo.common.Utils;
import net.jo.common.base64.Base64;
import net.jo.common.des.Des;
import net.jo.http.HttpResult;
import net.jo.http.HttpSimpleUtils;

import org.jsoup.Jsoup;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WYUtils {
    public static String genKey(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        int i = 0;
        while (i < str.length()) {
            int i2 = i + 1;
            String substring = str.substring(i, i2);
            String encode = URLEncoder.encode(substring);
            if (encode.equals(substring)) {
                String str2 = "00" + Integer.toHexString(substring.charAt(0));
                encode = str2.substring(str2.length() - 2);
            }
            stringBuffer.append(encode);
            i = i2;
        }
        return stringBuffer.toString().replace("%", "").toUpperCase();
    }

    public static String decript_url(String str, String str2) {
        byte[] bytes = Base64.decode(str2, "UTF-8").getBytes();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            stringBuffer.append((char) (bytes[i] ^ str.charAt(i % str.length())));
        }
        return Base64.decode(stringBuffer.toString(), "UTF-8");
    }

    public static String safe_addr(String str) {
        return decript_url("0548aaead4ba4bdd769ccb60c25d033a", str);
    }

    public static String lequgirl(JSONObject jSONObject) {
        String string = jSONObject.getString("url");
        try {
            if (!jSONObject.containsKey("utd") || string.startsWith("http")) {
                return string;
            }
            if ((System.currentTimeMillis() / 1000) - ((long) Integer.parseInt(safe_addr(jSONObject.getString("utd")))) >= 7200) {
                return "";
            }
            return URLDecoder.decode(decript_url("02a9c6b6168b25322194eadbf2f39033", string), "UTF-8");
        } catch (Exception unused) {
            return string;
        }
    }

    public static String sign(String str) {
        String stringMD5 = MD5Util.getStringMD5(str + "!abef987");
        StringBuffer stringBuffer = new StringBuffer("ab59");
        stringBuffer.append(stringMD5.substring(24, 30));
        stringBuffer.append("8ab5d6");
        stringBuffer.append(stringMD5.substring(10, 22));
        stringBuffer.append("loij");
        return stringBuffer.toString();
    }

    public static String base64_code(String str, String str2) {
        int[] iArr = new int[256];
        for (int i = 0; i < 256; i++) {
            iArr[i] = i;
        }
        int i2 = 0;
        for (int i3 = 0; i3 < 256; i3++) {
            i2 = ((i2 + iArr[i3]) + str2.charAt(i3 % str2.length())) % 256;
            int i4 = iArr[i3];
            iArr[i3] = iArr[i2];
            iArr[i2] = i4;
        }
        String str3 = "";
        int i5 = 0;
        int i6 = 0;
        for (int i7 = 0; i7 < str.length(); i7++) {
            i5 = (i5 + 1) % 256;
            i6 = (i6 + iArr[i5]) % 256;
            int i8 = iArr[i5];
            iArr[i5] = iArr[i6];
            iArr[i6] = i8;
            str3 = str3 + ((char) (str.charAt(i7) ^ iArr[(iArr[i5] + iArr[i6]) % 256]));
        }
        return str3;
    }

    public static String base64_encode(String data) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i < data.length();) {
            int i2 = i + 1;
            int charAt = data.charAt(i) & 255;
            if (i2 == data.length()) {
                sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(charAt >> 2));
                sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((charAt & 3) << 4));
                sb.append("==");
                break;
            }
            int i3 = i2 + 1;
            char charAt2 = data.charAt(i2);
            if (i3 == data.length()) {
                sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(charAt >> 2));
                sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(((charAt & 3) << 4) | ((charAt2 & 240) >> 4)));
                sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((charAt2 & 15) << 2));
                sb.append('=');
                break;
            }
            int i4 = i3 + 1;
            char charAt3 = data.charAt(i3);
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(charAt >> 2));
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(((charAt & 3) << 4) | ((charAt2 & 240) >> 4)));
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(((charAt2 & 15) << 2) | ((charAt3 & 192) >> 6)));
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(charAt3 & '?'));
            i = i4;
        }
        return sb.toString();
    }

    public static String getRealIp(HttpSimpleUtils httpSimpleUtils) {
        HttpResult httpResult = null;
        while (true) {
            if (httpResult != null && httpResult.isSign()) {
                break;
            }
            httpResult = httpSimpleUtils.doGetBody("https://data.video.iqiyi.com/v.f4v", null, null, "GB2312");
        }
        String string = Utils.getString(httpResult.getResult(), "(\\d+\\.\\d+\\.\\d+\\.\\d+)");
        return Utils.isEmpty(string) ? "192.168.2.199" : string;
    }

    public static String getDesnSuffix() {
        HttpSimpleUtils httpSimpleUtils = new HttpSimpleUtils();
        httpSimpleUtils.setConnectionTime(5000);
        httpSimpleUtils.setSoTime(5000);
        try {
            return Jsoup.parse(JSONObject.parseObject(httpSimpleUtils.doGetBody("http://note.youdao.com/yws/public/note/fc0d957c7a4be6a74ed7d38efbed5c9b", "http://note.youdao.com/share/?id=fc0d957c7a4be6a74ed7d38efbed5c9b&type=note", null, "UTF-8").getResult()).getString("content")).text();
        } catch (Exception unused) {
            return "fanteee";
        }
    }

    public static JSONObject parse(HttpResult jx_result) {
        String result = jx_result.getResult();
        if (result.contains("y.encode(other_l)")) {
            //String fuck_js_script = Utils.getString(result.getResult(), "tipstime\\(\\d+\\);\\s*(.*?)\\s*skin=");
            //String eval = JSUtils.eval(string10 + ";fuck;");
            //Date date = new Date();
            //String base64_encode2 = base64_encode(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            //String k1 = Jsoup.parse(result).getElementById("k1").val();
            //String genKey = genKey(k1);
            //String realIp = getRealIp(Constants.HSU);
            //String strEnc = new Des().strEnc(host, id, time, "(@#$!#~)");
            //String domain = Utils.getString(result, "domain\\s*=\\s*'(.*?)';");
            //String id = Utils.getString(result, "id':'(\\w+)',");
            //String time = (date.getTime() / 1000) + "";
            //String base64_encode3 = base64_encode(base64_code(host, time));
            //String base64_encode4 = base64_encode(base64_code(MD5Util.getStringMD5(domain + time), time));

            URI create = URI.create(jx_result.getFinal_URL());
            String url = create.getQuery().replace("url=", "");
            if (Utils.isEmpty(url)) {
                url = "kkkk";
            }
            String base64_encode = base64_encode(url);
            result = result.replaceAll(":\\s*form,", ":'',").replace("y.encode(other_l)", "'" + base64_encode + "'");
        } else if (result.contains("sign($('#hdMd5').val())")) {
            String string4 = Utils.getString(jx_result.getResult(), "eval\\(\"\\\\x(.*?)\"\\);");
            if (!Utils.isEmpty(string4)) {
                String string5 = Utils.getString(Utils.decode_ox("\\x" + string4), "val\\('(\\w+)'\\);");
                result = result.replace("sign($('#hdMd5').val())", "'" + sign(string5) + "'");
            }
        } else if (result.contains("desn($('#hdMd5').val())")) {
            String string6 = Utils.getString(jx_result.getResult(), "eval\\(\"\\\\x(.*?)\"\\);");
            if (!Utils.isEmpty(string6)) {
                String string7 = Utils.getString(Utils.decode_ox("\\x" + string6), "val\\('(\\w+)'\\);");
                String desnSuffix = getDesnSuffix();
                StringBuilder sb = new StringBuilder();
                sb.append("'");
                sb.append(MD5Util.getStringMD5(string7 + desnSuffix));
                sb.append("'");
                result = result.replace("desn($('#hdMd5').val())", sb.toString());
            }
        } else if (result.contains("md5\": $('#hdMd5').val()")) {
            String string8 = Utils.getString(jx_result.getResult(), "eval\\(\"\\\\x(.*?)\"\\);");
            result = result.replaceAll(":\\s*iqiyicip", ":'" + getRealIp(new HttpSimpleUtils()) + "',");
            if (!Utils.isEmpty(string8)) {
                result = result.replace("$('#hdMd5').val()", "'" + Utils.getString(Utils.decode_ox("\\x" + string8), "val\\('(\\w+)'\\);") + "'");
            }
        } else {
            URI create2 = URI.create(jx_result.getFinal_URL());
            String host2 = create2.getHost();
            String replace3 = create2.getQuery().replace("url=", "");
            if (Utils.isEmpty(replace3)) {
                replace3 = "kkkk";
            }
            String base64_encode5 = base64_encode(replace3);
            String realIp2 = getRealIp(new HttpSimpleUtils());
            String val2 = Jsoup.parse(result).getElementById("k1").val();
            String string13 = Utils.getString(result, "domain\\s*=\\s*'(.*?)';");
            String string14 = Utils.getString(result, "tipstime\\(\\d+\\);\\s*(.*?)\\s*skin=");
            String string15 = Utils.getString(result, "id':'(\\w+)',");
            String string16 = Utils.getString(result, "url':'(.*?)',");
            String genKey2 = genKey(val2);
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date2 = new Date();
            String base64_encode6 = base64_encode(simpleDateFormat2.format(date2));
            String str2 = (date2.getTime() / 1000) + "";
            String strEnc2 = new Des().strEnc(host2, string15, str2, "(@#$!#~)");
            String base64_encode7 = base64_encode(base64_code(host2, str2));
            String base64_encode8 = base64_encode(base64_code(MD5Util.getStringMD5(string13 + str2), str2));
            //String eval3 = JSUtils.eval(string14 + ";fuck;");
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("url", (Object) string16);
            jSONObject.put("referer", (Object) base64_encode(jx_result.getFinal_URL()));
            jSONObject.put("ref", (Object) "1");
            jSONObject.put("time", (Object) val2);
            jSONObject.put("type", (Object) "");
            jSONObject.put("other", (Object) base64_encode5);
            jSONObject.put("gen", (Object) genKey2);
            jSONObject.put("t", (Object) realIp2);
            jSONObject.put("times", (Object) base64_encode6);
            jSONObject.put("uuid", (Object) strEnc2);
            jSONObject.put("tip", (Object) str2);
            jSONObject.put("tips", (Object) base64_encode8);
            jSONObject.put("k1", (Object) base64_encode7);
            jSONObject.put("fuck", "");
            //jSONObject.put("fuck", (Object) eval3);
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("params", (Object) jSONObject.toString());
            jSONObject2.put("api", (Object) "api.php");
            return jSONObject2;
        }
        Matcher matcher = Pattern.compile("\\$\\.post\\(\"(.*?)\",\\s*\\{(.*?)\\},\\s*function\\(", 34).matcher(result);
        if (!matcher.find()) {
            return null;
        }
        try {
            JSONObject parse_result = new JSONObject();
            String group = matcher.group(1);
            parse_result.put("params", (Object) JSONObject.parseObject("{" + matcher.group(2) + "}").toString());
            parse_result.put("api", (Object) group);
            return parse_result;
        } catch (Exception e) {
            return null;
        }
    }

    public static ArrayList<String> QQParse(String str) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String encode = URLEncoder.encode(new HttpSimpleUtils().getUSER_AGENT(), "UTF-8");
            Map<String, String> Translate2Map = Translate2Map(str.split("\\?")[1]);
            String encode2 = URLEncoder.encode(Translate2Map.get("url"), "UTF-8");
            arrayList.add(Translate2Map.get("play") + "?ver=4");
            arrayList.add("https://tj.video.qq.com/fcgi-bin/set_cookie?lv_irt_id=&dm=v.qq.com&ua=" + encode + "&r=" + System.currentTimeMillis() + "&vid=" + Translate2Map.get("vid") + "&sr=1366x768&ul=zh-CN&tv=0.0.7&pt=%E8%85%BE%E8%AE%AF%E8%A7%86%E9%A2%91&guid=" + Translate2Map.get("guid") + "&url=" + encode2 + "&from=" + encode2 + "&playing_url=");
            arrayList.add("https://btrace.video.qq.com/kvcollect?BossId=4501&Pwd=142347456&loginid=&loginex=&logintype=0&guid=" + Translate2Map.get("guid") + "&longitude=&latitude=&vip=&online=1&p2p=1&downloadkit=0&resolution=1366*768*1&testid=&osver=windows+6.1&playerver=&playertype=1&uip=&confid=&cdnip=&cdnid=&cdnuip=&freetype=&sstrength=&network=&speed=&device=&appver=3.5.51&p2pver=&url=" + Translate2Map.get("url") + "&refer=&ua=" + encode + "&ptag=&flowid=" + Translate2Map.get("pid") + "_10201&platform=10201&dltype=3&vid=" + Translate2Map.get("vid") + "&fmt=&rate=&clip=&status=&type=&duration=&ext=%7B%22dltype%22%3A3%2C%22m3u8%22%3A0%7D&data=%7B%22quic%22%3Afalse%2C%22stime%22%3A" + System.currentTimeMillis() + "%2C%22etime%22%3A" + System.currentTimeMillis() + "%2C%22code%22%3A%22%22%7D&step=5&seq=0");
            arrayList.add("https://btrace.video.qq.com/kvcollect?BossId=4298&Pwd=686148428&uin=&vid=" + Translate2Map.get("vid") + "&coverid=&pid=" + Translate2Map.get("pid") + "&guid=" + Translate2Map.get("guid") + "&cmid=" + Translate2Map.get("guid") + "&unid=" + Translate2Map.get("guid") + "&vt=&type=&url=" + Translate2Map.get("url") + "&bi=&bt=&version=3.5.51&platform=10201&format=&defn=&ctime=" + format + "&ptag=&isvip=-1&tpid=&pversion=chromehls&hc_uin=&hc_main_login=&hc_vuserid=&hc_openid=&hc_appid=&hc_pvid=&hc_ssid=&hc_qq=&ua=" + encode + "&hh_ua=" + encode + "&ckey=&iformat=&hh_ref=" + Translate2Map.get("url") + "&vurl=&v_idx=0&rcd_info=&extrainfo=&step=3&val=1&idx=0&c_info=&pcplatform=web&diagonal=1374&isfocustab=1&isvisible=1&cpay=0&tpay=0&dltype=3");
            arrayList.add("https://btrace.video.qq.com/kvcollect?BossId=4298&Pwd=686148428&uin=&vid=" + Translate2Map.get("vid") + "&coverid=&pid=" + Translate2Map.get("pid") + "&guid=" + Translate2Map.get("guid") + "&cmid=" + Translate2Map.get("guid") + "&unid=" + Translate2Map.get("guid") + "&vt=&type=&url=" + Translate2Map.get("url") + "&bi=45&bt=&version=3.5.51&platform=10201&format=&defn=&ctime=" + format + "&ptag=&isvip=-1&tpid=&pversion=chromehls&hc_uin=&hc_main_login=&hc_vuserid=&hc_openid=&hc_appid=&hc_pvid=&hc_ssid=&hc_qq=&ua=" + encode + "&hh_ua=" + encode + "&ckey=&iformat=&hh_ref=" + Translate2Map.get("url") + "&vurl=&v_idx=0&rcd_info=&extrainfo=&step=5&val=50000&val1=1&idx=0&diagonal=1374&isfocustab=1&isvisible=1&c_info=&pcplatform=web&cpay=0&tpay=0&dltype=3");
            StringBuilder sb = new StringBuilder();
            sb.append("https://btrace.video.qq.com/kvcollect?BossId=6072&Pwd=1133018508&flowid=");
            sb.append(Translate2Map.get("pid"));
            sb.append("_10201&vid=");
            sb.append(Translate2Map.get("vid"));
            sb.append("&type=LD&step=1&step_duration=254&step_code=0&step_index=1&version=old.115&platform=1&firstview_duration=1046&client_version=&ext_info=actionType%3Ainfo%3BretryTimes%3A1%3Bdomain%3Avd.l.qq.com%3Bexistblock%3A1&req_from=v_qq_com&play_type=&page_first_load=1&url=");
            sb.append(Translate2Map.get("url"));
            sb.append("&browser=chrome&browser_ver=63.0.3239.132");
            arrayList.add(sb.toString());
            arrayList.add("https://btrace.video.qq.com/kvcollect?BossId=4564&Pwd=213967996&flowid=" + Translate2Map.get("pid") + "_10201&data=%7B%22stime%22%3A" + System.currentTimeMillis() + "%2C%22etime%22%3A" + System.currentTimeMillis() + "%2C%22code%22%3A%22%22%2C%22ip%22%3A%22" + Translate2Map.get("ip") + "%22%2C%22url%22%3A%22%2F%2Fvd.l.qq.com/proxyhttp/vinfoad%26charge%3D0%26defaultfmt%3Dauto%26otype%3Dojson%26guid%3D" + Translate2Map.get("guid") + "%26flowid%3D" + Translate2Map.get("pid") + "_10201%26platform%3D10201%26sdtfrom%3Dv1104%26defnpayver%3D1%26appVer%3D3.5.51%26host%3Dv.qq.com%26refer%3D" + Translate2Map.get("url") + "%26ehost%3Dhttp://v.qq.com/%26sphttps%3D1%26tm%3D1536329116%26spwm%3D4%26vid%3Dp0027mtywmj%26defn%3Dshd%26unid%3D" + Translate2Map.get("pid") + "%26fhdswitch%3D1%26onlyGetinfo%3Dtrue%26show1080p%3D1%26isHLS%3D1%26dtype%3D3%26sphls%3D2%26spgzip%3D1%26dlver%3D2%26defsrc%3D2%26encryptVer%3D7.5%26cKey%3Dce99084f76db3417fdded5e0339c6116%22%7D");
            arrayList.add("https://btrace.video.qq.com/kvcollect?BossId=4501&Pwd=142347456&loginid=&loginex=&logintype=0&guid=" + Translate2Map.get("guid") + "&longitude=&latitude=&vip=&online=1&p2p=1&downloadkit=0&resolution=1366*768*1&testid=7&osver=windows+6.1&playerver=&playertype=1&uip=" + Translate2Map.get("ip") + "&confid=&cdnip=&cdnid=2803&cdnuip=&freetype=&sstrength=&network=&speed=&device=&appver=3.5.51&p2pver=&url=" + Translate2Map.get("url") + "&refer=&ua=" + encode + "&ptag=&flowid=" + Translate2Map.get("pid") + "_10201&platform=10201&dltype=8&vid=" + Translate2Map.get("vid") + "&fmt=&rate=77&clip=10&status=2&type=1036&duration=2784.13&ext=%7B%22dltype%22%3A8%2C%22m3u8%22%3A1%7D&data=%7B%22ip%22%3A%22" + Translate2Map.get("ip") + "%22%2C%22quic%22%3Afalse%2C%22stime%22%3A" + format + "%2C%22etime%22%3A" + format + "%2C%22code%22%3A%22%22%7D&step=15&seq=1");
            StringBuilder sb2 = new StringBuilder();
            sb2.append("https://btrace.video.qq.com/kvcollect?BossId=2594&Pwd=0&gid=");
            sb2.append(Translate2Map.get("pid"));
            sb2.append("&plt=10201&uin=&vid=");
            sb2.append(Translate2Map.get("vid"));
            sb2.append("&cts=");
            sb2.append(System.currentTimeMillis());
            sb2.append("&sdt=undefined&fne=");
            sb2.append(Translate2Map.get("vid"));
            sb2.append(".321003.ts&dip=175.6.26.16&cdn=2803&vky=");
            arrayList.add(sb2.toString());
            arrayList.add("https://btrace.video.qq.com/kvcollect?BossId=4298&Pwd=686148428&uin=&vid=" + Translate2Map.get("vid") + "&coverid=&pid=" + Translate2Map.get("pid") + "&guid=" + Translate2Map.get("guid") + "&cmid=" + Translate2Map.get("guid") + "&unid=" + Translate2Map.get("guid") + "&vt=0&type=1036&url=" + Translate2Map.get("url") + "&bi=2784&bt=2784&version=3.5.51&platform=10201&format=&defn=hd&ctime=" + format + "&ptag=&isvip=-1&tpid=2&pversion=chromehls&hc_uin=&hc_main_login=&hc_vuserid=&hc_openid=&hc_appid=&hc_pvid=&hc_ssid=&hc_qq=&ua=" + encode + "&hh_ua=" + encode + "&ckey=&iformat=&hh_ref=" + Translate2Map.get("url") + "&vurl=&v_idx=0&rcd_info=&extrainfo=&step=1011&val1=1&val2=0&val=293&cpay=0&tpay=0&dltype=3");
            arrayList.add("https://btrace.video.qq.com/kvcollect?BossId=4298&Pwd=686148428&uin=&vid=" + Translate2Map.get("vid") + "&coverid=&pid=" + Translate2Map.get("pid") + "&guid=" + Translate2Map.get("guid") + "&cmid=" + Translate2Map.get("guid") + "&unid=" + Translate2Map.get("guid") + "&vt=2803&type=1036&url=" + Translate2Map.get("url") + "&bi=2&bt=2784&version=3.5.51&platform=10201&format=&defn=hd&ctime=" + format + "&ptag=&isvip=-1&tpid=2&pversion=chromehls&hc_uin=&hc_main_login=&hc_vuserid=&hc_openid=&hc_appid=&hc_pvid=&hc_ssid=&hc_qq=&ua=" + encode + "&hh_ua=" + encode + "&ckey=&iformat=&hh_ref=" + Translate2Map.get("url") + "&vurl=&v_idx=0&rcd_info=&extrainfo=&step=4&val=1&val1=1&idx=0&c_info=&pcplatform=web&diagonal=1374&isfocustab=1&isvisible=1&cpay=0&tpay=0&dltype=3");
            StringBuilder sb3 = new StringBuilder();
            sb3.append("https://btrace.video.qq.com/kvcollect?BossId=4501&Pwd=142347456&loginid=&loginex=&logintype=0&guid=");
            sb3.append(Translate2Map.get("guid"));
            sb3.append("&longitude=&latitude=&vip=&online=1&p2p=1&downloadkit=0&resolution=1366*768*1&testid=7&osver=windows+6.1&playerver=&playertype=1&uip=");
            sb3.append(Translate2Map.get("ip"));
            sb3.append("&confid=&cdnip=&cdnid=2803&cdnuip=&freetype=&sstrength=&network=&speed=&device=&appver=3.5.51&p2pver=&url=");
            sb3.append(Translate2Map.get("url"));
            sb3.append("&refer=&ua=");
            sb3.append(encode);
            sb3.append("&ptag=&flowid=");
            sb3.append(Translate2Map.get("pid"));
            sb3.append("_10201&platform=10201&dltype=8&vid=");
            sb3.append(Translate2Map.get("url"));
            sb3.append("&fmt=&rate=77&clip=10&status=2&type=1036&duration=2784.13&ext=%7B%22dltype%22%3A8%2C%22m3u8%22%3A1%7D&data=%7B%22stime%22%3A");
            sb3.append(System.currentTimeMillis());
            sb3.append("%2C%22etime%22%3A");
            sb3.append(System.currentTimeMillis());
            sb3.append("%2C%22p2p_ctime%22%3A0%2C%22p2p_pretime%22%3A0%2C%22bufferduration%22%3A%22%22%2C%22vt%22%3A2803%2C%22url%22%3A%22");
            sb3.append(Translate2Map.get("play"));
            sb3.append("?ver=4%22%2C%22urlindex%22%3A0%2C%22quic%22%3Afalse%2C%22code%22%3A%22%22%7D&step=30&seq=2");
            arrayList.add(sb3.toString());
            arrayList.add("https://btrace.video.qq.com/kvcollect?BossId=4298&Pwd=686148428&uin=&vid=" + Translate2Map.get("vid") + "&coverid=&pid=" + Translate2Map.get("pid") + "&guid=" + Translate2Map.get("guid") + "&cmid=" + Translate2Map.get("guid") + "&unid=" + Translate2Map.get("guid") + "&vt=2803&type=1036&url=" + Translate2Map.get("url") + "&bi=&bt=&version=3.5.51&platform=10201&format=&defn=hd&ctime=" + format + "&ptag=&isvip=-1&tpid=2&pversion=chromehls&hc_uin=&hc_main_login=&hc_vuserid=&hc_openid=&hc_appid=&hc_pvid=&hc_ssid=&hc_qq=&ua=" + encode + "&hh_ua=" + encode + "&ckey=&iformat=&hh_ref=" + Translate2Map.get("url") + "&vurl=" + Translate2Map.get("play") + "?ver=4&v_idx=0&rcd_info=&extrainfo=&step=6&val=581&val1=1&val2=1&idx=0&c_info=&pcplatform=web&diagonal=1374&isfocustab=1&isvisible=1&cpay=0&tpay=0&dltype=3");
            arrayList.add("https://btrace.video.qq.com/kvcollect?BossId=4298&Pwd=686148428&uin=&vid=" + Translate2Map.get("vid") + "&coverid=&pid=" + Translate2Map.get("pid") + "&guid=" + Translate2Map.get("guid") + "&cmid=" + Translate2Map.get("guid") + "&unid=" + Translate2Map.get("guid") + "&vt=2803&type=1036&url=" + Translate2Map.get("url") + "&bi=2784&bt=2784&version=3.5.51&platform=10201&format=&defn=hd&ctime=" + format + "&ptag=&isvip=-1&tpid=2&pversion=chromehls&hc_uin=&hc_main_login=&hc_vuserid=&hc_openid=&hc_appid=&hc_pvid=&hc_ssid=&hc_qq=&ua=" + encode + "&hh_ua=" + encode + "&ckey=&iformat=&hh_ref=" + Translate2Map.get("url") + "&vurl=" + Translate2Map.get("play") + "?ver=4&v_idx=0&rcd_info=&extrainfo=&step=35&val=608&val1=&val2=0&cpay=0&tpay=0&dltype=3");
            arrayList.add("https://btrace.video.qq.com/kvcollect?BossId=4298&Pwd=686148428&uin=&vid=" + Translate2Map.get("vid") + "&coverid=&pid=" + Translate2Map.get("pid") + "&guid=" + Translate2Map.get("guid") + "&cmid=" + Translate2Map.get("guid") + "&unid=" + Translate2Map.get("guid") + "&vt=2803&type=1036&url=" + Translate2Map.get("url") + "&bi=2784&bt=2784&version=3.5.51&platform=10201&format=&defn=hd&ctime=" + format + "&ptag=&isvip=-1&tpid=2&pversion=chromehls&hc_uin=&hc_main_login=&hc_vuserid=&hc_openid=&hc_appid=&hc_pvid=&hc_ssid=&hc_qq=&ua=" + encode + "&hh_ua=" + encode + "&ckey=&iformat=&hh_ref=" + Translate2Map.get("url") + "&vurl=" + Translate2Map.get("play") + "?ver=4&v_idx=0&rcd_info=&extrainfo=&step=30&val=591&val1=0&val2=3&cpay=0&tpay=0&dltype=3");
            StringBuilder sb4 = new StringBuilder();
            sb4.append("https://btrace.video.qq.com/kvcollect?BossId=3717&Pwd=1055758521&version=3.5.51&uid=");
            sb4.append(Translate2Map.get("guid"));
            sb4.append("&pid=");
            sb4.append(Translate2Map.get("pid"));
            sb4.append("&vid=");
            sb4.append(Translate2Map.get("vid"));
            sb4.append("&player_type=chromehls&video_type=1&platform=10201&usr_action=mute-toggle&usr_action_detail=&url=");
            sb4.append(Translate2Map.get("url"));
            sb4.append("");
            arrayList.add(sb4.toString());
        } catch (Exception unused) {
        }
        return arrayList;
    }

    public static Map<String, String> Translate2Map(String str) {
        HashMap hashMap = new HashMap();
        if (str != null && !str.trim().equals("")) {
            String[] split = str.split("&");
            for (int i = 0; i < split.length; i++) {
                int indexOf = split[i].indexOf("=");
                if (indexOf != -1) {
                    hashMap.put(split[i].substring(0, indexOf), split[i].substring(indexOf + 1));
                }
            }
        }
        return hashMap;
    }
}
