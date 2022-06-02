package com.wang;

import com.wang.registry.DefaultServiceRegistry;
import com.wang.transport.netty.NettyRpcServer;

public class NettyServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        DefaultServiceRegistry defaultServiceRegistry = new DefaultServiceRegistry();

        defaultServiceRegistry.register(helloService);
        NettyRpcServer socketRpcServer = new NettyRpcServer(9999);
        socketRpcServer.run();
    }
}
