package cloud.agileframework.dictionary.config;

import cloud.agileframework.dictionary.DictionaryDataManager;
import cloud.agileframework.dictionary.DictionaryDataManagerProxy;
import cloud.agileframework.dictionary.DictionaryEngine;
import cloud.agileframework.dictionary.DictionaryProperties;
import cloud.agileframework.dictionary.MemoryDictionaryManager;
import cloud.agileframework.dictionary.sync.SyncCache;
import com.google.common.collect.Maps;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author 佟盟
 * 日期 2020/7/30 19:46
 * 描述 字典配置
 * @version 1.0
 * @since 1.0
 */
@Configuration
@AutoConfigureBefore(name = "cloud.agileframework.jpa.config.DaoAutoConfiguration.class")
@ConditionalOnProperty(name = "enable", prefix = "agile.dictionary", matchIfMissing = true)
@EnableConfigurationProperties(DictionaryProperties.class)
public class DictionaryAutoConfiguration {
    /**
     * 字典引擎
     *
     * @return 注入字典引擎
     */
    @Bean
    DictionaryEngine dictionaryEngine(DictionaryProperties dictionaryProperties) {
        return new DictionaryEngine(dictionaryProperties.getRootParentId());
    }

    /**
     * 字典数据管理器
     *
     * @return 内存字典数据管理器
     */
    @Bean
    @ConditionalOnMissingBean(DictionaryDataManager.class)
    MemoryDictionaryManager dictionaryManager() {
        return new MemoryDictionaryManager();
    }

    /**
     * 创建字典数据管理器代理对象
     *
     * @return 字典数据管理器代理对象
     */
    @Bean
    @ConditionalOnBean(DictionaryDataManager.class)
    DictionaryDataManagerProxy dictionaryDataManagerProxy(DictionaryDataManager dictionaryDataManager) {
        return new DictionaryDataManagerProxy(dictionaryDataManager);
    }

    @Bean
    @ConditionalOnMissingBean(SyncCache.class)
    SyncCache syncCache() {
        return new SyncCache() {
            private final Map<String, Object> cache = Maps.newConcurrentMap();

            @Override
            public void notice(int newCacheVersion) {
                //默认不做任何动作
            }

            @Override
            public void put(String key, Object value) {
                cache.put(key, value);
            }

            @Override
            public Object get(String key) {
                return cache.get(key);
            }

        };
    }
}
