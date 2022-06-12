package com.wang;

import com.wang.remoting.transport.netty.server.NettyServer;

public class NettyServerMain2 {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 9998);
        nettyServer.publishService(helloService, HelloService.class);
    }
}
