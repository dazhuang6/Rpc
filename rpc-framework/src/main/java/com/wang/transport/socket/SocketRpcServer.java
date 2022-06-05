package com.wang.transport.socket;

import com.wang.utils.concurrent.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class SocketRpcServer {
    /**
     * 使用工具类线程池
     */
    private ExecutorService threadPool;

    private static final Logger logger = LoggerFactory.getLogger(SocketRpcServer.class);

    public SocketRpcServer(){ //通过注入服务来调用RpcServer
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-server-rpc-pool");
    }

    /**
     * 代理的端口
     * @param port 端口
     */
    public void start(int port) {

        try (ServerSocket server = new ServerSocket(port)) {
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
