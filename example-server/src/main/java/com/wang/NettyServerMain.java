package com.wang;

import com.wang.registry.DefaultServiceRegistry;
import com.wang.transport.netty.server.NettyServer;

public class NettyServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        DefaultServiceRegistry defaultServiceRegistry = new DefaultServiceRegistry();

        defaultServiceRegistry.register(helloService);
        NettyServer socketRpcServer = new NettyServer(9999);
        socketRpcServer.run();
    }
}
