package cloud.agileframework.dictionary;

import cloud.agileframework.cache.support.AgileCache;
import cloud.agileframework.cache.util.CacheUtil;
import cloud.agileframework.common.util.collection.TreeUtil;
import cloud.agileframework.spring.util.BeanUtil;
import com.google.common.collect.Maps;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;

/**
 * @author 佟盟
 * 日期 2019/3/18 18:30
 * 描述 字典服务
 * @version 1.0
 * @since 1.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DictionaryEngine implements ApplicationRunner, ApplicationContextAware {

    public static final String DEFAULT_SPLIT_CHAR = "$SPLIT$";
    public static final String CHANNEL = "dictionary-channel";
    public static final String ALL_MEMORY = "ALL_MEMORY";
    public static final String CODE_MEMORY = "CODE_MEMORY";
    public static final String NAME_MEMORY = "NAME_MEMORY";
    public static final String ROOT_VALUE = BeanUtil.getBean(DictionaryProperties.class).getRootParentId();
    public static final String DICTIONARY_DATA_CACHE = "DICTIONARY_DATA_CACHE";

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 缓存种类
     */
    public enum CacheType {
        /**
         * 字典码缓存
         */
        CODE_CACHE,
        /**
         * 字典值缓存
         */
        NAME_CACHE
    }

    @Override
    public void run(ApplicationArguments args) {
        applicationContext.getBeanProvider(DictionaryDataManager.class)
                .orderedStream()
                .forEach(cm -> {
                    try {
                        parseDataSource(cm);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });

    }

    private void parseDataSource(DictionaryDataManager dictionaryDataManager) throws IllegalAccessException {

        if (isFinish(dictionaryDataManager.dataSource())) {
            return;
        }
        //如果缓存中没有，则初始化
        List<DictionaryDataBase> list = dictionaryDataManager.all();

        list.forEach(dic -> {
            dic.setFullCode(dic.getCode());
            dic.setFullName(dic.getName());
        });

        TreeUtil.createTree(list,
                "id",
                "parentId",
                "children",
                "code",
                ROOT_VALUE,
                DEFAULT_SPLIT_CHAR,
                "fullName", "fullCode"
        );

        String dataSource = dictionaryDataManager.dataSource();
        AgileCache cache = CacheUtil.getCache(dataSource);
        cache.put(ALL_MEMORY, list);

        Map<String, DictionaryDataBase> codeMap = Maps.newHashMap();
        Map<String, DictionaryDataBase> nameMap = Maps.newHashMap();

        list.forEach(dic -> {
            codeMap.put(dic.getFullCode(), dic);
            nameMap.put(dic.getFullName(), dic);
        });

        cache.put(CODE_MEMORY, codeMap);
        cache.put(NAME_MEMORY, nameMap);
    }

    private boolean isFinish(String dataSource) {
        AgileCache cache = CacheUtil.getCache(dataSource);
        return cache.containKey(ALL_MEMORY);
    }
}
