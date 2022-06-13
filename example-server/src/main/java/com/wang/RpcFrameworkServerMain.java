package com.wang;

import com.wang.provider.ServiceProvider;
import com.wang.provider.ServiceProviderImpl;
import com.wang.remoting.transport.socket.SocketRpcServer;
import com.wang.serviceImpl.HelloServiceImpl;

public class RpcFrameworkServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 9999);
        socketRpcServer.start();

        ServiceProvider serviceProvider = new ServiceProviderImpl();
        serviceProvider.publishService(helloService);

    }
}
