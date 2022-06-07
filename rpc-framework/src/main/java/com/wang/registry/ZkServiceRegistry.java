package com.wang.registry;

import com.wang.utils.zk.CuratorHelper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ZkServiceRegistry implements ServiceRegistry{
    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);
    private final CuratorFramework zkClient;

    public ZkServiceRegistry() {
        zkClient = CuratorHelper.getZkClient();
        zkClient.start();
    }

    //InetSocketAddress类主要作用是封装端口,在InetAddress基础上加端口
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        //根节点下注册子节点：服务
        StringBuilder servicePath = new StringBuilder(CuratorHelper.ZK_REGISTER_ROOT_PATH).append("/").append(serviceName);
        //服务子节点下注册子节点：服务地址
        servicePath.append(inetSocketAddress.toString());
        CuratorHelper.creatEphemeralNode(zkClient, servicePath.toString());
        logger.info("节点创建成功，节点为：{}", servicePath);
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // TODO 负载均衡
        // 这里直接去了第一个找到的服务地址
        String serviceAddress = CuratorHelper.getChildrenNodes(zkClient, serviceName).get(0);
        logger.info("成功找到服务地址：{}", serviceAddress);
        return new InetSocketAddress(serviceAddress.split(":")[0],
                                    Integer.parseInt(serviceAddress.split(":")[1]));
    }
}
