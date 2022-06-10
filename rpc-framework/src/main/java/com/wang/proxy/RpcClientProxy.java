package com.wang.proxy;

import com.wang.remoting.dto.RpcRequest;
import com.wang.remoting.dto.RpcResponse;
import com.wang.remoting.transport.ClientTransport;
import com.wang.remoting.transport.netty.client.NettyClientTransport;
import com.wang.remoting.transport.socket.SocketRpcClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * JDK静态代理实现InvocationHandler接口和Proxy类
 * 当动态代理对象调用一个方法的时候，实际调用的是下面的 invoke 方法
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    //用于发送请求给服务端，对应socket和netty两种实现方式
    private final ClientTransport clientTransport; //因为需要兼顾netty，所以将主机号与端口放在内部

    public RpcClientProxy(ClientTransport clientTransport) {
        this.clientTransport = clientTransport;
    }

    /**
     * 通过Proxy的newProxyInstance方法创建代理对象
     * @param clazz 接口或者实现类
     */
    @SuppressWarnings("unchecked") //指示编译器去忽略注解中声明的警告（仅仅编译器阶段，不保留到运行时）
    public <T>T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), // 类加载器，用于加载代理对象。
                new Class<?>[]{clazz}, // 被代理类实现的一些接口；
                this); //实现了 InvocationHandler 接口的对象；
    }

    /**
     * 将当前方法传入RpcRequest，然后通过RpcClient将请求传给RpcServer处理
     * 动态代理对象调用方法时，这个方法会被转发到invoke方法来调用
     * @param proxy 动态生成的代理类
     * @param method 与代理类对象调用的方法相对应
     * @param args 当前 method 方法的参数
     */
    @SneakyThrows //偷偷抛出异常
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("Call invoke method and invoked method: {}", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString()) //生成请求ID
                .build();

        Object result = null;
        if (clientTransport instanceof NettyClientTransport){
            CompletableFuture<RpcResponse> completableFuture =
                    (CompletableFuture<RpcResponse>) clientTransport.sendRpcRequest(rpcRequest);
            result = completableFuture.get().getData(); //获取future的结果，结果就是其泛型
        }
        if (clientTransport instanceof SocketRpcClient) {
            RpcResponse rpcResponse = (RpcResponse) clientTransport.sendRpcRequest(rpcRequest);
            result = rpcResponse.getData();
        }
        return result;
    }
}
