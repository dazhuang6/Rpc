package com.wang.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * 实现负载均衡
 */
public class RandomLoadBalance extends AbstractLoadBalance{

    @Override
    protected String doSelect(List<String> serviceAddresses) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
