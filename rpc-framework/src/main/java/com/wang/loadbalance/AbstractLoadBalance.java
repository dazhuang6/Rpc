package com.wang.loadbalance;

import java.util.List;

/**
 * 定义抽象类确定本质
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    //抽象类中可以有具体的方法和属性
    @Override
    public String selectServiceAddress(List<String> serviceAddresses) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses);
    }

    protected abstract String doSelect(List<String> serviceAddresses);
}
