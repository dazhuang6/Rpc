package com.wang.transport.socket;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.exception.RpcException;
import com.wang.registry.ServiceDiscovery;
import com.wang.registry.ServiceRegistry;
import com.wang.registry.ZkServiceDiscovery;
import com.wang.registry.ZkServiceRegistry;
import com.wang.transport.ClientTransport;
import com.wang.utils.checker.RpcMessageChecker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@AllArgsConstructor
@Slf4j
public class SocketRpcClient implements ClientTransport {

    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient() {
        this.serviceDiscovery = new ZkServiceDiscovery();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()){
            socket.connect(inetSocketAddress);//通过inetSocket连接
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());//对象输出流
            objectOutputStream.writeObject(rpcRequest);//传给服务端

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());//对象输入流

            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();//将从服务器接收的对象传给RpcResponse
            //通过common里的统一校验rpcResponse和rpcRequest
            RpcMessageChecker.check(rpcResponse, rpcRequest);

            return rpcResponse.getData(); //从rpc回复里获取数据
        } catch (IOException | ClassNotFoundException e){
            log.error("occur exception when send sendRpcRequest");
            throw new RpcException("调用服务失败：", e);
        }
    }
}
