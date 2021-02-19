package cloud.agileframework.dictionary.config;

import cloud.agileframework.dictionary.DictionaryEngine;
import cloud.agileframework.dictionary.sync.RedisSyncCacheProxy;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author 佟盟
 * 日期 2021-02-02 19:28
 * 描述 redis作为缓存同步的介质
 * @version 1.0
 * @since 1.0
 */
@ConditionalOnProperty(name = "sync-cache", prefix = "agile.dictionary")
@AutoConfigureBefore(value = DictionaryAutoConfiguration.class)
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class")
@Configuration
public class RedisSynchronousAutoConfiguration {

    /**
     * redis初始化监听容器
     *
     * @param redisSyncCacheProxy    缓存同步监听器
     * @param redisConnectionFactory redis连接工厂
     * @return redis监听容器
     */
    @Bean
    RedisMessageListenerContainer container(MessageListener redisSyncCacheProxy,
                                            RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(new MessageListenerAdapter(redisSyncCacheProxy), new PatternTopic(DictionaryEngine.CHANNEL));
        return container;
    }

    /**
     * redis监听器
     *
     * @return redis监听器
     */
    @Bean
    RedisSyncCacheProxy syncCache() {
        return new RedisSyncCacheProxy();
    }
}
