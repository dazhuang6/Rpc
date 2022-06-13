package com.wang.remoting.transport.socket;

import com.wang.config.CustomShutdownHook;
import com.wang.factory.SingletonFactory;
import com.wang.provider.ServiceProvider;
import com.wang.provider.ServiceProviderImpl;
import com.wang.registry.ServiceRegistry;
import com.wang.registry.zk.ZkServiceRegistry;
import com.wang.utils.concurrent.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SocketRpcServer {
    /**
     * 使用工具类线程池
     */
    private final ExecutorService threadPool;

    private final String host;
    private final int port;

    public SocketRpcServer(String host, int port) { //通过注入服务来调用RpcServer
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    /**
     * 代理的端口
     */
    public void start() {

        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(host, port));
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
