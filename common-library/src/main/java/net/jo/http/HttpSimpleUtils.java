package net.jo.http;

import android.util.Base64;

import com.sonalb.net.http.Header;
import com.sonalb.net.http.cookie.Client;
import com.sonalb.net.http.cookie.Cookie;
import com.sonalb.net.http.cookie.CookieJar;
import com.sonalb.net.http.cookie.CookieMatcher;

import net.jo.common.MD5Util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class HttpSimpleUtils {
    private CookieJar Cookies = new CookieJar();
    private String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36";
    private Client client = new Client();
    private boolean followRedirects = true;
    private int so_timeout = 30000;
    private int connection_timeout = 30000;
    private String proxy_host = null;
    private int proxy_port = 0;
    private String proxy_pwd = null;
    private String proxy_un = null;

    static {
        try {
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new X509TrustManager[]{new SSLTrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(new Tls12SocketFactory(sslContext.getSocketFactory()));
        } catch (GeneralSecurityException e) {}
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String str, SSLSession sSLSession) {
                return true;
            }
        });
    }

    public void setConHeader(String method, HttpURLConnection conn, String referer, Map<String, String> request_headers) throws Exception {
        conn.setRequestMethod(method);
        conn.setReadTimeout(getSoTime());
        conn.setConnectTimeout(this.connection_timeout);
        conn.setRequestProperty("User-Agent", this.USER_AGENT);
        if (referer != null) {
            conn.setRequestProperty("Referer", referer);
        }
        conn.setDoInput(true);
        if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) {
            conn.setDoOutput(true);
        }
        conn.setUseCaches(false);
        this.client.setCookies(conn, this.Cookies);
        if (request_headers != null) {
            for (String str3 : request_headers.keySet()) {
                conn.setRequestProperty(str3, request_headers.get(str3));
            }
        }
    }

    public HttpURLConnection init(String url) throws Exception {
        URL serverUrl = new URL(url);
        HttpURLConnection conn = null;
        if (this.proxy_host != null && this.proxy_host.length()>0) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxy_host, this.proxy_port));
            conn = (HttpURLConnection) serverUrl.openConnection(proxy);
            if (this.proxy_un != null && this.proxy_un.length()>0) {
                conn.setRequestProperty("Proxy-Authorization", "Basic "+ Base64.encode((this.proxy_un + ":" + this.proxy_pwd).getBytes("UTF-8"), 0));
            }
        } else {
            conn = (HttpURLConnection) serverUrl.openConnection();
        }
        HttpURLConnection.setFollowRedirects(false);
        HttpsURLConnection.setFollowRedirects(false);
        return conn;
    }

    public HttpResult processReturn(String url, String referer, HttpURLConnection conn,Map<String,String> headers, String... other_sets) {
        try {
            this.Cookies.addAll(this.client.getCookies(conn));
        } catch (Exception ex){}
        try {
            String location = getLocation(conn);
            int status_code = conn.getResponseCode();
            if(status_code == 307){
                return this.formatResult("重定向错误码-307(Location: "+location+")",false, url, conn);
            }
            if(isRedirect(status_code) && location != null){
                try {
                    URI currentUri  = URI.create(url);
                    URI redirectUri = URI.create(location);
                    if (!redirectUri.isAbsolute()) {
                        redirectUri = currentUri.resolve(redirectUri);
                    }
                    return this.doGetBody(redirectUri.toString(), url,headers, other_sets);
                } catch (Exception ex) {
                    return this.doGetBody(location, url,headers, other_sets);
                }
            } else if(conn.getResponseCode() == 200){
                return getResponse(conn,true, other_sets);
            } else {
                return getResponse(conn, false, other_sets);
            }
        } catch (Exception e) {
            return this.formatResult(e.getMessage(), false, url, conn);
        } finally {
            conn.disconnect();
        }
    }

    public HttpResult getResponse(HttpURLConnection conn, boolean sign, String... other_sets) {
        HttpResult result = this.formatResult(null, sign, conn.getURL().toString(), conn);

        InputStream ins = null;
        ByteArrayOutputStream bos = null;
        try {
            if(conn.getResponseCode() == 200){
                ins = conn.getInputStream();
            } else {
                ins = conn.getErrorStream();
            }
            if ((conn.getContentEncoding() != null) && (conn.getContentEncoding().toLowerCase().contains("gzip"))){
                ins = new GZIPInputStream(conn.getInputStream());
            }

            bos = new ByteArrayOutputStream();
            byte[] bArr = new byte[1024];
            int read = -1;
            while ((read = ins.read(bArr))!= -1) {
                bos.write(bArr, 0, read);
            }
            result.setBytesResult(bos.toByteArray());

            if (other_sets.length > 1) {
                String save_filename = other_sets[1];
                FileOutputStream fos = new FileOutputStream(save_filename);
                try {
                    bos.writeTo(fos);
                    result.setResult(MD5Util.getFileMD5String(new File(save_filename)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (Exception e) {}
                }
            } else {
                String charset = other_sets[0];
                result.setResult(bos.toString(charset));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {}
            try {
                ins.close();
            } catch (Exception e) {}
            try {
                bos.close();
            } catch (Exception e) {}
        }
        return result;
    }

    private HttpResult formatResult(String content, boolean sign, String url, HttpURLConnection conn) {
        try {
            Header response_headers = new Header();
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            Set<String> keys = headerFields.keySet();
            for (String key : keys) {
                List<String> header_values = headerFields.get(key);
                for(String header_value : header_values){
                    if(key != null){
                        response_headers.add(key, header_value);
                    } else {
                        response_headers.add("", header_value);
                    }
                }
            }
            return new HttpResult(content,sign,url,response_headers);
        } catch (Exception ex){
            return new HttpResult(content,sign,url,null);
        }
    }

    private boolean isRedirect(int status) {
        return this.getFollowRedirects() && (status == 301 || status == 302 || status == 303 || status == 307);
    }

    public HttpResult doGetBody(String url, String reffer, Map<String, String> headers, String... other_sets) {
        try {
            HttpURLConnection conn = init(url);
            setConHeader("GET", conn, reffer, headers);
            conn.connect();
            return processReturn(url, reffer, conn, headers, other_sets);
        } catch (Exception e) {e.printStackTrace();
            return formatResult(e.getLocalizedMessage(), false, url, null);
        }
    }

    public HttpResult doPostBody(String url, String params, String referer, Map<String, String> headers, String... other_sets) {
        try {
            HttpURLConnection conn = init(url);
            setConHeader("POST", conn, referer, headers);
            // 写参数
            if(params == null){
                params = "";
            }
            conn.connect();
            conn.getOutputStream().write(params.getBytes(other_sets[0]));
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            return processReturn(url, referer, conn, headers, other_sets);
        } catch (Exception e2) {
            return formatResult(e2.getLocalizedMessage(), false, url, null);
        }
    }

    public HttpResult doPostBody(String url, Map<String, String> params, String reffer, Map<String, String> headers, String... other_sets) {
        try {
            HttpURLConnection conn = init(url);
            setConHeader("POST", conn, reffer, headers);
            StringBuffer stringBuffer = new StringBuffer();
            if (params != null) {
                for (String str3 : params.keySet()) {
                    try {
                        if (stringBuffer.length() > 0) {
                            stringBuffer.append("&");
                        }
                        stringBuffer.append(URLEncoder.encode(str3, other_sets[0]));
                        stringBuffer.append("=");
                        stringBuffer.append(URLEncoder.encode(params.get(str3), other_sets[0]));
                    } catch (Exception e) {
                    }
                }
            }
            conn.connect();
            conn.getOutputStream().write(stringBuffer.toString().getBytes(other_sets[0]));
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            return processReturn(url, reffer, conn, headers, other_sets);
        } catch (Exception e2) {
            return formatResult(e2.getLocalizedMessage(), false, url, null);
        }
    }

    public HttpResult doPostBody_Json(String url, String jsonString, String reffer, Map<String, String> headers, String... other_sets) {
        try {
            HttpURLConnection conn = init(url);
            setConHeader("POST", conn, reffer, headers);
            conn.connect();
            conn.getOutputStream().write(jsonString.toString().getBytes(other_sets[0]));
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            return processReturn(url, reffer, conn, headers, other_sets);
        } catch (Exception e2) {
            return formatResult(e2.getLocalizedMessage(), false, url, null);
        }
    }

    public HttpResult doPostFiles(String url, String str2, String str3, List<File> list, Map<String, String> map, String str4, Map<String, String> map2, String... other_sets) {
        try {
            HttpURLConnection conn = init(url);
            setConHeader("POST", conn, str4, map2);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "---------7d4a6d158c9");
            conn.connect();
            DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
            byte[] bytes = ("\r\n--" + "---------7d4a6d158c9" + "--\r\n").getBytes();
            if (map != null) {
                for (String str5 : map.keySet()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("--");
                    sb.append("---------7d4a6d158c9");
                    sb.append("\r\n");
                    sb.append("Content-Disposition: form-data;name=\"" + str5 + "\";\r\n");
                    sb.append("Content-Type: text/plain; charset=" + other_sets[0] + "\r\n\r\n");
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(map.get(str5));
                    sb2.append("\r\n");
                    sb.append(sb2.toString());
                    dataOutputStream.write(sb.toString().getBytes());
                }
            }
            for (File file : list) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("--");
                sb3.append("---------7d4a6d158c9");
                sb3.append("\r\n");
                sb3.append("Content-Disposition: form-data;name=\"" + str3 + "\";filename=\"" + file.getName() + "\"\r\n");
                sb3.append("Content-Type:application/octet-stream\r\n\r\n");
                dataOutputStream.write(sb3.toString().getBytes());
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = dataInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    dataOutputStream.write(bArr, 0, read);
                }
                dataOutputStream.write("\r\n".getBytes());
                dataInputStream.close();
            }
            dataOutputStream.write(bytes);
            dataOutputStream.flush();
            dataOutputStream.close();
            return processReturn(url, str2, conn, map2, other_sets);
        } catch (Exception e) {
            return formatResult(e.getLocalizedMessage(), false, url, null);
        }
    }

    public HttpResult doPutBody(String url, String params, String reffer, Map<String, String> headers, String... other_sets) {
        try {
            HttpURLConnection conn = init(url);
            setConHeader("PUT", conn, reffer, headers);
            if (params == null) {
                params = "";
            }
            conn.connect();
            conn.getOutputStream().write(params.getBytes(other_sets[0]));
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            return processReturn(url, reffer, conn, headers, other_sets);
        } catch (Exception e) {
            return formatResult(e.getLocalizedMessage(), false, url, null);
        }
    }

    public HttpResult doHeadBody(String url, String reffer, Map<String, String> headers, String... other_sets) {
        try {
            HttpURLConnection conn = init(url);
            setConHeader("HEAD", conn, reffer, headers);
            conn.connect();
            return processReturn(url, reffer, conn, headers, other_sets);
        } catch (Exception e) {
            return formatResult(e.getLocalizedMessage(), false, url, null);
        }
    }

    public HttpResult getFile(String url, String file_name, String str3, Map<String, String> headers) {
        return doGetBody(url, str3, headers, "UTF-8", file_name);
    }

    public HttpResult getFile(String str, Map<String, String> map, String str2, String str3, Map<String, String> headers) {
        return doPostBody(str, map, str3, headers, "UTF-8", str2);
    }

    public String getLocation(HttpURLConnection conn) {
        String location = null;
        Set<String> keys = conn.getHeaderFields().keySet();
        for(String key : keys){
            if(key != null && key.equalsIgnoreCase("location")){
                for (String nextLocation : (List<String>) conn.getHeaderFields().get(key)) {
                    location = nextLocation;
                }
                break;
            }
        }
        return location;
    }

    public byte[] readFromInput(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int len = 0; (len = inStream.read(buffer)) != -1;)
            outStream.write(buffer, 0, len);
        inStream.close();
        return outStream.toByteArray();
    }

    public void copyCookie(String domain) {
        CookieJar cj = this.Cookies.getCookies(new CookieMatcher() {
            public boolean doMatch(Cookie cookie) {
                return true;
            }
        });
        Iterator<Cookie> it = cj.iterator();
        CookieJar domainCJ = new CookieJar();
        while (it.hasNext()) {
            try {
                Cookie cookie = (Cookie) it.next().clone();
                cookie.setDomain(domain);
                domainCJ.add(cookie);
            } catch (Exception e) {
            }
        }
        this.Cookies.addAll(domainCJ);
    }

    public boolean delCookies(final String name, final String domain) {
        this.Cookies.removeCookies(new CookieMatcher() {
            @Override
            public boolean doMatch(Cookie cookie) {
                return cookie.getName().equals(name) && cookie.getDomain().contains(domain);
            }
        });
        return true;
    }

    public String getCookies(String name, String value) {
        Iterator it = this.Cookies.getCookies(name).iterator();
        while (it.hasNext()) {
            Cookie cookie = (Cookie) it.next();
            if (cookie.getDomain().contains(value)) {
                return cookie.getName() + "=" + cookie.getValue();
            }
        }
        return null;
    }

    public Cookie[] getCookies() {
        CookieJar cookies = this.Cookies.getCookies(new CookieMatcher() {
            @Override
            public boolean doMatch(Cookie cookie) {
                return true;
            }
        });
        ArrayList arrayList = new ArrayList();
        Iterator it = cookies.iterator();
        while (it.hasNext()) {
            arrayList.add((Cookie) it.next());
        }
        return (Cookie[]) arrayList.toArray(new Cookie[0]);
    }

    public String getCookiesValue(String name, String domain) {
        Iterator it = this.Cookies.getCookies(name).iterator();
        while (it.hasNext()) {
            Cookie cookie = (Cookie) it.next();
            if (cookie.getDomain().contains(domain)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String setCookie(String name, String value, String domain) {
        try {
            Date d = new Date();
            d.setTime(d.getTime() + 9000000000l);
            Cookie mstuid = new Cookie(name, value, domain, "/");
            mstuid.setVersion("0");
            mstuid.setExpires(d);
            this.Cookies.add(mstuid);
            return mstuid.toString();
        } catch (Exception e) {}
        return null;
    }

    public void setProxy(String str, int i) {
        this.proxy_host = str;
        this.proxy_port = i;
    }

    private boolean testProxy(String url, String keyword) {
        try {
            HttpResult result = this.doGetBody(url, null,null, "UTF-8");
            if (result.indexOf(false, keyword)) {
                return true;
            }
        } catch (Exception e) {}
        this.proxy_host = null;
        return false;
    }

    public boolean setProxyTest(String host, int port, String url, String key) {
        this.setProxy(host, port);
        return testProxy(url, key);
    }

    public boolean setProxyTest(String host, int port, String un, String pwd, String url, String key) {
        this.proxy_host = host;
        this.proxy_port = port;
        this.proxy_un = un;
        this.proxy_pwd = pwd;
        return testProxy(url, key);
    }

    public String getUSER_AGENT() {
        return this.USER_AGENT;
    }

    public boolean getFollowRedirects() {
        return this.followRedirects;
    }

    public void setFollowRedirects(boolean z) {
        this.followRedirects = z;
    }

    public int getConnectionTime() {
        return this.connection_timeout;
    }

    public void setConnectionTime(int i) {
        this.connection_timeout = i;
    }

    public int getSoTime() {
        return this.so_timeout;
    }

    public void setSoTime(int i) {
        this.so_timeout = i;
    }

    public void setUSER_AGENT(String str) {
        this.USER_AGENT = str;
    }
}