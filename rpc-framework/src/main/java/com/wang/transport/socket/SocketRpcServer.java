package com.wang.transport.socket;

import com.wang.provider.ServiceProvider;
import com.wang.provider.ServiceProviderImpl;
import com.wang.registry.ServiceRegistry;
import com.wang.registry.ZkServiceRegistry;
import com.wang.utils.concurrent.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class SocketRpcServer {
    /**
     * 使用工具类线程池
     */
    private final ExecutorService threadPool;

    private static final Logger logger = LoggerFactory.getLogger(SocketRpcServer.class);
    private final String host;
    private final int port;
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer(String host, int port) { //通过注入服务来调用RpcServer
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-server-rpc-pool");
        serviceRegistry = new ZkServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
    }

    public <T> void publicService(T service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service,serviceClass);
        serviceRegistry.registerService(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }

    /**
     * 代理的端口
     */
    public void start() {

        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(host, port));
            logger.info("server starts...");
            Socket socket;
            while ((socket = server.accept()) != null) {
                logger.info("client connected");
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("occur IOException:", e);
        }
    }
}
