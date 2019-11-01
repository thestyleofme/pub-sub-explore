package com.isacc.event.spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * description
 *
 * @author isacc 2019/07/30 0:24
 * @since 1.0
 */
@Service
@Slf4j
public class SomeService {

    private final ApplicationContext context;

    public SomeService(ApplicationContext context) {
        this.context = context;
    }

    public void doSomething(String thing) {
        // 主业务的逻辑
        log.info("do something：{}", thing);
        // 发布事件，让订阅者去处理，异步处理，不然还是当前线程处理，有异常会报错，若是有事务，会回滚的
        context.publishEvent(thing);
        log.info("do something done");
    }
}
