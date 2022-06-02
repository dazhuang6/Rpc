package com.wang.transport;

import com.wang.dto.RpcRequest;

public interface RpcClient {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
