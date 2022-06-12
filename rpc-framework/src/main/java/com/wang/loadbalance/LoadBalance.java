package com.wang.loadbalance;

import java.util.List;

/**
 * 定义接口实现选择服务地址的这个动作
 */
public interface LoadBalance {

    //在已有服务提供地址列表中选择一个
    String selectServiceAddress(List<String>serviceAddresses);
}
