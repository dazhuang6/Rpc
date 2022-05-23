package com.wang;

public class RpcFrameworkServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(helloService, 9999);
        System.out.println("后面的不会执行");
        rpcServer.register(new HelloServiceImpl(), 9999);
    }
}
