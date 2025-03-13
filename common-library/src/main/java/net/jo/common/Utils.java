package net.jo.common;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Utils {
    public static JSONObject getParseLibsModuleList(){
        if(Constants.PARSE_LIBS_CONFIGS.containsKey("modules")){
            return Constants.PARSE_LIBS_CONFIGS.getJSONObject("modules");
        } else {
            return Constants.PARSE_LIBS_CONFIGS;
        }
    }

    public static long format(SimpleDateFormat format, String src) {
        try {
            return format.parse(src).getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    public static String format(SimpleDateFormat format, long time) {
        try {
            return format.format(time);
        } catch (Exception e) {
            return "";
        }
    }

    public static Map<String, String> TranslateString2Map(String params) {
        Map<String, String> map = new HashMap<String, String>();
        if ((params != null) && (!params.trim().equals(""))) {
            String[] _params = params.split("&");
            for (int i = 0; i < _params.length; i++) {
                int _i = _params[i].indexOf("=");
                if (_i != -1) {
                    String name = _params[i].substring(0, _i);
                    String value = _params[i].substring(_i + 1);
                    map.put(name, value);
                }
            }
        }
        return map;
    }

    public static String URLDecode(String url) {
        if (url.toLowerCase().startsWith("http%3a%2f%2f") || url.toLowerCase().startsWith("https%3a%2f%2f")) {
            try {
                return URLDecoder.decode(url, "UTF-8");
            } catch (Exception e) {}
        }
        return url;
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if ((obj instanceof String) && obj.toString().trim().equals("")) {
            return true;
        }
        if ((obj instanceof Number) && ((Number) obj).doubleValue() == 0.0d) {
            return true;
        }
        if ((obj instanceof Collection) && ((Collection) obj).isEmpty()) {
            return true;
        }
        if ((obj instanceof Map) && ((Map) obj).isEmpty()) {
            return true;
        }
        if (!(obj instanceof Object[]) || ((Object[]) obj).length != 0) {
            return false;
        }
        return true;
    }

    public static String resolveURI(String source_url, String url) {
        if (url.startsWith("//")) {
            if(source_url.startsWith("https")){
                return "https:" + url;
            } else {
                return "http:" + url;
            }
        }
        String replaceAll = url.replaceAll("http://\\w+\\.\\w+\\.\\w+/\\w+/http", "").replaceAll("https://\\w+\\.\\w+\\.\\w+/\\w+/http", "");
        URI create = URI.create(source_url);
        URI create2 = URI.create(replaceAll);
        if (!create2.isAbsolute()) {
            create2 = create.resolve(create2);
        }
        return create2.toString();
    }

    public static String getString(String str, String str2) {
        Matcher matcher = Pattern.compile(str2, 34).matcher(str.trim());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    public static String getMemInfo(){
        int M = 1024*1024;
        Runtime r = Runtime.getRuntime();
        StringBuilder sb = new StringBuilder();
        sb.append("Max:" + r.maxMemory() / M + "M,");
        sb.append("Total:" + r.totalMemory() / M + "M,");
        sb.append("Cost:" + (r.totalMemory() - r.freeMemory()) / M + "M,");
        return sb.toString();
    }

    public static String decode_ox(String str) {
        byte[] bytes = str.getBytes();
        ByteBuffer allocate = ByteBuffer.allocate(bytes.length);
        int i = 0;
        while (i < bytes.length) {
            if (bytes[i] == 92 && bytes[i + 1] == 120) {
                StringBuilder sb = new StringBuilder();
                sb.append((char) bytes[i + 2]);
                sb.append("");
                i += 3;
                sb.append((char) bytes[i]);
                allocate.put(Integer.valueOf(Integer.valueOf(sb.toString(), 16).intValue() & 255).byteValue());
            } else {
                allocate.put(bytes[i]);
            }
            i++;
        }
        return new String(allocate.array()).trim();
    }

    public static String encode_ox(String str) {
        byte[] bytes = "\\x".getBytes();
        ByteBuffer allocate = ByteBuffer.allocate(str.length() * 24);
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt < 0 || charAt > 127) {
                for (byte b : String.valueOf(charAt).getBytes()) {
                    String upperCase = Integer.toHexString(b & 255).toUpperCase();
                    allocate.put(bytes);
                    allocate.put(upperCase.getBytes());
                }
            } else {
                allocate.put(Integer.valueOf(charAt).byteValue());
            }
        }
        return new String(allocate.array()).trim();
    }

    /**
     *
     * @param data
     * @param key 928395479
     * @param flag 1是解密 0是加密
     * @return
     * @throws Exception
     */
    public static String doCipher(String data,String key,int flag)throws Exception{
        if (flag == 1) {
            data = atob(data,"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
        }
        int[] padding_keys = new int[256];
        int[] padding_ivs = new int[256];
        for (int i = 0; i < 256; i++) {
            padding_keys[i] = key.charAt(i % key.length());
            padding_ivs[i] = i;
        }

        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + padding_ivs[i] + padding_keys[i]) % 0x100;
            int tmp = padding_ivs[i];
            padding_ivs[i] = padding_ivs[j];
            padding_ivs[j] = tmp;
        }

        StringBuffer final_cipher_data = new StringBuffer();
        int a = 0;
        j = 0;
        for (int i = 0; i < data.length(); i++) {
            a = (a + 1) % 256;
            j = (j + padding_ivs[a]) % 256;
            int tmp = padding_ivs[a];
            padding_ivs[a] = padding_ivs[j];
            padding_ivs[j] = tmp;
            int k = padding_ivs[(padding_ivs[a] + padding_ivs[j]) % 0x100];
            final_cipher_data.append((char) (data.charAt(i) ^ k));
        }
        if (flag == 1) {
            return URLDecoder.decode(final_cipher_data.toString(), "UTF-8");
        } else {
            return btoa(final_cipher_data.toString(),"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
        }
    }

    public static String btoa(String inStr,String base64hash) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        int mod = 0;
        int ascii;
        int prev = 0;
        while (i < inStr.length()) {
            ascii = inStr.charAt(i);
            mod = i % 3;
            switch (mod) {
                case 0:
                    result.append(String.valueOf(base64hash.charAt(ascii >> 2)));
                    break;
                case 1:
                    result.append(String.valueOf(base64hash.charAt((prev & 3) << 4 | (ascii >> 4))));
                    break;
                case 2:
                    result.append(String.valueOf(base64hash.charAt((prev & 0x0f) << 2 | (ascii >> 6))));
                    result.append(String.valueOf(base64hash.charAt(ascii & 0x3f)));
                    break;

            }
            prev = ascii;
            i++;
        }

        if (mod == 0) {
            result.append(String.valueOf(base64hash.charAt((prev & 3) << 4)));
            result.append("==");
        } else if (mod == 1) {
            result.append(String.valueOf(base64hash.charAt((prev & 0x0f) << 2)));
            result.append("=");
        }
        return result.toString();
    }

    public static String atob(String inStr,String base64hash) {
        if (inStr == null) return null;
        inStr = inStr.replaceAll("\\s|=", "");
        StringBuilder result = new StringBuilder();
        int cur;
        int prev = -1;
        int mod;
        int i = 0;
        while (i < inStr.length()) {
            cur = base64hash.indexOf(inStr.charAt(i));
            mod = i % 4;
            switch (mod) {
                case 0:
                    break;
                case 1:
                    result.append(String.valueOf((char) (prev << 2 | cur >> 4)));
                    break;
                case 2:
                    result.append(String.valueOf((char) ((prev & 0x0f) << 4 | cur >> 2)));
                    break;
                case 3:
                    result.append(String.valueOf((char) ((prev & 3) << 6 | cur)));
                    break;
            }
            prev = cur;
            i++;
        }
        return result.toString();
    }

    public static void save(File cache_file, byte[] buf)
    {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(cache_file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static String getDownloadCachePath(Context context) {
        String string = context.getSharedPreferences("sharePCachePath", 0).getString("cachePath", "");
        if (string.length() > 0) {
            return new File(string).getAbsolutePath();
        }
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "DataSuperPlayerCache").getAbsolutePath();
    }

    public static boolean fileIsExists(String str) {
        try {
            return new File(str).exists();
        } catch (Exception unused) {
            return false;
        }
    }

    public static Integer getKeyByValue(Map<Integer, String> map, Object obj) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            String value = entry.getValue();
            if (value != null && value.equals(obj)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public static String getFileNameExtension(String str) {
        int lastIndexOf;
        if (TextUtils.isEmpty(str) || (lastIndexOf = str.lastIndexOf(".")) < 0) {
            return null;
        }
        return str.substring(lastIndexOf + 1);
    }

    public static String covertStreamToString(InputStream inputStream, String str) throws IOException {
        if (inputStream == null) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        byte[] bArr = new byte[4096];
        while (true) {
            int read = inputStream.read(bArr);
            if (read == -1) {
                return stringBuffer.toString();
            }
            if (str == null) {
                stringBuffer.append(new String(bArr, 0, read));
            } else {
                stringBuffer.append(new String(bArr, 0, read, str));
            }
        }
    }

    public static void changeFileMod(String str, String str2) {
        if (!TextUtils.isEmpty(str)) {
            try {
                File file = new File(str);
                if (file.exists()) {
                    String str3 = str2 + " " + file.getAbsolutePath();
                    Log.d("changeFileMod", "path = " + str + "command = " + str3);
                    for (int i = 0; i < 5; i++) {
                        Log.d("changeFileMod", "chmod count = " + i);
                        if (Runtime.getRuntime().exec(str3).waitFor() == 0) {
                            Log.d("changeFileMod", "change mod success.");
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("changeFileMod", "Error changeFileMod()" + e.toString());
                e.printStackTrace();
            }
        }
    }

    public static String getFilePath(String str) {
        int lastIndexOf;
        if (TextUtils.isEmpty(str) || (lastIndexOf = str.lastIndexOf("/")) < 0) {
            return null;
        }
        return str.substring(0, lastIndexOf + 1);
    }

    public static String getFileName(String str) {
        int lastIndexOf;
        if (TextUtils.isEmpty(str) || (lastIndexOf = str.lastIndexOf("/")) < 0) {
            return null;
        }
        return str.substring(lastIndexOf + 1);
    }

    public static boolean containNonEnglishChar(String str) {
        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] > 255 || charArray[i] < 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkFileValid(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        File file = new File(str);
        return file.exists() && file.isFile();
    }

    public static int px2sp(Context context, float f) {
        return (int) ((f / context.getResources().getDisplayMetrics().scaledDensity) + 0.5f);
    }

    public static void covertStreamToFile(InputStream inputStream, File file) throws IOException {
        file.delete();
        if (!file.exists()) {
            file.createNewFile();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            byte[] bArr = new byte[8192];
            while (true) {
                int read = inputStream.read(bArr);
                if (read <= 0) {
                    break;
                }
                bufferedOutputStream.write(bArr, 0, read);
            }
            bufferedOutputStream.close();
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static boolean deleteFile(String str) {
        File file = new File(str);
        if (!file.exists()) {
            return true;
        }
        if (!file.isFile()) {
            return false;
        }
        file.delete();
        return true;
    }

    public static void testForZmon(Context context, String str) {
        try {
            File file = new File(context.getFilesDir().getAbsolutePath() + "/desktop");
            if (!file.exists()) {
                file.mkdirs();
            }
            File file2 = new File(file.getAbsolutePath(), "test.txt");
            if (!file2.exists()) {
                file2.createNewFile();
            } else if (file2.length() >= 10240) {
                file2.delete();
                file2.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2, true)));
            String format = DateFormat.getDateTimeInstance().format(new Date());
            bufferedWriter.write(format + "-------" + str + "\n");
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public static void testForUpdate(Context context, String str) {
        try {
            File file = new File(context.getFilesDir().getAbsolutePath() + "/desktop");
            if (!file.exists()) {
                file.mkdirs();
            }
            File file2 = new File(file.getAbsolutePath(), "test_status.txt");
            if (!file2.exists()) {
                file2.createNewFile();
            } else if (file2.length() >= 10240) {
                file2.delete();
                file2.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2, true)));
            String format = DateFormat.getDateTimeInstance().format(new Date());
            bufferedWriter.write(format + "-------" + str + "\n");
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public static String getMimeType(String str) {
        String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(str);
        if (fileExtensionFromUrl != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensionFromUrl);
        }
        return null;
    }

    public static float dp2px(Context context, float f) {
        return (f * context.getResources().getDisplayMetrics().density) + 0.5f;
    }

    public static float sp2px(Context context, float f) {
        return f * context.getResources().getDisplayMetrics().scaledDensity;
    }

    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    public static <E> ArrayList<E> newArrayList(E... eArr) {
        ArrayList<E> arrayList = new ArrayList<>(((eArr.length * 110) / 100) + 5);
        Collections.addAll(arrayList, eArr);
        return arrayList;
    }

    public static void playKeySound(View view, int i) {
        if (view == null) {
            return;
        }
        if (i == 1) {
            view.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
        } else if (i == 0) {
            view.playSoundEffect(SoundEffectConstants.NAVIGATION_UP);
        }
    }

    public static boolean isConnected(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static long readSystemAvailableSize() {
        StatFs statFs = new StatFs("/cache");
        long blockSize = (long) statFs.getBlockSize();
        Log.d("block size", "block size: " + blockSize);
        long availableBlocks = (long) statFs.getAvailableBlocks();
        Log.d("available count", "available count: " + availableBlocks);
        return (blockSize * availableBlocks) / 1024;
    }

    public static String longToDate(long j) {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Long.valueOf(j));
    }

    public static byte[] readStreamToByteArray(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[524288];
        while (true) {
            int read = inputStream.read(bArr);
            if (read != -1) {
                byteArrayOutputStream.write(bArr, 0, read);
            } else {
                byteArrayOutputStream.close();
                inputStream.close();
                return byteArrayOutputStream.toByteArray();
            }
        }
    }

    public static String getExtName(String str) {
        try {
            String name = new File(str).getName();
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception unused) {
            return "";
        }
    }

    public static String unescape(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.ensureCapacity(str.length());
        int i = 0;
        while (i < str.length()) {
            int indexOf = str.indexOf("%", i);
            if (indexOf == i) {
                int i2 = indexOf + 1;
                if (str.charAt(i2) == 'u') {
                    int i3 = indexOf + 2;
                    indexOf += 6;
                    stringBuffer.append((char) Integer.parseInt(str.substring(i3, indexOf), 16));
                } else {
                    indexOf += 3;
                    stringBuffer.append((char) Integer.parseInt(str.substring(i2, indexOf), 16));
                }
            } else if (indexOf == -1) {
                stringBuffer.append(str.substring(i));
                i = str.length();
            } else {
                stringBuffer.append(str.substring(i, indexOf));
            }
            i = indexOf;
        }
        return stringBuffer.toString();
    }

    public static String getStringMD5(String str) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            messageDigest = null;
        }
        messageDigest.update(str.getBytes());
        return new String(Base64.encode(messageDigest.digest(), 8));
    }

    public static String getCacheFolder(Context context) {
        File file = new File(context.getCacheDir().getAbsolutePath() + File.separator + "app_icons");
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getAbsolutePath();
    }

    public static String base64(String s) {
        return base64(s.getBytes());
    }

    public static String base64(byte[] bytes) {
        return base64(bytes, Base64.DEFAULT | Base64.NO_WRAP);
    }

    public static String base64(String s, int flags) {
        return base64(s.getBytes(), flags);
    }

    public static String base64(byte[] bytes, int flags) {
        return Base64.encodeToString(bytes, flags);
    }

    public static byte[] decode(String s) {
        return decode(s, Base64.DEFAULT | Base64.NO_WRAP);
    }

    public static byte[] decode(String s, int flags) {
        return Base64.decode(s, flags);
    }

    public static String getNowTime() {
        return new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public static String formatTime(int i) {
        int i2 = i / 1000;
        int i3 = i2 % 60;
        int i4 = (i2 / 60) % 60;
        int i5 = i2 / 3600;
        if (i5 > 0) {
            return String.format("%02d:%02d:%02d", Integer.valueOf(i5), Integer.valueOf(i4), Integer.valueOf(i3));
        }
        return String.format("%02d:%02d", Integer.valueOf(i4), Integer.valueOf(i3));
    }

    public static void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch(Exception ex){}
    }

    public static List<String> parseUrls(JSONArray urls){
        List<String> parse_urls = new ArrayList<String>();
        for(int i=0; i<urls.size(); i++){
            parse_urls.addAll(Utils.parseUrls(urls.getString(i)));
        }
        return parse_urls;
    }

    public static List<String> parseUrls(String url){
        List<String> parse_urls = new ArrayList<String>();
        String rand_size = Utils.getString(url, "\\{(\\d+\\-\\d+)\\}");
        if(Utils.isEmpty(rand_size)){
            parse_urls.add(url);
        } else {
            int start_index = Integer.parseInt(rand_size.split("-")[0]);
            int end_index = Integer.parseInt(rand_size.split("-")[1]);
            for(int j=start_index;j<=end_index;j++){
                parse_urls.add(url.replace("{"+rand_size+"}", j+""));
            }
        }
        return parse_urls;
    }

    public static String getWifiLocalIp(Context context){
        WifiManager wm=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        //检查Wifi状态
        if(!wm.isWifiEnabled()) wm.setWifiEnabled(true);
        WifiInfo wi=wm.getConnectionInfo();
        //获取32位整型IP地址
        int ipAdd=wi.getIpAddress();
        //把整型地址转换成“*.*.*.*”地址
        return (ipAdd & 0xFF ) + "." + ((ipAdd >> 8 ) & 0xFF) + "." + ((ipAdd >> 16 ) & 0xFF) + "." + (ipAdd >> 24 & 0xFF);
    }

    //String codestring：要生成二维码的字符串
    // int width：二维码图片的宽度
    // int height：二维码图片的高度
    public static Bitmap createQRCode(String codestring, int width, int height){
        try {
            //首先判断参数的合法性，要求字符串内容不能为空或图片长宽必须大于0
            if (TextUtils.isEmpty(codestring)||width<=0||height<=0){
                return null;
            }
            //设置二维码的相关参数，生成BitMatrix（位矩阵）对象
            Hashtable<EncodeHintType,String> hashtable=new Hashtable<>();
            hashtable.put(EncodeHintType.CHARACTER_SET,"utf-8");  //设置字符转码格式
            hashtable.put(EncodeHintType.ERROR_CORRECTION,"H");   //设置容错级别
            hashtable.put(EncodeHintType.MARGIN,"2"); //设置空白边距
            //encode需要抛出和处理异常
            BitMatrix bitMatrix=new QRCodeWriter().encode(codestring, BarcodeFormat.QR_CODE,width,height,hashtable);
            //再创建像素数组，并根据位矩阵为数组元素赋颜色值
            int[] pixel=new int[width*width];
            for (int h=0;h<height;h++){
                for (int w=0;w<width;w++){
                    if (bitMatrix.get(w,h)){
                        pixel[h*width+w]= Color.BLACK;  //设置黑色色块
                    }else{
                        pixel[h*width+w]=Color.WHITE;  //设置白色色块
                    }
                }
            }
            //创建bitmap对象
            //根据像素数组设置Bitmap每个像素点的颜色值，之后返回Bitmap对象
            Bitmap qrcodemap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
            qrcodemap.setPixels(pixel,0,width,0,0,width,height);
            return qrcodemap;
        }catch (WriterException e){
            return null;
        }
    }
    private static final Pattern snifferMatch = Pattern.compile("http((?!http).){26,}?\\.(m3u8|mp4)\\?.*|http((?!http).){26,}\\.(m3u8|mp4)|http((?!http).){26,}?/m3u8\\?pt=m3u8.*|http((?!http).)*?default\\.ixigua\\.com/.*|http((?!http).)*?cdn-tos[^\\?]*|http((?!http).)*?/obj/tos[^\\?]*|http.*?/player/m3u8play\\.php\\?url=.*|http.*?/player/.*?[pP]lay\\.php\\?url=.*|http.*?/playlist/m3u8/\\?vid=.*|http.*?\\.php\\?type=m3u8&.*|http.*?/download.aspx\\?.*|http.*?/api/up_api.php\\?.*|https.*?\\.66yk\\.cn.*|http((?!http).)*?netease\\.com/file/.*");

    /**
     * 是否支持画中画
     * @return
     */
    public static boolean supportsPiPMode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * 是否支持触摸
     * @param context
     * @return
     */
    public static boolean supportsTouch(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
    }

    public static int getTextWidth(String content, int size) {
        Paint paint = new Paint();
        paint.setTextSize(sp2px(Constants.APP, size));
        return (int) paint.measureText(content);
    }

    /**
     * zip解压
     * @param bytes
     * @return
     */
    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static String uncompressToString(byte[] bytes, String encoding) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString(encoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
