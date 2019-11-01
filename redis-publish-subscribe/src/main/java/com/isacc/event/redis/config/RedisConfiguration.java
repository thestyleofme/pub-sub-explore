package com.isacc.event.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isacc.event.redis.listener.CatListener;
import com.isacc.event.redis.listener.FishListener;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * <p>
 * description
 * </p>
 *
 * @author isacc 2019/09/04 17:25
 * @since 1.0
 */
@Configuration
@EnableCaching
public class RedisConfiguration extends CachingConfigurerSupport {

    /**
     * 驱动事件监听容器线程
     * <p>
     * 这里为了线程安全，用一个线程来监听。而且这里不需要很大的并发～
     *
     * @return SimpleAsyncTaskExecutor
     */
    @Bean
    public SimpleAsyncTaskExecutor simpleEventAsyncTaskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(1);
        taskExecutor.setDaemon(true);
        taskExecutor.setThreadNamePrefix("EventListener");
        return taskExecutor;
    }

    /**
     * redis消息监听器容器
     * 可以添加多个去监听不同话题的redis监听器，只需要把消息监听器和相应的消息订阅处理器绑定，
     * 该消息监听器通过反射技术调用消息订阅处理器的相关方法进行一些业务处理
     *
     * @param connectionFactory            RedisConnectionFactory
     * @param catAdapter                   MessageListenerAdapter
     * @param fishAdapter                  MessageListenerAdapter
     * @param simpleEventAsyncTaskExecutor SimpleAsyncTaskExecutor
     * @return RedisMessageListenerContainer
     */
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter catAdapter,
                                            MessageListenerAdapter fishAdapter,
                                            SimpleAsyncTaskExecutor simpleEventAsyncTaskExecutor) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 设置线程池
        container.setTaskExecutor(simpleEventAsyncTaskExecutor);
        // 这个container 可以添加多个 messageListener 订阅了一个叫cat 的通道
        container.addMessageListener(catAdapter, new PatternTopic("cat"));
        container.addMessageListener(fishAdapter, new PatternTopic("fish"));
        return container;
    }

    /**
     * 消息监听器适配器，绑定消息处理器
     *
     * @param redisTemplate RedisTemplate
     * @return MessageListenerAdapter
     */
    @Bean
    MessageListenerAdapter catAdapter(RedisTemplate redisTemplate) {
        return new MessageListenerAdapter(new CatListener(redisTemplate));
    }

    /**
     * 消息监听器适配器，绑定消息处理器
     *
     * @param redisTemplate RedisTemplate
     * @return MessageListenerAdapter
     */
    @Bean
    MessageListenerAdapter fishAdapter(RedisTemplate redisTemplate) {
        return new MessageListenerAdapter(new FishListener(redisTemplate));
    }

    @Bean
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 使用Jackson2JsonRedisSerialize 替换默认序列化
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        // 设置value的序列化规则和 key的序列化规则
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
