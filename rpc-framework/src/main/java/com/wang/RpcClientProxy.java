package com.wang;

import com.wang.dto.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK静态代理实现InvocationHandler接口和Proxy类
 */
public class RpcClientProxy implements InvocationHandler {
    private String host;
    private int port;

    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 通过Proxy的newProxyInstance方法创建代理对象
     * @param clazz 接口或者实现类
     */
    @SuppressWarnings("unchecked") //指示编译器去忽略注解中声明的警告（仅仅编译器阶段，不保留到运行时）
    public <T>T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), // 类加载器，用于加载代理对象。
                                          new Class<?>[]{clazz}, // 被代理类实现的一些接口；
                                          RpcClientProxy.this); //实现了 InvocationHandler 接口的对象；
    }

    /**
     * 将当前方法传入RpcRequest，然后通过RpcClient将请求传给RpcServer处理
     * 动态代理对象调用方法时，这个方法会被转发到invoke方法来调用
     * @param proxy 动态生成的代理类
     * @param method 与代理类对象调用的方法相对应
     * @param args 当前 method 方法的参数
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .build();
        RpcClient rpcClient = new RpcClient();
        return rpcClient.sendRpcRequest(rpcRequest, host, port);
    }
}
