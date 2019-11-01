package com.isacc.event.zk;

import java.util.concurrent.ExecutorService;

import com.isacc.event.zk.util.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

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
public class ZookeeperConnectorByZkClient implements Runnable {

    /**
     * 配置文件存放的节点
     */
    private static final String CONF_PATH = "/configuration";
    /**
     * zk的url
     */
    private static final String ZK_URL = "localhost:2181";

    private static ZkClient zkClient;

    static {
        // 初始化
        zkClient = new ZkClient(ZK_URL, 1000, 1000, new SerializableSerializer());
        if (zkClient.exists(CONF_PATH)) {
            zkClient.delete(CONF_PATH);
        }
        zkClient.createPersistent(CONF_PATH);
        zkClient.writeData(CONF_PATH, "192.168.11.200");
    }


    @Override
    public void run() {
        // 1. 读取初始配置
        String initData = zkClient.readData(CONF_PATH);
        log.info("======={}开始监听，初始值为：{}=======", Thread.currentThread().getName(), initData);
        // 2. 定义监听器
        IZkDataListener listener = new IZkDataListener() {

            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                if (data == null) {
                    return;
                }
                if (!initData.equals(data)) {
                    String changeData = zkClient.readData(CONF_PATH);
                    log.info("=======node: {}changed，new data：{}=======", dataPath, changeData);
                }
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                log.info("=======node: {} deleted=======", dataPath);
            }
        };
        // 3. 给配置节点新增监听器
        zkClient.subscribeDataChanges(CONF_PATH, listener);
    }

    public static void main(String[] args)  {
        ExecutorService executorService = ThreadPoolUtil.getExecutorService();
        executorService.execute(new ZookeeperConnectorByZkClient());
        executorService.execute(new ZookeeperConnectorByZkClient());
        executorService.execute(new ZookeeperConnectorByZkClient());
        executorService.execute(new ZookeeperConnectorByZkClient());
        executorService.execute(new ZookeeperConnectorByZkClient());
        try {
            // 主线程等待子线程结束
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("InterruptedException",e);
        }
        zkClient.close();
    }
}
