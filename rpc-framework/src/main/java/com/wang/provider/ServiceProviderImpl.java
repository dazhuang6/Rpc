package com.wang.provider;

import com.wang.enumeration.RpcErrorMessageEnum;
import com.wang.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Map<String, Object> serviceMap = new ConcurrentHashMap<>(); //使用线程安全的HashMap;
    //ConcurrentHashMap<K, Boolean>的一个包装器,所有映射值都为 Boolean.TRUE
    private static Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 将这个对象所有实现的接口都注册进去
     */
    @Override
    public <T> void addServiceProvider(T service, Class<T> serviceClass) { //线程安全
        String serviceName = serviceClass.getCanonicalName(); //用于返回基础类的授权名称
        if (registeredService.contains(serviceName)) {
            return;
        }
        registeredService.add(serviceName);
        serviceMap.put(serviceName, service);

        log.info("Add service: {} and interfaces:{}", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getServiceProvider(String serviceName) { //线程安全
        Object service = serviceMap.get(serviceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }
}
