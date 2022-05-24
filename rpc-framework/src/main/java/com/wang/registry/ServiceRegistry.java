package com.wang.registry;

public interface ServiceRegistry {
    /**
     * 代理的服务
     * @param service 代理的服务，将接口的实现类注入
     */
    <T> void register (T service);

    Object getService(String serviceName);
}
