package com.wang.remoting.transport.socket;

import com.wang.remoting.dto.RpcRequest;
import com.wang.remoting.dto.RpcResponse;
import com.wang.remoting.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 客服端消息处理线程
 * 执行任务需要实现Runnable结果或Callable接口
 */
@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable{

    private final Socket socket;

    private final RpcRequestHandler rpcRequestHandler;

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
        this.rpcRequestHandler = new RpcRequestHandler();
    }

    @Override
    public void run() {
        log.info("server handle message from client by thread: [{}]", Thread.currentThread().getName());
        //try-with-resources
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())){
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();

            //通过注册的服务里寻找接口的实现方法
            String interfaceName = rpcRequest.getInterfaceName();
            Object result = rpcRequestHandler.handle(rpcRequest);

            oos.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));//输出流,先输出给Rpc回复
            oos.flush();
        } catch (IOException | ClassNotFoundException e){
            log.error("occur exception:", e);
        }
    }
}
