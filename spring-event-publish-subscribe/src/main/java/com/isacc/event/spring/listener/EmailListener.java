package com.isacc.event.spring.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * description
 *
 * @author isacc 2019/07/30 0:22
 * @since 1.0
 */
@Slf4j
@Component
public class EmailListener {

    @EventListener
    @Async
    @Order(1)
    public void handlerMessage(String message) {
        // 发送消息
        log.info("email：{}", message);
    }
}
