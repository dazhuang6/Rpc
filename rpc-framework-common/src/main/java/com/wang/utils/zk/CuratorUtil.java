package com.wang.utils.zk;

import com.wang.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class CuratorUtil {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3; //最大重试次数
    private static final String CONNECT_STRING = "127.0.0.1:2181";

    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    //存放以serviceName为key的所有子节点路径
    private static Map<String, List<String>> serviceAddressMap = new ConcurrentHashMap<>();

    private static CuratorFramework zkClient = getZkClient();

    //用于存放路径，情况注册的服务
    private static Set<String> registeredPathSet = ConcurrentHashMap.newKeySet();

    private CuratorUtil() {
    }

    //framework框架， curator监护人
    public static CuratorFramework getZkClient() {
        // 重试策略，重试3次，并在两次重试之间等待100毫秒，以防出现连接问题。
        RetryPolicy retryPolicy = new RetryNTimes(BASE_SLEEP_TIME, MAX_RETRIES);

        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                //要连接的服务器(可以是服务器列表)
                .connectString(CONNECT_STRING)
                .retryPolicy(retryPolicy)
                .build();
        curatorFramework.start();
        return curatorFramework;
    }

    /**
     * 创建临时节点
     * 临时节点驻存在ZooKeeper中，当连接和session断掉时被删除。
     *
     * 创建持久化节点。不同于临时节点，持久化节点不会因为客户端断开连接而被删除
     *
     */
    public static void creatPersistentNode(String path) {
        try {
            if (!registeredPathSet.contains(path) && zkClient.checkExists().forPath(path) == null) {
                //eg: /my-rpc/rpc.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("节点创建成功，节点为:[{}]", path);
                registeredPathSet.add(path);
            } else {
                log.info("节点已经存在，节点为:[{}]", path);
            }
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 获取某个字节下的子节点,也就是获取所有提供服务的生产者的地址
     */
    public static List<String> getChildrenNodes(String serviceName){
        if (serviceAddressMap.containsKey(serviceName)) {
            return serviceAddressMap.get(serviceName);
        }
        List<String> result = Collections.emptyList();
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        try {
            result = zkClient.getChildren().forPath(servicePath); //获取节点的所有子节点路径
            serviceAddressMap.put(serviceName, result);
            registerWatcher(zkClient, serviceName); //对该节点注册监听器
        } catch (Exception e){
            throw new RpcException(e.getMessage(), e.getCause());
        }
        return result;
    }

    /**
     * 子节点监听器
     * @param serviceName 服务名称
     */
    private static void registerWatcher(CuratorFramework zkClient, String serviceName){
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        //给某个节点注册子节点监听器。之后，这个节点的子节点发生变化的时候可以自定义回调操作。
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            serviceAddressMap.put(serviceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e){
            log.error("occur exception:", e);
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 清空注册中心的数据
     */
    public static void clearRegistry() {
        registeredPathSet.stream().parallel().forEach( p -> {
            try {
                zkClient.delete().forPath(p);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e.getCause());
            }
        });
        log.info("服务端（Provider）所有注册的服务都被清空：[{}]", registeredPathSet.toString());
    }
}
