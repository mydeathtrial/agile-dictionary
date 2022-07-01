package cloud.agileframework.dictionary;

import cloud.agileframework.common.util.collection.TreeUtil;
import cloud.agileframework.dictionary.cache.DictionaryCacheUtil;
import cloud.agileframework.dictionary.cache.NotFoundCacheException;
import cloud.agileframework.dictionary.util.DictionaryUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author 佟盟
 * 日期 2019/3/18 18:30
 * 描述 字典服务
 * @version 1.0
 * @since 1.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DictionaryEngine implements ApplicationRunner, ApplicationContextAware {

    /**
     * 字典内置层级分隔符
     */
    public static final String DEFAULT_SPLIT_CHAR = "$$";
    /**
     * 默认的字典区域（datasource）
     */
    public static final String DICTIONARY_DATA_CACHE = "DICTIONARY_DATA_CACHE";

    private ApplicationContext applicationContext;

    /**
     * 字典数据管理器缓存
     */
    private static Map<String,DictionaryDataManager<? extends DictionaryDataBase>> dictionaryDataManagerMap = Maps.newConcurrentMap();

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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

    }

    /**
     * 处理单个数据源，每个字典管理器都会对应一个数据源
     *
     * @param dictionaryDataManager 字典管理其
     */
    private void parseDataSource(DictionaryDataManager<?> dictionaryDataManager) throws NotFoundCacheException {
        //设置缓存操作
        DictionaryCacheUtil.setDictionaryCache(dictionaryDataManager.dataSource(), dictionaryDataManager.cache());
        
        //初始化缓存
        dictionaryDataManagerMap.put(dictionaryDataManager.dataSource(),dictionaryDataManager);

        //如果缓存中没有，则初始化
        SortedSet<DictionaryDataBase> treeSet = new ConcurrentSkipListSet<>(dictionaryDataManager.all());

        //判断是否重复处理
        if (isFinish(dictionaryDataManager.dataSource(), treeSet)) {
            return;
        }

        //初始化全字典值与字典码默认值
        treeSet.forEach(dic -> {
            dic.setFullCode(dic.getCode());
            dic.setFullName(dic.getName());
            dic.setFullId(dic.getId());
        });

        //构建树形结构，过程中重新计算全字典值与全字典码
        TreeUtil.createTree(treeSet,
                dictionaryDataManager.rootParentId(),
                DEFAULT_SPLIT_CHAR,
                "fullName", "fullCode","fullId"
        );

        //做缓存同步
        dictionaryDataManager.cache().initData(dictionaryDataManager.dataSource(),treeSet);
    }

    /**
     * 判断本次新产生的数据是否做过缓存同步，避免重复处理
     *
     * @param dataSource 数据源
     * @param newData    新产生的数据
     * @return true 已处理过
     */
    private boolean isFinish(String dataSource, SortedSet<DictionaryDataBase> newData) {
        SortedSet<DictionaryDataBase> old = DictionaryUtil.findAll(dataSource);
        return CollectionUtils.isEqualCollection(newData, old);
    }

    /**
     * 根据数据源获取对应的字典数据管理器
     * @param datasource 数据源
     * @return 字典数据管理器
     */
    public static DictionaryDataManager<? extends DictionaryDataBase> getDictionaryDataManagerMap(String datasource) {
        return dictionaryDataManagerMap.get(datasource);
    }
}
