package com.wang.remoting.transport.socket;

import com.wang.entity.RpcServiceProperties;
import com.wang.exception.RpcException;
import com.wang.registry.ServiceDiscovery;
import com.wang.registry.zk.ZkServiceDiscovery;
import com.wang.remoting.dto.RpcRequest;
import com.wang.remoting.transport.ClientTransport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        // 通过请求建立服务名称
        String rpcServiceName = RpcServiceProperties.builder().serviceName(rpcRequest.getInterfaceName())
                .group(rpcRequest.getGroup()).version(rpcRequest.getVersion()).build().toRpcServiceName();

        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);

        try (Socket socket = new Socket()){
            socket.connect(inetSocketAddress);//通过inetSocket连接
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());//对象输出流
            objectOutputStream.writeObject(rpcRequest);//传给服务端

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());//对象输入流

            return objectInputStream.readObject();//将从服务器接收的对象传给RpcResponse
        } catch (IOException | ClassNotFoundException e){
            throw new RpcException("调用服务失败：", e);
        }
    }
}
