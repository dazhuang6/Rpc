package com.wang.utils.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CuratorHelper {
    private static final Logger logger = LoggerFactory.getLogger(CuratorHelper.class);
    private static final int SLEEP_MS_BETWEEN_RETRIES = 100;
    private static final int MAX_RETRIES = 3; //最大重试次数
    private static final String CONNECT_STRING = "127.0.0.1:2181";
    private static final int CONNECTION_TIMEOUT_MS = 10 * 1000;
    private static final int SESSION_TIMEOUT_MS = 60 * 1000;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    //存放以serviceName为key的所有子节点路径
    private static Map<String, List<String>> serviceAddressMap = new ConcurrentHashMap<>();

    private CuratorHelper() {
    }

    //framework框架， curator监护人
    public static CuratorFramework getZkClient() {
        // 重试策略，重试3次，并在两次重试之间等待100毫秒，以防出现连接问题。
        RetryPolicy retryPolicy = new RetryNTimes(
                MAX_RETRIES, SLEEP_MS_BETWEEN_RETRIES);

        return CuratorFrameworkFactory.builder()
                //要连接的服务器(可以是服务器列表)
                .connectString(CONNECT_STRING)
                .retryPolicy(retryPolicy)
                //连接超时时间，10秒
                .connectionTimeoutMs(CONNECTION_TIMEOUT_MS)
                //会话超时时间，60秒
                .sessionTimeoutMs(SESSION_TIMEOUT_MS)
                .build();
    }

    /**
     * 创建临时节点
     * 临时节点驻存在ZooKeeper中，当连接和session断掉时被删除。
     */
    public static void creatEphemeralNode(final CuratorFramework zkClient, final String path) {
        try {
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            logger.error("occur exception:", e);
        }
    }

    /**
     * 获取某个字节下的子节点,也就是获取所有提供服务的生产者的地址
     */
    public static List<String> getChildrenNodes(final CuratorFramework zkClient, final String serviceName){
        if (serviceAddressMap.containsKey(serviceName)) {
            return serviceAddressMap.get(serviceName);
        }
        List<String> result = Collections.emptyList();
        String servicePath = CuratorHelper.ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        try {
            result = zkClient.getChildren().forPath(servicePath); //获取节点的所有子节点路径
            serviceAddressMap.put(serviceName, result);
            registerWatcher(zkClient, serviceName); //对该节点注册监听器
        } catch (Exception e){
            logger.error("occur exception:", e);
        }
        return result;
    }

    /**
     * 子节点监听器
     * @param serviceName 服务名称
     */
    private static void registerWatcher(CuratorFramework zkClient, String serviceName){
        String servicePath = CuratorHelper.ZK_REGISTER_ROOT_PATH + "/" + serviceName;
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
            logger.error("occur exception:", e);
        }
    }
}
