package com.wang.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * dto：data transfer object数据传输对象
 * 使用@Builder注解的作用主要是用来生成对象，并且可以为对象链式赋值。
 */

@Data
@Builder
public class RpcRequest implements Serializable {

    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
}
