package com.wang.registry.zk;

import com.wang.registry.ServiceRegistry;
import com.wang.registry.zk.util.CuratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    //InetSocketAddress类主要作用是封装端口,在InetAddress基础上加端口
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        //根节点下注册子节点：服务
        //服务子节点下注册子节点：服务地址
        String servicePath = CuratorUtil.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        CuratorUtil.creatPersistentNode(zkClient, servicePath);
    }

}
