package com.wang.utils.checker;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.enumeration.RpcErrorMessageEnum;
import com.wang.enumeration.RpcResponseCode;
import com.wang.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//检查返回消息，返回错误抛出异常
public class RpcMessageChecker {
    private static final Logger logger = LoggerFactory.getLogger(RpcMessageChecker.class);
    public static final String INTERFACE_NAME = "interfaceName";

    private RpcMessageChecker(){};

    public static void check(RpcResponse rpcResponse, RpcRequest rpcRequest){
        if (rpcResponse == null) { //说明服务调用失败，抛出调用接口的异常
            logger.error("调用服务失败,serviceName:{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        //当服务端的处理结果不是200成功
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
            logger.error("调用服务失败,serviceName:{},RpcResponse:{}", rpcRequest.getInterfaceName(), rpcResponse);
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
