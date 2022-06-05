package com.wang.dto;

import com.wang.enumeration.RpcResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RpcResponse<T> implements Serializable { //泛型类
    private static final long serialVersionUID = 715745410605631233L;

    private Integer code; //响应码
    private String message; //响应消息
    private T data; //响应数据
    private String requestId;

    public static <T> RpcResponse<T> success(T data, String requestId){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCode.SUCCESS.getCode());

        response.setMessage(RpcResponseCode.SUCCESS.getMessage());
        response.setRequestId(requestId);

        if (null != data)
            response.setData(data);

        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCode rpcResponseCode){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCode.getCode());
        response.setMessage(rpcResponseCode.getMessage());
        return response;
    }
}
