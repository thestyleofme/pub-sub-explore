package com.isacc.event.redis.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * <p>
 * description
 * </p>
 *
 * @author isacc 2019/09/04 17:21
 * @since 1.0
 */
@Component
@Slf4j
public class FishListener implements MessageListener {

    private final RedisTemplate redisTemplate;

    public FishListener(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        log.debug("FishListener message: {}", message.toString());
    }
}
