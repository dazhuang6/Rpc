package com.wang.registry;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    /**
     * 查找服务
     *
     * @param rpcServiceName 服务名称
     * @return 提供服务的地址
     */
    InetSocketAddress lookupService(String rpcServiceName);
}
