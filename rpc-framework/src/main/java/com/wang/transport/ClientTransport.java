package com.wang.transport;

import com.wang.dto.RpcRequest;

//实现了 RpcClient 接口的对象需要具有发送 RpcRequest 的能力
public interface ClientTransport {

    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
