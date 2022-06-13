package com.wang.provider;

import com.wang.entity.RpcServiceProperties;

/**
 * 保存和提供服务实例对象。
 */
public interface ServiceProvider {
    /**
     * 保存服务实例对象和服务实例对象实现的接口类的对应关系
     *
     * @param service      服务实例对象
     * @param serviceClass 服务实例对象实现的接口类
     * @param rpcServiceProperties 服务关联对象
     */
    void addServiceProvider(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties);


    /**
     * 获取服务实例对象
     *
     * @param rpcServiceProperties 服务关联对象
     * @return 服务实例对象
     */
    Object getServiceProvider(RpcServiceProperties rpcServiceProperties);

    /**
     * 发布服务
     *
     * @param service 服务实例对象
     */
    void publishService(Object service, RpcServiceProperties rpcServiceProperties);

    void publishService(Object service);
}
