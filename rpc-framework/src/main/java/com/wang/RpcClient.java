package com.wang;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.enumeration.RpcErrorMessageEnum;
import com.wang.enumeration.RpcResponseCode;
import com.wang.exception.RpcException;
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

            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();//将从服务器接收的对象传给RpcResponse
            if (rpcResponse == null){//说明服务调用失败，抛出调用接口的异常
                logger.error("调用服务失败，serviceName:{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "interfaceName:" + rpcRequest.getInterfaceName());
            }
            if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {//当服务端的处理结果不是200成功
                logger.error("调用服务失败,serviceName:{},RpcResponse:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, "interfaceName:" + rpcRequest.getInterfaceName());
            }

            return rpcResponse.getData(); //从rpc回复里获取数据
        } catch (IOException | ClassNotFoundException e){
            throw new RpcException("调用服务失败：", e);
        }
    }
}
