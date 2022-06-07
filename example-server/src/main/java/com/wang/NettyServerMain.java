package com.wang;

import com.wang.provider.ServiceProviderImpl;
import com.wang.transport.netty.server.NettyServer;

public class NettyServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 9999);
        nettyServer.publishService(helloService, HelloService.class);
    }
}
