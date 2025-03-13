package net.jo.common;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

@ChannelHandler.Sharable
public class DefaultHtmlAdapter extends ChannelInboundHandlerAdapter {
    private ServerEvent.Type type;
    private String title;
    private String tips;
    private String hint;

    public DefaultHtmlAdapter(ServerEvent.Type type, String title, String tips, String hint){
        this.type = type;
        this.title = title;
        this.tips = tips;
        this.hint = hint;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = null;
        FullHttpRequest request = null;
        if (msg instanceof HttpRequest) {
            request = (FullHttpRequest) msg;
            Map<String,String> params = new HashMap<String,String>();
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder((FullHttpRequest)msg);
            decoder.offer((FullHttpRequest)msg);
            List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
            for (InterfaceHttpData parm : parmList) {
                Attribute data = (Attribute) parm;
                params.put(data.getName(), data.getValue());
            }
            String data = params.get("data");
            if (request.getUri().contains("/" + HtmlServiceUtils.getPath())) {
                if(!Utils.isEmpty(data)){
                    switch (this.type){
                        case SETTING:
                            ServerEvent.setting(data);
                            break;
                        case SEARCH:
                            ServerEvent.search(data);
                            break;
                        case PUSH:
                            ServerEvent.push(data);
                            break;
                    }
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("{\"code\":200,\"data\":\"设置成功\"}".getBytes("UTF-8")));
                    response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
                } else {
                    String html = getHtmlByTemplate();
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(html.getBytes("UTF-8")));
                    response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=utf-8");
                }
            } else {
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("{\"code\":1000,\"data\":\"初始化成功\"}".getBytes("UTF-8")));
                response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            }
        }
        if (response == null) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER);
        }
        response.headers().set(HttpHeaders.Names.CACHE_CONTROL, "no-cache");
        response.headers().set(HttpHeaders.Names.PRAGMA, "no-cache");
        response.headers().set(HttpHeaders.Names.EXPIRES, "0");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        request.release();
        ctx.channel().writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    private String getHtmlByTemplate() {
        try {
            InputStream is = Constants.APP.getAssets().open("index.html");
            byte[] data = new byte[is.available()];
            is.read(data);
            String content = new String(data, "UTF-8");
            content = content.replace("%title%", this.title);
            content = content.replace("%tips%", this.tips);
            content = content.replace("%path%", "/" + HtmlServiceUtils.getPath());
            content = content.replace("%hint%", this.hint);
            return content;
        } catch (Exception e) {
            return "";
        }
    }
}