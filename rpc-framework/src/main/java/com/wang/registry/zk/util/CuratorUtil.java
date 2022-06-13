package com.wang.registry.zk.util;

import com.wang.enumeration.RpcProperties;
import com.wang.exception.RpcException;
import com.wang.utils.file.PropertiesFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class CuratorUtil {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3; //最大重试次数
    private static String defaultZookeeperAddress = "127.0.0.1:2181";

    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    //存放以serviceName为key的所有子节点路径
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();

    private static CuratorFramework zkClient;

    //用于存放路径，情况注册的服务
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    private CuratorUtil() {
    }

    //framework框架， curator监护人
    public static CuratorFramework getZkClient() {
        // check if user has set zk address
        Properties properties = PropertiesFileUtils.readPropertiesFile(RpcProperties.RPC_CONFIG_PATH.getPropertyValue());
        if (properties != null) {
            defaultZookeeperAddress = properties.getProperty(RpcProperties.ZK_ADDRESS.getPropertyValue());
        }
        // if zkClient has been started, return directly
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                // the server to connect to (can be a server list)
                .connectString(defaultZookeeperAddress)
                .authorization("digest", "user1:123456".getBytes())
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        return zkClient;
    }

    /**
     * 创建临时节点
     * 临时节点驻存在ZooKeeper中，当连接和session断掉时被删除。
     *
     * 创建持久化节点。不同于临时节点，持久化节点不会因为客户端断开连接而被删除
     *
     */
    public static void creatPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (!REGISTERED_PATH_SET.contains(path) && zkClient.checkExists().forPath(path) == null) {
                //eg: /my-rpc/rpc.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("节点创建成功，节点为:[{}]", path);
                REGISTERED_PATH_SET.add(path);
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
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String serviceName){
        if (SERVICE_ADDRESS_MAP.containsKey(serviceName)) {
            return SERVICE_ADDRESS_MAP.get(serviceName);
        }
        List<String> result = Collections.emptyList();
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        try {
            result = zkClient.getChildren().forPath(servicePath); //获取节点的所有子节点路径
            SERVICE_ADDRESS_MAP.put(serviceName, result);
            registerWatcher(serviceName, zkClient); //对该节点注册监听器
        } catch (Exception e){
            throw new RpcException(e.getMessage(), e.getCause());
        }
        return result;
    }

    /**
     * 子节点监听器
     * @param serviceName 服务名称
     */
    private static void registerWatcher(String serviceName, CuratorFramework zkClient){
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        //给某个节点注册子节点监听器。之后，这个节点的子节点发生变化的时候可以自定义回调操作。
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(serviceName, serviceAddresses);
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
    public static void clearRegistry(CuratorFramework zkClient) {
        REGISTERED_PATH_SET.stream().parallel().forEach( p -> {
            try {
                zkClient.delete().forPath(p);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e.getCause());
            }
        });
        log.info("服务端（Provider）所有注册的服务都被清空：[{}]", REGISTERED_PATH_SET.toString());
    }
}
