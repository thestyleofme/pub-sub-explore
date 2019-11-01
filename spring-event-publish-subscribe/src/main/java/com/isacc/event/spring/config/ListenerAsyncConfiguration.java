package com.isacc.event.spring.config;

import java.util.concurrent.Executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * description
 *
 * @author isacc 2019/07/30 10:58
 * @since 1.0
 */
@Configuration
@EnableAsync
@Slf4j
public class ListenerAsyncConfiguration implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
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
        threadPool.setThreadNamePrefix("CoreTaskExecutor");
        // 初始化线程
        threadPool.initialize();
        return threadPool;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        log.error("getAsyncUncaughtExceptionHandler");
        return null;
    }
}
