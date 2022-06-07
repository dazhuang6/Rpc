package com.wang;

import com.wang.provider.ServiceProviderImpl;
import com.wang.transport.socket.SocketRpcServer;

public class RpcFrameworkServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 9998);
        socketRpcServer.publicService(helloService, HelloService.class);

    }
}
