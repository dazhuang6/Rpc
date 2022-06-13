package com.wang.registry.zk;

import com.wang.loadbalance.LoadBalance;
import com.wang.loadbalance.RandomLoadBalance;
import com.wang.registry.ServiceDiscovery;
import com.wang.registry.zk.util.CuratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        this.loadBalance = new RandomLoadBalance();
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        List<String> serviceUrlList = CuratorUtil.getChildrenNodes(zkClient, serviceName);
        // 负载均衡 eg:127.0.0.1:9999
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList);
        // 这里直接去了第一个找到的服务地址,
        //String serviceAddress = CuratorUtil.getChildrenNodes(serviceName).get(0);
        log.info("成功找到服务地址：[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }

}
