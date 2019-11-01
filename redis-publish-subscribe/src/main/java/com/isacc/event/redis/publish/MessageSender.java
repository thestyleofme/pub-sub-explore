package com.isacc.event.redis.publish;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 创建一个定时器模拟发布消息
 * </p>
 *
 * @author isacc 2019/09/04 17:37
 * @since 1.0
 */
@EnableScheduling
@Component
public class MessageSender {

    private final StringRedisTemplate stringRedisTemplate;

    public MessageSender(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 间隔2s 通过StringRedisTemplate对象向redis消息队列cat频道发布消息
     */
    @Scheduled(fixedRate = 5000)
    public void sendCatMessage() {
        stringRedisTemplate.convertAndSend("cat", "i am cat");
    }

    /**
     * 间隔1s 通过StringRedisTemplate对象向redis消息队列fish频道发布消息
     */
    @Scheduled(fixedRate = 10000)
    public void sendFishMessage() {
        stringRedisTemplate.convertAndSend("fish", "i am fish");
    }

}
