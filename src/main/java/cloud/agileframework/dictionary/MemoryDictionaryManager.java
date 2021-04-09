package cloud.agileframework.dictionary;

import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.dictionary.util.DictionaryUtil;
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
public class MemoryDictionaryManager extends AbstractDictionaryDataManager<DictionaryDataBase> {
    /**
     * 字典数据缓存
     */
    public static final Set<DictionaryDataBase> CACHE = Sets.newHashSet();


    @Override
    public List<DictionaryDataBase> all() {
        return new ArrayList<>(CACHE);
    }

    @Override
    public String dataSource() {
        return DictionaryEngine.DICTIONARY_DATA_CACHE;
    }

    @Override
    public void add(DictionaryDataBase dictionaryDataBase) {
        CACHE.add(dictionaryDataBase);
    }

    @Override
    public void delete(DictionaryDataBase dictionaryDataBase) {
        CACHE.removeIf(n -> dictionaryDataBase.getId().equals(n.getId()));
    }

    @Override
    public DictionaryDataBase update(DictionaryDataBase dictionaryDataBase) {
        delete(dictionaryDataBase);
        add(dictionaryDataBase);
        return dictionaryDataBase;
    }

    @Override
    public DictionaryDataBase updateOfNotNull(DictionaryDataBase dictionaryDataBase) {
        DictionaryDataBase dic = DictionaryUtil.findById(dataSource(), dictionaryDataBase.getId());
        ObjectUtil.copyProperties(dictionaryDataBase,dic, ObjectUtil.Compare.DIFF_SOURCE_NOT_NULL);
        update(dic);
        return dic;
    }
}
