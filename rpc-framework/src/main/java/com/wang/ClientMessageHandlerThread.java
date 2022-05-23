package com.wang;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.enumeration.RpcResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * 客服端消息处理线程
 * 执行任务需要实现Runnable结果或Callable接口
 */
public class ClientMessageHandlerThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(ClientMessageHandlerThread.class);
    private Socket socket;
    private Object service;

    public ClientMessageHandlerThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        //try-with-resources
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())){
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();

            //通过构造的方法反射调用接口的实现方法
            Object result = invokeTargetMethod(rpcRequest);

            oos.writeObject(RpcResponse.success(result));//输出流,先输出给Rpc回复
            oos.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            logger.error("occur exception:", e);
        }
    }

    //构造了一个方法获取service中的方法，以抛出类没实现接口或接口的实现里没有该方法
    public Object invokeTargetMethod(RpcRequest rpcRequest) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        Class<?> cls = Class.forName(rpcRequest.getInterfaceName());
        //判断类是否实现了对应的接口
        if (!cls.isAssignableFrom(service.getClass())){
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_CLASS);
        }
        //通过反射获取service中的方法
        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());//参数类型
        if (null == method){
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }
        return method.invoke(service, rpcRequest.getParameters());//获取参数
    }
}
