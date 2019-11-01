package com.isacc.event.zk.util;

import java.util.Objects;
import java.util.concurrent.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * <p>
 * 线程池不允许使用Executors去创建，而是通过ThreadPoolExecutor的方式，这样的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险。 说明：Executors各个方法的弊端：
 * 1）newFixedThreadPool和newSingleThreadExecutor:
 *   主要问题是堆积的请求处理队列可能会耗费非常大的内存，甚至OOM。
 * 2）newCachedThreadPool和newScheduledThreadPool:
 *   主要问题是线程数最大数是Integer.MAX_VALUE，可能会创建数量非常多的线程，甚至OOM。
 * </p>
 *
 * @author isacc 2019/07/29 16:29
 * @since 1.0
 */
public class ThreadPoolUtil {

    private static volatile ExecutorService executorService;

    private ThreadPoolUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * guava的方式
     */
    public static ExecutorService getExecutorService() {
        if (Objects.isNull(executorService)) {
            synchronized (ThreadPoolUtil.class) {
                /*
                 * 使用谷歌的guava框架
                 * ThreadPoolExecutor参数解释
                 *   1.corePoolSize 核心线程池大小
                 *   2.maximumPoolSize 线程池最大容量大小
                 *   3.keepAliveTime 线程池空闲时，线程存活的时间
                 *   4.TimeUnit 时间单位
                 *   5.ThreadFactory 线程工厂
                 *   6.BlockingQueue任务队列
                 *   7.RejectedExecutionHandler 线程拒绝策略
                 */
                if (Objects.isNull(executorService)) {
                    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("thread-pool-%d").build();
                    executorService = new ThreadPoolExecutor(10,
                            20,
                            0L,
                            TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
                }
            }
        }
        return executorService;
    }

    /**
     * spring的方式
     */
    public static Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        threadPool.setCorePoolSize(5);
        // 设置最大线程数
        threadPool.setMaxPoolSize(10);
        // 线程池所使用的缓冲队列
        threadPool.setQueueCapacity(25);
        // 设置线程活跃时间（秒）
        threadPool.setKeepAliveSeconds(60);
        // 等待所有任务结束后再关闭线程池
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        // 线程名称前缀
        threadPool.setThreadNamePrefix("thread-pool");
        // 初始化线程
        threadPool.initialize();
        return threadPool;
    }
}
