package cloud.agileframework.dictionary;

import cloud.agileframework.common.util.collection.TreeUtil;
import cloud.agileframework.dictionary.sync.SyncCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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
public class DictionaryEngine implements ApplicationRunner {

    public static final String DEFAULT_SPLIT_CHAR = "$SPLIT$";
    public static final String CHANNEL = "dictionary-channel";
    private final String rootValue;
    private static final Map<String, DictionaryDataBase> CODE_MEMORY = Maps.newConcurrentMap();
    private static final Map<String, DictionaryDataBase> NAME_MEMORY = Maps.newConcurrentMap();
    private static final List<DictionaryDataBase> ALL_MEMORY = Lists.newArrayList();
    /**
     * 缓存同步工具
     */
    @Autowired
    private SyncCache cacheSync;

    /**
     * 字典持久层操作工具
     */
    @Autowired
    private DictionaryDataManager dictionaryDataManager;

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

    public DictionaryEngine(String rootValue) {
        this.rootValue = rootValue;
    }

    @Override
    public void run(ApplicationArguments args) {
        cacheSync.sync(() -> {
            try {
                //如果有缓存，直接用
                boolean success = cacheSync.cacheToMemory();

                if (success) {
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
                        rootValue,
                        DEFAULT_SPLIT_CHAR,
                        "fullName", "fullCode"
                );

                ALL_MEMORY.addAll(list);

                cacheSync.memoryToCache();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, false);
    }

    public static Map<String, DictionaryDataBase> getCodeMemory() {
        return CODE_MEMORY;
    }

    public static Map<String, DictionaryDataBase> getNameMemory() {
        return NAME_MEMORY;
    }

    public static List<DictionaryDataBase> getAllMemory() {
        return ALL_MEMORY;
    }
}
