package com.wang.remoting.transport.netty.server;

import com.wang.enumeration.RpcMessageType;
import com.wang.enumeration.RpcResponseCode;
import com.wang.factory.SingletonFactory;
import com.wang.remoting.handler.RpcRequestHandler;
import com.wang.remoting.dto.RpcRequest;
import com.wang.remoting.dto.RpcResponse;
import com.wang.utils.concurrent.CustomThreadPoolConfig;
import com.wang.utils.concurrent.ThreadPoolFactoryUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

//自定义服务端的 ChannelHandler 来处理客户端发过来的数据
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler rpcRequestHandler;
    private final ExecutorService threadPool;
    private static final String THREAD_NAME_PREFIX = "netty-server-handler-rpc-pool";

    public NettyServerHandler() {
        //通过单例模式获取请求处理
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        customThreadPoolConfig.setCorePoolSize(6);
        this.threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent(THREAD_NAME_PREFIX, customThreadPoolConfig);
    }

    //使用线程池处理数据
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        threadPool.execute(() -> {
            log.info(String.format("server handle message from client by thread: %s", Thread.currentThread().getName()));

            try {
                log.info(String.format("server receive msg: %s", msg));
                RpcRequest rpcRequest = (RpcRequest) msg;
                //处理心跳请求
                if (rpcRequest.getRpcMessageTypeEnum() == RpcMessageType.HEART_BEAT) {
                    log.info("receive heat beat msg from client");
                    return;
                }
                //请求处理里调用了注册中心，获取了请求的目标类。执行目标方法（客户端需要执行的方法）并且返回方法结果
                Object result = rpcRequestHandler.handle(rpcRequest);
                log.info(String.format("server get result: %s", result.toString()));
                //返回方法执行结果给客户端
                if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                    RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                    //监听通道的状态
                    ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCode.FAIL);
                    ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    log.error("not writable now, message dropped");
                }

            } finally {
                //确保 ByteBuf 被释放，不然可能会有内存泄露问题
                ReferenceCountUtil.release(msg);
            }
        });
    }

    //长时间没有读操作关闭通道
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
