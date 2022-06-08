package com.wang.handler;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.enumeration.RpcResponseCode;
import com.wang.exception.RpcException;
import com.wang.provider.ServiceProvider;
import com.wang.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RpcRequest 的处理器
 */
@Slf4j
public class RpcRequestHandler { //请求处理

    private static ServiceProvider serviceProvider;

    //直接在请求处理阶段调用服务注册信息
    static {
        serviceProvider = new ServiceProviderImpl();
    }

    //处理 rpcRequest 然后返回方法执行结果
    public Object handle(RpcRequest rpcRequest){
        Object result;

        //通过注册中心获取到目标类（客户端需要调用类）
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());

        result = invokeTargetMethod(rpcRequest, service);
        log.info("service:{} successful invoke method:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        return result;
    }

    //构造了一个方法获取service中的方法，以抛出类没实现接口或接口的实现里没有该方法
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        //通过反射获取service中的方法
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());//参数类型
            if (null == method) {
                return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
            }
            result = method.invoke(service, rpcRequest.getParameters());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RpcException(e.getMessage(), e);
        }

        return result;//获取参数，在代理里执行
    }
}
