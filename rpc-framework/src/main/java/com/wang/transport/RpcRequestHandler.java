package com.wang.transport;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.enumeration.RpcResponseCode;
import com.wang.registry.DefaultServiceRegistry;
import com.wang.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RpcRequestHandler { //请求处理
    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    private static final ServiceRegistry serviceRegistry;

    //直接在请求处理阶段调用服务注册信息
    static {
        serviceRegistry = new DefaultServiceRegistry();
    }

    //处理 rpcRequest 然后返回方法执行结果
    public Object handle(RpcRequest rpcRequest){
        Object result = null;

        //通过注册中心获取到目标类（客户端需要调用类）
        Object service = serviceRegistry.getService(rpcRequest.getInterfaceName());

        try {
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("service:{} successful invoke method:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("occur exception", e);
        }
        return result;
    }

    //构造了一个方法获取service中的方法，以抛出类没实现接口或接口的实现里没有该方法
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        //通过反射获取service中的方法
        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());//参数类型
        if (null == method) {
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }
        return method.invoke(service, rpcRequest.getParameters());//获取参数，在代理里执行
    }
}
