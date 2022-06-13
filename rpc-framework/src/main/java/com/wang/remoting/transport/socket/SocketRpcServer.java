package com.wang.remoting.transport.socket;

import com.wang.config.CustomShutdownHook;
import com.wang.entity.RpcServiceProperties;
import com.wang.factory.SingletonFactory;
import com.wang.provider.ServiceProvider;
import com.wang.provider.ServiceProviderImpl;
import com.wang.utils.concurrent.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SocketRpcServer {
    public static final int PORT = 9999;
    /**
     * 使用工具类线程池
     */
    private final ExecutorService threadPool;

    private final ServiceProvider serviceProvider;

    public SocketRpcServer() { //通过注入服务来调用RpcServer
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        SingletonFactory.getInstance(ServiceProviderImpl.class);
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    //直接在服务端注册服务的两种方法
    public void registerService(Object service) {
        serviceProvider.publishService(service);
    }

    public void registerService(Object service, RpcServiceProperties rpcServiceProperties) {
        serviceProvider.publishService(service, rpcServiceProperties);
    }

    /**
     * 代理的端口
     */
    public void start() {

        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            log.info("server starts...");
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }
}
