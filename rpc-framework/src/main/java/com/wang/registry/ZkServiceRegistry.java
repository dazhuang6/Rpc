package com.wang.registry;

import com.wang.utils.zk.CuratorHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

@Slf4j
public class ZkServiceRegistry implements ServiceRegistry{

    //InetSocketAddress类主要作用是封装端口,在InetAddress基础上加端口
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        //根节点下注册子节点：服务
        StringBuilder servicePath = new StringBuilder(CuratorHelper.ZK_REGISTER_ROOT_PATH).append("/").append(serviceName);
        //服务子节点下注册子节点：服务地址
        servicePath.append(inetSocketAddress.toString());
        CuratorHelper.creatEphemeralNode(servicePath.toString());
    }

}
