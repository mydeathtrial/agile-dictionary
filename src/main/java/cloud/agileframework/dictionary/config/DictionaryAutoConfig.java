package cloud.agileframework.dictionary.config;

import cloud.agileframework.dictionary.DictionaryDataManager;
import cloud.agileframework.dictionary.DictionaryDataManagerProxy;
import cloud.agileframework.dictionary.DictionaryEngine;
import cloud.agileframework.dictionary.MemoryDictionaryManager;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 佟盟
 * 日期 2020/7/30 19:46
 * 描述 字典配置
 * @version 1.0
 * @since 1.0
 */
@Configuration
@AutoConfigureBefore(name="cloud.agileframework.jpa.config.DaoAutoConfiguration.class")
@ConditionalOnProperty(name = "enable", prefix = "agile.dictionary")
public class DictionaryAutoConfig {
    public DictionaryAutoConfig() {
    }

    /**
     * 字典引擎
     *
     * @return 注入字典引擎
     */
    @Bean
    DictionaryEngine dictionaryEngine() {
        return new DictionaryEngine();
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
}
