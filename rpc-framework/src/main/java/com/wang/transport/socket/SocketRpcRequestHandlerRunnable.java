package com.wang.transport.socket;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.registry.DefaultServiceRegistry;
import com.wang.registry.ServiceRegistry;
import com.wang.transport.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 客服端消息处理线程
 * 执行任务需要实现Runnable结果或Callable接口
 */
public class SocketRpcRequestHandlerRunnable implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(SocketRpcRequestHandlerRunnable.class);
    private Socket socket;

    private static RpcRequestHandler rpcRequestHandler;
    private static ServiceRegistry serviceRegistry;
    static {
        rpcRequestHandler = new RpcRequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
    }

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        logger.info(String.format("server handle message from client by thread: %s", Thread.currentThread().getName()));
        //try-with-resources
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())){
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();

            //通过注册的服务里寻找接口的实现方法
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result = rpcRequestHandler.handle(rpcRequest, service);

            oos.writeObject(RpcResponse.success(result));//输出流,先输出给Rpc回复
            oos.flush();
        } catch (IOException | ClassNotFoundException e){
            logger.error("occur exception:", e);
        }
    }
}
