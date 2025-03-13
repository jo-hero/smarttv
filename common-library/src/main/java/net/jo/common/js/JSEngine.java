package net.jo.common.js;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.sonalb.net.http.HeaderEntry;

import net.jo.common.Constants;
import net.jo.http.HttpResult;
import net.jo.http.HttpSimpleUtils;
import com.github.tvbox.quickjs.JSArray;
import com.github.tvbox.quickjs.JSCallFunction;
import com.github.tvbox.quickjs.JSModule;
import com.github.tvbox.quickjs.JSObject;
import com.github.tvbox.quickjs.QuickJSContext;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JSEngine {
    private static final String TAG = "JSEngine";

    static JSEngine instance = null;

   public JSEngine(){
        System.loadLibrary("quickjs");
    }

    public static JSEngine getInstance() {
        if (instance == null) {
            instance = new JSEngine();
        }
        return instance;
    }

    public class JSThread {
        private QuickJSContext jsContext;
        private Handler handler;
        private Thread thread;
        private volatile byte retain;
        private HttpSimpleUtils hsu;

        public JSThread(HttpSimpleUtils hsu){
            this.hsu = hsu;
        }

        public QuickJSContext getJsContext() {
            return jsContext;
        }

        public JSObject getGlobalObj() {
            return jsContext.getGlobalObject();
        }

        public <T> T post(final Event<T> event) throws Throwable {
            if ((thread != null && thread.isInterrupted())) {
                Log.e("QuickJS", "QuickJS is released");
                return null;
            }
            if (Thread.currentThread() == thread) {
                return event.run(jsContext, getGlobalObj());
            }
            if (handler == null) {
                return event.run(jsContext, getGlobalObj());
            }
            final Object[] result = new Object[2];
            final RuntimeException[] errors = new RuntimeException[1];
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        result[0] = event.run(jsContext, getGlobalObj());
                    } catch (RuntimeException e) {
                        errors[0] = e;
                    }
                    synchronized (result) {
                        result[1] = true;
                        result.notifyAll();
                    }
                }
            });
            synchronized (result) {
                try {
                    if (result[1] == null) {
                        result.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (errors[0] != null) {
                throw errors[0];
            }
            return (T) result[0];
        }

        public void postVoid(Event<Void> event) throws Throwable {
            postVoid(event, true);
        }

        public void postVoid(final Event<Void> event, final boolean block) throws Throwable {
            if ((thread != null && thread.isInterrupted())) {
                Log.e("QuickJS", "QuickJS is released");
                return;
            }
            if (Thread.currentThread() == thread) {
                event.run(jsContext, getGlobalObj());
                return;
            }
            if (handler == null) {
                event.run(jsContext, getGlobalObj());
                return;
            }
            final Object[] result = new Object[2];
            final RuntimeException[] errors = new RuntimeException[1];
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        event.run(jsContext, getGlobalObj());
                    } catch (RuntimeException e) {
                        errors[0] = e;
                    }
                    if (block) {
                        synchronized (result) {
                            result[1] = true;
                            result.notifyAll();
                        }
                    }
                }
            });
            if (block) {
                synchronized (result) {
                    try {
                        if (result[1] == null) {
                            result.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (errors[0] != null) {
                    throw errors[0];
                }
            }
        }

        public void init() {
            initConsole();
            initOkHttp();
            initLocalStorage();
        }

        void initConsole() {
            jsContext.evaluate("var console = {};");
            JSObject console = (JSObject) jsContext.getGlobalObject().getProperty("console");
            console.setProperty("log", new JSCallFunction() {
                @Override
                public Object call(Object... args) {
                    StringBuilder b = new StringBuilder();
                    for (Object o : args) {
                        b.append(o == null ? "null" : o.toString());
                    }
                    System.out.println(TAG + " >>> " + b);
                    return null;
                }
            });
        }

        void initLocalStorage() {
            jsContext.evaluate("var local = {};");
            JSObject console = (JSObject) jsContext.getGlobalObject().getProperty("local");
            console.setProperty("get", new JSCallFunction() {
                @Override
                public Object call(Object... args) {
                    SharedPreferences sharedPreferences = Constants.APP.getSharedPreferences("js_engine_" + args[0].toString(), Context.MODE_PRIVATE);
                    return sharedPreferences.getString(args[1].toString(), "");
                }
            });
            console.setProperty("set", new JSCallFunction() {
                @Override
                public Object call(Object... args) {
                    SharedPreferences sharedPreferences = Constants.APP.getSharedPreferences("js_engine_" + args[0].toString(), Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString(args[1].toString(), args[2].toString()).commit();
                    return null;
                }
            });
            console.setProperty("delete", new JSCallFunction() {
                @Override
                public Object call(Object... args) {
                    SharedPreferences sharedPreferences = Constants.APP.getSharedPreferences("js_engine_" + args[0].toString(), Context.MODE_PRIVATE);
                    sharedPreferences.edit().remove(args[1].toString()).commit();
                    return null;
                }
            });
        }

        void initOkHttp() {
            jsContext.getGlobalObject().setProperty("req", new JSCallFunction() {
                @Override
                public Object call(Object... args) {
                    try {
                        String url = args[0].toString();
                        JSONObject opt = JSONObject.parseObject(jsContext.stringify((JSObject) args[1]));
                        Map<String, String> headers = new HashMap<String, String>();
                        JSONObject optHeader = opt.getJSONObject("headers");
                        if (optHeader != null) {
                            Iterator<String> hdKeys = optHeader.keySet().iterator();
                            while (hdKeys.hasNext()) {
                                String k = hdKeys.next();
                                String v = optHeader.getString(k);
                                headers.put(k, v);
                            }
                        }
                        if(opt.containsKey("redirect") && opt.getInteger("redirect") == 0){
                            JSThread.this.hsu.setFollowRedirects(false);
                        }
                        if(opt.containsKey("timeout")){
                            JSThread.this.hsu.setConnectionTime(opt.getInteger("timeout"));
                            JSThread.this.hsu.setSoTime(opt.getInteger("timeout"));
                        }
                        String method = "GET";
                        if(opt.containsKey("method")){
                            method = opt.getString("method");
                        }
                        HttpResult result = null;
                        if (method.equalsIgnoreCase("post")) {
                            if (opt.containsKey("data")) {
                                result = JSThread.this.hsu.doPostBody_Json(url, opt.getString("data"), null, headers, "UTF-8");
                            } else {
                                result = JSThread.this.hsu.doPostBody(url, opt.getString("body"), null, headers, "UTF-8");
                            }
                        } else if (method.equalsIgnoreCase("header")) {
                            result = JSThread.this.hsu.doHeadBody(url, null, headers, "UTF-8");
                        } else {
                            result = JSThread.this.hsu.doGetBody(url, null, headers, "UTF-8");
                        }

                        JSObject jsObject = jsContext.createNewJSObject();
                        JSObject resHeader = jsContext.createNewJSObject();
                        if(result.getResponse_headers() != null){
                            for (int i=0;i<result.getResponse_headers().size();i++) {
                                HeaderEntry header = result.getResponse_headers().getEntryAt(i);
                                resHeader.setProperty(header.getKey(), header.getValue());
                            }
                        }
                        jsObject.setProperty("headers", resHeader);
                        int returnBuffer = 0;
                        if(opt.containsKey("buffer")){
                            returnBuffer = opt.getInteger("buffer");
                        }
                        if (returnBuffer == 1) {
                            JSArray array = jsContext.createNewJSArray();
                            byte[] bytes = result.getBytesResult();
                            for (int i = 0; i < bytes.length; i++) {
                                array.set(bytes[i], i);
                            }
                            jsObject.setProperty("content", array);
                        } else if (returnBuffer == 2) {
                            jsObject.setProperty("content", Base64.encodeToString(result.getBytesResult(), Base64.DEFAULT));
                        } else {
                            String res;
                            if (headers.get("Content-Type") != null && headers.get("Content-Type").contains("=")) {
                                byte[] responseBytes = UTF8BOMFighter.removeUTF8BOM(result.getBytesResult());
                                res = new String(responseBytes, headers.get("Content-Type").split("=")[1].trim());
                            } else {
                                res = result.getResult();
                            }
                            jsObject.setProperty("content", res);
                        }
                        return jsObject;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    JSObject jsObject = jsContext.createNewJSObject();
                    JSObject resHeader = jsContext.createNewJSObject();
                    jsObject.setProperty("headers", resHeader);
                    jsObject.setProperty("content", "");
                    return jsObject;
                }
            });
            jsContext.getGlobalObject().setProperty("joinUrl", new JSCallFunction() {
                @Override
                public String call(Object... args) {
                    URL url;
                    String q = "";
                    try {
                        String parent = args[0].toString();
                        String child = args[1].toString();
                        // TODO
                        if (parent.isEmpty()) {
                            return child;
                        }
                        url = new URL(new URL(parent), child);
                        q = url.toExternalForm();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    return q;
                }
            });
            jsContext.getGlobalObject().setProperty("pdfh", new JSCallFunction() {
                @Override
                public String call(Object... args) {
                    try {
//                      LOG.i("pdfh----------------:"+args[1].toString().trim());
                        String html = args[0].toString();
                        return HtmlParser.parseDomForUrl(html, args[1].toString().trim(), "");
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    return "";
                }
            });
            jsContext.getGlobalObject().setProperty("pdfa", new JSCallFunction() {
                @Override
                public Object call(Object... args) {
                    try {
//                      LOG.i("pdfa----------------:"+args[1].toString().trim());
                        String html = args[0].toString();
                        return jsContext.parseJSON(JSONObject.toJSONString(HtmlParser.parseDomForList(html, args[1].toString().trim())));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    return null;
                }
            });
            jsContext.getGlobalObject().setProperty("pd", new JSCallFunction() {
                @Override
                public String call(Object... args) {
                    try {
//                      LOG.i("pd----------------:"+args[2].toString().trim());
                        String html = args[0].toString();
                        return HtmlParser.parseDomForUrl(html, args[1].toString().trim(), args[2].toString());
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    return "";
                }
            });
        }

    }

    private final ConcurrentHashMap<String, JSThread> threads = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, String> moduleCache = new ConcurrentHashMap<>();

    public static String loadModule(HttpSimpleUtils hsu, String name) {
        try {
            String cache = moduleCache.get(name);
            if (cache != null && !cache.isEmpty()) return cache;
            String content = null;
            if (name.startsWith("http://") || name.startsWith("https://")) {
                content = hsu.doGetBody(name, null, null, "UTF-8").getResult();
            }
            if (name.startsWith("assets://")) {
                InputStream is = Constants.APP.getAssets().open(name.substring(9));
                byte[] data = new byte[is.available()];
                is.read(data);
                content = new String(data, "UTF-8");
            }
            if (content != null && !content.isEmpty()) {
                moduleCache.put(name, content);
                return content;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void create() {
        System.loadLibrary("quickjs");
    }

    public JSThread getJSThread(final HttpSimpleUtils hsu) {
        byte count = Byte.MAX_VALUE;
        JSThread thread = null;
        for (String name : threads.keySet()) {
            JSThread jsThread = threads.get(name);
            if (jsThread.retain < count && jsThread.retain < 1) {
                thread = jsThread;
                count = jsThread.retain;
            }
        }
        if (thread == null) {
            final Object[] objects = new Object[2];
            String name = "QuickJS-Thread-" + threads.size();
            HandlerThread handlerThread = new HandlerThread(name + "-0");
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    objects[0] = QuickJSContext.create();
                    synchronized (objects) {
                        objects[1] = true;
                        objects.notify();
                    }
                }
            });
            synchronized (objects) {
                try {
                    if (objects[1] == null) {
                        objects.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            QuickJSContext jsContext = (QuickJSContext) objects[0];
            JSModule.setModuleLoader(new JSModule.Loader() {
                @Override
                public String getModuleScript(String moduleName) {
                    return loadModule(hsu, moduleName);
                }
            });
            final JSThread jsThread = new JSThread(hsu);
            jsThread.handler = handler;
            jsThread.thread = handlerThread;
            jsThread.jsContext = jsContext;
            jsThread.retain = 0;
            thread = jsThread;
            try {
                jsThread.postVoid(new Event<Void>() {
                    @Override
                    public Void run(QuickJSContext ctx, JSObject globalThis) {
                        jsThread.init();
                        return null;
                    }
                });
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            threads.put(name, jsThread);
        }
        thread.retain++;
        String name = thread.thread.getName();
        name = name.substring(0, name.lastIndexOf("-") + 1) + thread.retain;
        thread.thread.setName(name);
        return thread;
    }

    public void destroy() {
        for (String name : threads.keySet()) {
            JSThread jsThread = threads.get(name);
            if (jsThread != null && jsThread.thread != null) {
                jsThread.thread.interrupt();
            }
            if (jsThread.jsContext != null) {
                jsThread.jsContext.destroyContext();
            }
        }
        threads.clear();
    }

    public void stopAll() {
        for (String name : threads.keySet()) {
            JSThread jsThread = threads.get(name);
            if (jsThread.handler != null) {
                jsThread.handler.removeCallbacksAndMessages(null);
            }
        }
    }

    public interface Event<T> {
        T run(QuickJSContext ctx, JSObject globalThis);
    }
}