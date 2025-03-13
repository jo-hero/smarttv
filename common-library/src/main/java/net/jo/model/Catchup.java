package net.jo.model;

import android.net.Uri;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 回看參數
 *         "days": "7",
 *         "type": "append",
 *         "regex": "/PLTV/",
 *         "source": "?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}"
 */
public class Catchup {

    private String type;
    private String days;
    private String regex;
    private String source;

    public static Catchup PLTV() {
        Catchup item = new Catchup();
        item.setDays("7");
        item.setType("append");
        item.setRegex("/PLTV/");
        item.setSource("?playseek=${(b)yyyyMMddHHmmss}-${(e)yyyyMMddHHmmss}");
        return item;
    }

    public static Catchup create() {
        return new Catchup();
    }

    public static Catchup decide(Catchup major, Catchup minor) {
        if (!major.isEmpty()) return major;
        if (!minor.isEmpty()) return minor;
        return null;
    }

    public String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDays() {
        return TextUtils.isEmpty(days) ? "" : days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getRegex() {
        return TextUtils.isEmpty(regex) ? "" : regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getSource() {
        return TextUtils.isEmpty(source) ? "" : source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean match(String url) {
        return url.contains(getRegex()) || Pattern.compile(getRegex()).matcher(url).find();
    }

    public boolean isEmpty() {
        return getSource().isEmpty();
    }

    private boolean isAppend() {
        return getType().equals("append");
    }

    private boolean isDefault() {
        return getType().equals("default");
    }

    private String format(String url, String result) {
        if (!TextUtils.isEmpty(Uri.parse(url).getQuery())) result = result.replace("?", "&");
        if (url.contains("/PLTV/")) url = url.replace("/PLTV/", "/TVOD/");
        return url + result;
    }

    public String format(String url, LiveEpgItem data) {
        String result = getSource();
        if (data.isInRange()) return url;
        Matcher matcher = Pattern.compile("(\\$\\{[^}]*\\})").matcher(result);
        while (matcher.find()) result = result.replace(matcher.group(1), data.format(matcher.group(1)));
        return isDefault() ? result : format(url, result);
    }
}