package com.wang;

import com.wang.dto.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * 执行任务需要实现Runnable结果或Callable接口
 */
public class WorkerThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);
    private Socket socket;
    private Object service;

    public WorkerThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        //try-with-resources
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())){
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();

            //通过反射获取service中的方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());//参数类型
            Object result = method.invoke(service, rpcRequest.getParameters());//获取参数

            oos.writeObject(result);//输出流
            oos.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            logger.error("occur exception:", e);
        }
    }
}
