package com.wang;

import com.wang.entity.RpcServiceProperties;
import com.wang.provider.ServiceProvider;
import com.wang.provider.ServiceProviderImpl;
import com.wang.remoting.transport.netty.server.NettyServer;
import com.wang.serviceImpl.HelloServiceImpl;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class NettyServerMain2 {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);

        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        nettyServer.start();

        ServiceProvider serviceProvider = new ServiceProviderImpl();

        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test").version("1").build();
        serviceProvider.publishService(helloService, rpcServiceProperties);
    }
}
