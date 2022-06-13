package com.wang.provider;

import com.wang.enumeration.RpcErrorMessage;
import com.wang.exception.RpcException;
import com.wang.registry.ServiceRegistry;
import com.wang.registry.zk.ZkServiceRegistry;
import com.wang.remoting.transport.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现了 ServiceProvider 接口，可以将其看做是一个保存和提供服务实例对象的示例
 * 默认的服务注册中心实现，通过 Map 保存服务信息，可以通过 zookeeper 来改进
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    /**
     * 接口名和服务的对应关系
     * key:service/interface name
     * value:service
     */
    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>(); //使用线程安全的HashMap;
    //ConcurrentHashMap<K, Boolean>的一个包装器,所有映射值都为 Boolean.TRUE
    private static final Set<String> REGISTERED_SERVICE = ConcurrentHashMap.newKeySet();

    private final ServiceRegistry serviceRegistry = new ZkServiceRegistry();
    /**
     * 将这个对象所有实现的接口都注册进去
     */
    @Override
    public void addServiceProvider(Object service, Class<?> serviceClass) { //线程安全
        String serviceName = serviceClass.getCanonicalName(); //用于返回基础类的授权名称
        if (REGISTERED_SERVICE.contains(serviceName)) {
            return;
        }
        REGISTERED_SERVICE.add(serviceName);
        SERVICE_MAP.put(serviceName, service);

        log.info("Add service: {} and interfaces:{}", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getServiceProvider(String serviceName) { //线程安全
        Object service = SERVICE_MAP.get(serviceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(Object service) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> anInterface = service.getClass().getInterfaces()[0];
            this.addServiceProvider(service, anInterface);
            serviceRegistry.registerService(anInterface.getCanonicalName(), new InetSocketAddress(host, NettyServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }

    }
}
