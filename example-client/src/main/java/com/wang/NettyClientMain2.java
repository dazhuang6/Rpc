package com.wang;

import com.wang.entity.RpcServiceProperties;
import com.wang.proxy.RpcClientProxy;
import com.wang.remoting.transport.ClientTransport;
import com.wang.remoting.transport.netty.client.NettyClientTransport;

public class NettyClientMain2 {
    public static void main(String[] args) throws InterruptedException {
        ClientTransport rpcClient = new NettyClientTransport();

        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test2").version("version1").build();

        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceProperties);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        //assert "Hello description is 222".equals(hello);

        //System.out.println("上面的调用卡住之后，这里也不会调用了");使用zookeeper后就可以了
        for (int i = 0; i < 10; i++) {
            String des = helloService.hello(new Hello("111", "~~~" + i));
            Thread.sleep(1000);
            System.out.println(des);
        }
    }
}
