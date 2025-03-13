package net.jo.common;

import java.util.UUID;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 本地M3U8服务管理，主要用于本地过滤M3U8广告切片后重新组装M3U8文件使用
 */
public class HtmlServiceUtils {
    private static int SERVICE_PORT = 18808;
    private static ChannelInboundHandlerAdapter handlerAdapter;
    private static EventLoopGroup boss = null;
    private static EventLoopGroup worker = null;
    private static String PATH = null;

    public static void start(ChannelInboundHandlerAdapter handlerAdapter) {
        HtmlServiceUtils.start(handlerAdapter, HtmlServiceUtils.SERVICE_PORT);
    }

    public static void start(ChannelInboundHandlerAdapter handlerAdapter, int service_port) {
        HtmlServiceUtils.shutdown();

        HtmlServiceUtils.PATH = UUID.randomUUID().toString().split("-")[0];
        HtmlServiceUtils.SERVICE_PORT = service_port;
        HtmlServiceUtils.handlerAdapter = handlerAdapter;
        new HTMLServer().start();
    }

    public static void shutdown() {
        try {
            if (HtmlServiceUtils.worker != null && !HtmlServiceUtils.worker.isShutdown()) {
                HtmlServiceUtils.worker.shutdownGracefully();
            }
        } catch (Exception ex) {
            System.out.println("关闭本地(Worker)Html服务异常");
        }
        try {
            if (HtmlServiceUtils.boss != null && !HtmlServiceUtils.boss.isShutdown()) {
                HtmlServiceUtils.boss.shutdownGracefully();
            }
        } catch (Exception ex) {
            System.out.println("关闭本地(Boss)Html服务异常");
        }
    }

    public static String getServerUrl() {
        String host = Utils.getWifiLocalIp(Constants.APP);
        return "http://" + host + ":" + HtmlServiceUtils.SERVICE_PORT + "/" + HtmlServiceUtils.PATH;
    }

    public static String getPath(){
        return HtmlServiceUtils.PATH;
    }

    public static int getPort(){
        return HtmlServiceUtils.SERVICE_PORT;
    }

    private static class HTMLServer extends Thread {
        public void run() {
            System.out.println("开启本地Html服务:" + HtmlServiceUtils.SERVICE_PORT);
            HtmlServiceUtils.boss = new NioEventLoopGroup(1);
            HtmlServiceUtils.worker = new NioEventLoopGroup(1);
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(HtmlServiceUtils.boss, HtmlServiceUtils.worker);
                serverBootstrap.channel(NioServerSocketChannel.class);
                serverBootstrap.childHandler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpResponseEncoder());
                        ch.pipeline().addLast(new HttpRequestDecoder());
                        ch.pipeline().addLast(new HttpObjectAggregator(65536));
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                        ch.pipeline().addLast(HtmlServiceUtils.handlerAdapter);
                    }
                });
                serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
                serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
                serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                ChannelFuture cf = serverBootstrap.bind(HtmlServiceUtils.SERVICE_PORT).sync();
                cf.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                HtmlServiceUtils.worker.shutdownGracefully();
                HtmlServiceUtils.boss.shutdownGracefully();
                System.out.println("关闭本地Html服务");
            }
        }
    }
}