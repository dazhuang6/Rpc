package com.wang.provider;

import com.wang.entity.RpcServiceProperties;
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
    private final Map<String, Object> serviceMap; //使用线程安全的HashMap;
    //ConcurrentHashMap<K, Boolean>的一个包装器,所有映射值都为 Boolean.TRUE
    private final Set<String> registeredService;

    private final ServiceRegistry serviceRegistry;

    public ServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = new ZkServiceRegistry();
    }
    /**
     * 将这个对象所有实现的接口都注册进去
     */
    @Override
    public void addServiceProvider(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties) { //线程安全
        //String serviceName = serviceClass.getCanonicalName(); //用于返回基础类的授权名称
        String rpcServiceName = rpcServiceProperties.toRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, service);

        log.info("Add service: {} and interfaces:{}", rpcServiceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getServiceProvider(RpcServiceProperties rpcServiceProperties) { //线程安全
        Object service = serviceMap.get(rpcServiceProperties.toRpcServiceName());
        if (null == service) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service, RpcServiceProperties.builder().group("").version("").build());
    }

    @Override
    public void publishService(Object service, RpcServiceProperties rpcServiceProperties) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> serviceRelatedInterface = service.getClass().getInterfaces()[0];

            String serviceName = serviceRelatedInterface.getCanonicalName(); //用于返回基础类的授权名称
            rpcServiceProperties.setServiceName(serviceName);

            this.addServiceProvider(service, serviceRelatedInterface, rpcServiceProperties);
            serviceRegistry.registerService(rpcServiceProperties.toRpcServiceName(), new InetSocketAddress(host, NettyServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
