package com.wang;

import com.wang.dto.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    public Object sendRpcRequest(RpcRequest rpcRequest, String host, int port){
        try (Socket socket = new Socket(host, port)){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());//对象输出流
            objectOutputStream.writeObject(rpcRequest);//传给服务端

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());//对象输入流
            return objectInputStream.readObject();//从服务端接收
        } catch (IOException | ClassNotFoundException e){
            logger.error("occur exception:", e);
        }
        return null;
    }
}
