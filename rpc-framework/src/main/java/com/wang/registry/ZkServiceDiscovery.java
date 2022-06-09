package com.wang.registry;

import com.wang.utils.zk.CuratorUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery{

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // TODO 负载均衡
        // 这里直接去了第一个找到的服务地址,eg:127.0.0.1:99990000000017
        String serviceAddress = CuratorUtil.getChildrenNodes(serviceName).get(0);
        log.info("成功找到服务地址：{}", serviceAddress);
        return new InetSocketAddress(serviceAddress.split(":")[0],
                Integer.parseInt(serviceAddress.split(":")[1]));
    }

}
