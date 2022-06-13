package com.wang;

import com.wang.remoting.transport.netty.server.NettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.wang")
public class NettyServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        nettyServer.start();

//        HelloService helloService = new HelloServiceImpl();
//        NettyServer nettyServer = new NettyServer("127.0.0.1", 9999);
//        nettyServer.publishService(helloService, HelloService.class);
    }
}
