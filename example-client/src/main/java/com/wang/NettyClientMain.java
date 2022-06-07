package com.wang;

import com.wang.transport.ClientTransport;
import com.wang.proxy.RpcClientProxy;
import com.wang.transport.netty.client.NettyClientTransport;

import java.net.InetSocketAddress;

public class NettyClientMain {
    public static void main(String[] args) {
        ClientTransport rpcClient = new NettyClientTransport();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        //assert "Hello description is 222".equals(hello);

        //System.out.println("上面的调用卡住之后，这里也不会调用了");
        String hello2 = helloService.hello(new Hello("222", "333"));
        System.out.println(hello2);
    }
}
