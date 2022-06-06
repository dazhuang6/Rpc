package com.wang;

import com.wang.transport.ClientTransport;
import com.wang.transport.RpcClientProxy;
import com.wang.transport.socket.SocketRpcClient;

public class RpcFrameworkClientMain {
    public static void main(String[] args) {
        ClientTransport rpcClient = new SocketRpcClient("127.0.0.1", 9998);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);

        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
