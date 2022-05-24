package com.wang;

import com.wang.registry.DefaultServiceRegistry;
import com.wang.remoting.socket.RpcServer;

public class RpcFrameworkServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        DefaultServiceRegistry defaultServiceRegistry = new DefaultServiceRegistry();
        //手动注册
        defaultServiceRegistry.register(helloService);
        //通过注册的服务调用远程服务
        RpcServer rpcServer = new RpcServer(defaultServiceRegistry);
        rpcServer.start(9998);

    }
}
