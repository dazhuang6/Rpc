package com.wang.transport.netty.server;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.serialize.kryo.KryoSerializer;
import com.wang.transport.netty.coder.NettyKryoDecoder;
import com.wang.transport.netty.coder.NettyKryoEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//服务端:接收客户端消息，并且根据客户端的消息调用相应的方法，然后返回结果给客户端。
public class NettyServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private final int port;
    private final KryoSerializer kryoSerializer;

    public NettyServer(int port) {
        this.port = port;
        kryoSerializer = new KryoSerializer();
    }

    public void run(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); //服务端启动类
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class))
                                    .addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class))
                                    .addLast(new NettyServerHandler());
                        }
                    })
                    //设置TCP缓冲区
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128);
//                    .option(ChannelOption.SO_KEEPALIVE, true); 开启TCP底层心跳机制，会报错，因为这个版本没用这个配置选项

            //绑定端口，同步等待绑定成功
            ChannelFuture f = b.bind(port).sync();
            //等待服务端监听窗口关闭
            f.channel().closeFuture().sync();

        } catch (InterruptedException e){
            logger.error("occur exception when start server:", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
