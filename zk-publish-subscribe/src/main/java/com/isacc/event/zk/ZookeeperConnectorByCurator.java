package com.isacc.event.zk;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

/**
 * zk连接器
 * <p>1. 连接zk获取最新的配置信息</p>
 * <p>2. 给configuration加上watch</p>
 *
 * @author isacc 2019/07/25 21:18
 * @since 1.0
 */
@SuppressWarnings("unused")
@Slf4j
public class ZookeeperConnectorByCurator implements Runnable {

    private static ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 配置文件存放的节点
     */
    private static final String CONF_PATH = "/configuration";
    /**
     * zk的url
     */
    private static final String ZK_URL = "localhost:2181";

    private static CuratorFramework zkClient;

    static {
        // 创建 CuratorFramework实例
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        zkClient = CuratorFrameworkFactory.newClient(ZK_URL, 1000, 1000, retryPolicy);
        try {
            // 启动
            zkClient.start();
            // avoid PathChildrenCache connected events
            zkClient.blockUntilConnected();
            Stat stat = zkClient.checkExists().forPath(CONF_PATH);
            if (Objects.isNull(stat)) {
                // 创建节点并赋初始值
                zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(CONF_PATH, "192.168.11.200".getBytes());
            } else {
                // 重新赋值
                zkClient.setData().forPath(CONF_PATH, "192.168.11.200".getBytes());
            }
        } catch (Exception e) {
            log.error("create node error", e);
        }
    }

    @Override
    public void run() {
        // 1. 读取初始配置
        String initData;
        // 创建PathChildrenCache
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, CONF_PATH, true);
        try {
            initData = new String(zkClient.getData().forPath(CONF_PATH));
            // 2. 设置watch
            log.info("=======开始监听，初始值为：{}=======", initData);
            // useWatcher();
            // useCuratorWatcher();
            usePathChildrenCache(pathChildrenCache);
            // 3. 更新值
            log.info("=======更新或新增值=======");
            // PathChildrenCache对指定的路径节点的一级子目录进行监听，不对该节点的操作进行监听，对其子目录的节点进行增、删、改的操作监听
            zkClient.setData().forPath(CONF_PATH, "192.168.11.227".getBytes());
            // 为了方便测试 添加临时节点
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(CONF_PATH + "/kettle", "KETTLE".getBytes());
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(CONF_PATH + "/datax", "DATAX".getBytes());
            Thread.sleep(3000L);
            // 4. 删除值
            log.info("=======删除值=======");
            zkClient.delete().forPath(CONF_PATH + "/kettle");
            // 等待3秒，看是否监听成功
            Thread.sleep(3000L);
        } catch (Exception e) {
            log.error("get data error", e);
        } finally {
            // 关闭
            CloseableUtils.closeQuietly(pathChildrenCache);
            CloseableUtils.closeQuietly(zkClient);
        }
    }

    /**
     * 不能监听到节点删除事件
     */
    private void useWatcher() throws Exception {
        zkClient.getData().usingWatcher((Watcher) watchedEvent -> {
            String watchedEventStr;
            try {
                watchedEventStr = objectMapper.writeValueAsString(watchedEvent);
                log.info(String.format("=======监听到%s事件： %s=======", watchedEvent.getType(), watchedEventStr));
                // log.info(String.format("=======新值为：%s=======", new String(zkClient.getData().forPath(CONF_PATH))));
            } catch (Exception e) {
                log.error("Jso nProcessing Exception", e);
            }
        }).forPath(CONF_PATH);
    }

    /**
     * 不能监听到节点删除事件
     */
    private void useCuratorWatcher() throws Exception {
        zkClient.getData().usingWatcher((CuratorWatcher) event -> {
            String watchedEventStr = objectMapper.writeValueAsString(event);
            log.info(String.format("=======监听到%s事件： %s=======", event.getType(), watchedEventStr));
            // log.info(String.format("=======新值为：%s=======", new String(zkClient.getData().forPath(CONF_PATH))));
        }).forPath(CONF_PATH);
    }

    /**
     * <p>
     * 1) NodeCache: 对一个节点进行监听，监听事件包括指定的路径节点的增、删、改的操作。
     * 2) PathChildrenCache: 对指定的路径节点的一级子目录进行监听，不对该节点的操作进行监听，对其子目录的节点进行增、删、改的操作监听
     * 3) TreeCache:  可以将指定的路径节点作为根节点（祖先节点），对其所有的子节点操作进行监听，呈现树形目录的监听，可以设置监听深度，最大监听深度为2147483647（int类型的最大值）。
     * </p>
     */
    private void usePathChildrenCache(PathChildrenCache pathChildrenCache) throws Exception {
        // 触发INITIALIZED类型的事件，其他的与NORMAL一致
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        // 添加监听事件
        pathChildrenCache.getListenable().addListener((curatorFramework, pathChildrenCacheEvent) -> {
                    if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.INITIALIZED) {
                        log.info("PathChildrenCache初始化");
                    } else {
                        log.info(String.format("pathChildrenCache发生的节点变化类型为：%s,发生变化的节点内容为：%s,路径：%s",
                                pathChildrenCacheEvent.getType(),
                                new String(pathChildrenCacheEvent.getData().getData()),
                                pathChildrenCacheEvent.getData().getPath()));
                    }
                }
        );
    }


    public static void main(String[] args) {
        ZookeeperConnectorByCurator zookeeperConnectorByCurator = new ZookeeperConnectorByCurator();
        zookeeperConnectorByCurator.run();
    }
}
