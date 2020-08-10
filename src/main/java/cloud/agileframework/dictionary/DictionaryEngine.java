package cloud.agileframework.dictionary;

import cloud.agileframework.dictionary.util.DictionaryUtil;
import com.agile.common.util.collection.TreeUtil;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;
import java.util.Map;

/**
 * @author 佟盟
 * 日期 2019/3/18 18:30
 * 描述 字典服务
 * @version 1.0
 * @since 1.0
 */
public class DictionaryEngine implements ApplicationRunner {

    public static final String CODE_CACHE = "codeCache";
    public static final String NAME_CACHE = "nameCache";
    public static final String DEFAULT_SPLIT_CHAR = "$SPLIT$";

    @Autowired
    private DictionaryDataManager dictionaryDataManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<DictionaryData> list = dictionaryDataManager.all();
        TreeUtil.createTree(list,
                "id",
                "parentId",
                "children",
                "code",
                null,
                DEFAULT_SPLIT_CHAR,
                "fullName",
                "fullCode"
        );

        refreshCache(list);
    }

    /**
     * 刷新字典缓存
     *
     * @param list 字典数据
     */
    public void refreshCache(List<DictionaryData> list) {
        Map<String, DictionaryData> codeCache = Maps.newHashMapWithExpectedSize(list.size());
        Map<String, DictionaryData> nameCache = Maps.newHashMapWithExpectedSize(list.size());
        list.forEach(dic -> {
            codeCache.put(dic.getFullCode(), dic);
            nameCache.put(dic.getFullName(), dic);
        });
        DictionaryUtil.getCache().put(CODE_CACHE, codeCache);
        DictionaryUtil.getCache().put(NAME_CACHE, nameCache);
    }
}
