package com.wang.remoting.dto;

import com.wang.entity.RpcServiceProperties;
import com.wang.enumeration.RpcMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * dto：data transfer object数据传输对象
 * 使用@Builder注解的作用主要是用来生成对象，并且可以为对象链式赋值。
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L; //自定义序列化ID

    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String requestId;
    private RpcMessageType rpcMessageTypeEnum; //发送心跳

    private String version;
    private String group;

    /**
     * 通过调用请求的接口，版本，组建立服务配置类，然后可以通过这个配置类得到配置名
     * @return
     */
    public RpcServiceProperties toRpcProperties() {
        return RpcServiceProperties.builder().serviceName(this.getInterfaceName())
                .version(this.getVersion())
                .group(this.getGroup()).build();
    }
}
