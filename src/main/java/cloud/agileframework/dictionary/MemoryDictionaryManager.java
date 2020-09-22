package cloud.agileframework.dictionary;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author 佟盟
 * 日期 2020/8/3 15:35
 * 描述 内存字典数据管理器
 * @version 1.0
 * @since 1.0
 */
public class MemoryDictionaryManager implements DictionaryDataManager {
    /**
     * 字典数据缓存
     */
    private static final Set<DictionaryData> CACHE = Sets.newHashSet();


    @Override
    public List<DictionaryData> all() {
        return new ArrayList<>(CACHE);
    }

    @Override
    public void add(DictionaryData dictionaryData) {
        CACHE.add(dictionaryData);
    }

    @Override
    public void delete(DictionaryData dictionaryData) {
        CACHE.removeIf(n -> dictionaryData.getId().equals(n.getId()));
    }

    @Override
    public void update(DictionaryData dictionaryData) {
        delete(dictionaryData);
        add(dictionaryData);
    }
}
