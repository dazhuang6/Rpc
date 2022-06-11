package com.wang.registry;

import com.wang.utils.zk.CuratorUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ZkServiceRegistry implements ServiceRegistry{

    //InetSocketAddress类主要作用是封装端口,在InetAddress基础上加端口
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        //根节点下注册子节点：服务
        //服务子节点下注册子节点：服务地址
        String servicePath = CuratorUtil.ZK_REGISTER_ROOT_PATH + "/" + serviceName + inetSocketAddress.toString();
        CuratorUtil.creatPersistentNode(servicePath);
    }

}
