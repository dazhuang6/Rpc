package com.wang;

import com.wang.remoting.transport.ClientTransport;
import com.wang.proxy.RpcClientProxy;
import com.wang.remoting.transport.socket.SocketRpcClient;

public class RpcFrameworkClientMain {
    public static void main(String[] args) {
        ClientTransport clientTransport = new SocketRpcClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(clientTransport);

        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
