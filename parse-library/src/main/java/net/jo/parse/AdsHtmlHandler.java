package net.jo.parse;

import io.netty.buffer.Unpooled;
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

@ChannelHandler.Sharable
public class AdsHtmlHandler  extends ChannelInboundHandlerAdapter {
    private String m3u8_content = "";

    public void setM3u8Content(String m3u8_content){
        this.m3u8_content = m3u8_content;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = null;
        FullHttpRequest request = null;
        if (msg instanceof HttpRequest) {
            request = (FullHttpRequest) msg;
            if (request.getUri().contains("/index.m3u8")) {
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(this.m3u8_content.getBytes("UTF-8")));
                response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=utf-8");
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
}