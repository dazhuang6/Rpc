package com.wang;

import com.wang.entity.RpcServiceProperties;
import com.wang.remoting.transport.netty.server.NettyServer;
import com.wang.serviceImpl.HelloServiceImpl2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.wang")
public class NettyServerMain {
    public static void main(String[] args) {
        // 通过注解注册
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        // 常规注册
        HelloService helloService2 = new HelloServiceImpl2();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test2").version("version2").build();
        nettyServer.registerService(helloService2, rpcServiceProperties);
        nettyServer.start();
    }
}
