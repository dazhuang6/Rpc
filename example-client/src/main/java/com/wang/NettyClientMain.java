package com.wang;

import com.wang.remoting.transport.ClientTransport;
import com.wang.proxy.RpcClientProxy;
import com.wang.remoting.transport.netty.client.NettyClientTransport;
import org.apache.log4j.spi.ThrowableRenderer;

public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        ClientTransport rpcClient = new NettyClientTransport();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        //assert "Hello description is 222".equals(hello);

        //System.out.println("上面的调用卡住之后，这里也不会调用了");使用zookeeper后就可以了
        for (int i = 0; i < 50; i++) {
            String des = helloService.hello(new Hello("111", "~~~" + i));
            Thread.sleep(500);
            System.out.println(des);
        }
    }
}
