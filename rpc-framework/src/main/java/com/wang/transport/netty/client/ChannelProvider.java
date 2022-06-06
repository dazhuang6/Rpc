package com.wang.transport.netty.client;

import com.wang.enumeration.RpcErrorMessageEnum;
import com.wang.exception.RpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ChannelProvider {
    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);

    private static Bootstrap bootstrap = NettyClient.initializeBootstrap();
    private static Channel channel = null;
    /**
     * 最多重试次数
     */
    private static final int MAX_RETRY_COUNT = 5;

    public static Channel get(InetSocketAddress inetSocketAddress){
        CountDownLatch countDownLatch = new CountDownLatch(1); //加法计数器，这里计数为1
        try {
            connect(bootstrap, inetSocketAddress, countDownLatch);
            countDownLatch.await(); //只需成功一次就可以往下执行返回channel
        } catch (InterruptedException e) {
            logger.error("occur exception when get channel:", e);
        }
        return channel;
    }

    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, CountDownLatch countDownLatch) {
        connect(bootstrap, inetSocketAddress, MAX_RETRY_COUNT, countDownLatch);
    }

    /**
     * 带有重试机制的客户端连接方法
     * @param bootstrap 启动类
     * @param inetSocketAddress 连接地址
     * @param retry 重试次数
     * @param countDownLatch 同步加法计数器
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, int retry, CountDownLatch countDownLatch) {
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("客户端连接成功!");
                channel = future.channel();
                countDownLatch.countDown();
                return;
            }
            if (retry == 0) {
                logger.error("客户端连接失败:重试次数已用完，放弃连接！");
                countDownLatch.countDown();
                throw new RpcException(RpcErrorMessageEnum.CLIENT_CONNECT_SERVER_FAILURE);
            }
            // 第几次重连
            int order = (MAX_RETRY_COUNT - retry) + 1;
            // 本次重连的间隔
            int delay = 1 << order;
            logger.error("{}: 连接失败，第 {} 次重连……", new Date(), order);
            bootstrap.config().group().schedule(() -> connect(bootstrap, inetSocketAddress, retry - 1, countDownLatch), delay, TimeUnit
                    .SECONDS);
        });
    }
}
