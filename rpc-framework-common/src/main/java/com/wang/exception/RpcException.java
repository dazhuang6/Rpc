package com.wang.exception;

import com.wang.enumeration.RpcErrorMessage;

public class RpcException extends RuntimeException{ //自定义异常
    public RpcException(RpcErrorMessage rpcErrorMessageEnum, String detail){ //远程服务调用错误的异常和信息抛出
        super(rpcErrorMessageEnum.getMessage() + ":" + detail); //直接传给RuntimeException
    }

    public RpcException(String message, Throwable cause){ //其他异常
        super(message, cause);
    }

    public RpcException(RpcErrorMessage rpcErrorMessageEnum){ //远程服务调用错误的异常
        super(rpcErrorMessageEnum.getMessage());
    }
}
