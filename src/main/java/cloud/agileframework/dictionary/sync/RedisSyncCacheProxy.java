package cloud.agileframework.dictionary.sync;

import cloud.agileframework.dictionary.DictionaryEngine;
import lombok.SneakyThrows;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author 佟盟
 * 日期 2021-02-19 10:37
 * 描述 Redis作为缓存介质的缓存同步代理工具
 * @version 1.0
 * @since 1.0
 */
public class RedisSyncCacheProxy implements SyncCache, MessageListener {
    /**
     * redis操作工具，用于广播
     */
    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public void notice(int newCacheVersion) {
        //发布
        redisTemplate.convertAndSend(DictionaryEngine.CHANNEL, String.valueOf(newCacheVersion));
    }

    @Override
    public void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value.toString());
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean lock() {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(SYNC_LOCK_CACHE_KEY, SYNC_LOCK_CACHE_KEY, Duration.ofMinutes(2).toMillis(), TimeUnit.SECONDS);
        return success != null && success;
    }

    @Override
    public void unlock() {
        redisTemplate.delete(SYNC_LOCK_CACHE_KEY);
    }

    @SneakyThrows
    @Override
    public void onMessage(Message message, byte[] pattern) {
        Object channel = redisTemplate.getKeySerializer().deserialize(message.getChannel());
        if (!DictionaryEngine.CHANNEL.equals(channel)) {
            return;
        }

        //提取通知的最新版本号
        Object noticeVersionData = redisTemplate.getValueSerializer().deserialize(message.getBody());
        if (noticeVersionData == null || !NumberUtils.isCreatable(noticeVersionData.toString())) {
            //接收到非数字，不做同步
            throw new DictionarySyncException("Notification content does not conform to version number format");
        }
        int noticeVersion = NumberUtils.toInt(noticeVersionData.toString());

        //按照缓存的版本号进行同步
        message(noticeVersion);
    }
}
