package com.wang.transport.netty.client;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.registry.ServiceDiscovery;
import com.wang.registry.ServiceRegistry;
import com.wang.registry.ZkServiceDiscovery;
import com.wang.registry.ZkServiceRegistry;
import com.wang.transport.ClientTransport;
import com.wang.utils.checker.RpcMessageChecker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

//获取连接，发送请求
@Slf4j
public class NettyClientTransport implements ClientTransport {

    private final ServiceDiscovery serviceDiscovery;

    public NettyClientTransport() {
        this.serviceDiscovery = new ZkServiceDiscovery();
    }

    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //原子引用：意味着多个线程试图改变同一个AtomicReference(例如比较和交换操作)将不会使得AtomicReference处于不一致的状态
        AtomicReference<Object> result = new AtomicReference<>(null);
        try {
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.get(inetSocketAddress);
            if (channel.isActive()) {
                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("client send message: {}", rpcRequest);
                    } else {
                        future.channel().close();
                        log.error("Send failed:", future.cause());
                    }
                });
                channel.closeFuture().sync();
                //回复的key值里包含请求的ID号
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = channel.attr(key).get();
                log.info("client get rpcResponse from channel:{}", rpcResponse);
                //校验 RpcResponse 和 RpcRequest
                RpcMessageChecker.check(rpcResponse, rpcRequest);
                result.set(rpcResponse.getData());
            } else {
                NettyClient.close();
                System.exit(0);
            }

        } catch (InterruptedException e) {
            log.error("occur exception when send rpc message from client:", e);
        }
        return result.get();
    }
}
