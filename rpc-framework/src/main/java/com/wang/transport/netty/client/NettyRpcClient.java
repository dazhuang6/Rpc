package com.wang.transport.netty.client;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.serialize.kryo.KryoSerializer;
import com.wang.transport.RpcClient;
import com.wang.transport.netty.coder.NettyKryoDecoder;
import com.wang.transport.netty.coder.NettyKryoEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//客户端:发送消息到服务端，并接收服务端返回的方法执行结果
public class NettyRpcClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);
    private String host;
    private int port;
    private static final Bootstrap b; //引导启动类

    public NettyRpcClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    // 初始化相关资源比如 EventLoopGroup、Bootstrap
    static {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        b= new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        /*自定义序列化编解码器*/
                        // RpcResponse -> ByteBuf
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class))
                                // ByteBuf -> RpcRequest
                                .addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class))
                                .addLast(new NettyClientHandler());
                    }
                });
    }

    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        try {
            ChannelFuture f = b.connect(host, port).sync();//异步的同步
            logger.info("client connect {}", host + ":" + port);
            Channel futureChannel = f.channel();

            if (futureChannel != null){
                futureChannel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()){
                        logger.info(String.format("client send message: %s", rpcRequest.toString()));
                    } else
                        logger.error("send failed:", future.cause());
                });
                futureChannel.closeFuture().sync();

                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse = futureChannel.attr(key).get();
                return rpcResponse.getData();
            }
        } catch (InterruptedException e){
            logger.error("occur exception when connect server:", e);
        }
        return null;
    }
}
