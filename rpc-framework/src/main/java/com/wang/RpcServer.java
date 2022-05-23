package com.wang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class RpcServer {
    private ExecutorService threadPool;
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    public RpcServer(){
        //线程池参数
        int corePoolSize = 10;
        int maximumPoolSize = 100;
        long keepAliveTime = 1;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        this.threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MINUTES, workQueue, threadFactory);
    }

    /**
     * 代理的服务和端口
     * @param service 代理的服务，将接口的实现类注入
     * @param port 端口
     */
    public void register(Object service, int port){
        try (ServerSocket server = new ServerSocket(port)){
            logger.info("server starts...");
            Socket socket;
            while ((socket = server.accept()) != null){
                logger.info("client connected");
                threadPool.execute(new WorkerThread(socket, service));//实现了Runnable接口
            }
        } catch (IOException e){
            logger.error("occur IOException:",e);
        }
    }
}
