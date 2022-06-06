package com.wang.transport.netty.client;

import com.wang.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，内部的 channelRead 方法会替你释放 ByteBuf ，
 * 避免可能导致的内存泄露问题。
 */
//自定义客户端 ChannelHandler 来处理服务端发过来的数据
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    //读取服务端传输的消息
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            logger.info(String.format("client receive msg: %s", msg));
            RpcResponse rpcResponse = (RpcResponse) msg;

            //声明一个AttributeKey对象
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcResponse.getRequestId());
            //将服务端的返回结果保存到AttributeMap上，AttributeMap可以看作是一个Channel的共享数据源
            //AttributeMap的key是AttributeKey，value是Attribute
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    //处理客户端消息发生异常的时候被调用
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("client catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
