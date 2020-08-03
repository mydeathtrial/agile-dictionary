package com.agile.common.dictionary;

import com.agile.common.util.DictionaryUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author 佟盟
 * 日期 2020/8/3 16:27
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class DictionaryDataManagerProxy {
    private DictionaryDataManager dictionaryDataManager;

    public DictionaryDataManagerProxy(DictionaryDataManager dictionaryDataManager) {
        this.dictionaryDataManager = dictionaryDataManager;
    }

    public List<DictionaryData> all() {
        return dictionaryDataManager.all();
    }

    public void add(DictionaryData dictionaryData) {
        dictionaryDataManager.add(dictionaryData);
        add(DictionaryEngine.CODE_CACHE, dictionaryData, DictionaryData::getFullCode);
        add(DictionaryEngine.NAME_CACHE, dictionaryData, DictionaryData::getFullName);
    }

    private void add(String cacheKey, DictionaryData dictionaryData, Function<DictionaryData, String> function) {
        Map<String, DictionaryData> map = DictionaryUtil.getCache().get(cacheKey, Map.class);
        assert map != null;
        map.values().stream()
                .filter(n -> n.getId().equals(dictionaryData.getParentId()))
                .forEach(n -> {
                    dictionaryData.setFullCode(n.getFullCode() + DictionaryEngine.DEFAULT_SPLIT_CHAR + dictionaryData.getCode());
                    dictionaryData.setFullName(n.getFullName() + DictionaryEngine.DEFAULT_SPLIT_CHAR + dictionaryData.getName());
                    List<DictionaryData> children = dictionaryData.getChildren();
                    if (children != null) {
                        children.forEach(this::add);
                        n.getChildren().add(dictionaryData);
                    }
                });
        map.put(function.apply(dictionaryData), dictionaryData);
        DictionaryUtil.getCache().put(cacheKey, map);
    }

    public void delete(String fullCode) {
        dictionaryDataManager.delete(fullCode);
        DictionaryData dictionaryData = DictionaryUtil.coverDicBean(fullCode);
        delete(dictionaryData);
    }

    private void delete(DictionaryData dictionaryData) {
        delete(DictionaryEngine.CODE_CACHE, dictionaryData, dictionaryData.getFullCode());
        delete(DictionaryEngine.NAME_CACHE, dictionaryData, dictionaryData.getFullName());
    }

    private void delete(String cacheKey, DictionaryData dictionaryData, String removeKey) {
        Map<String, DictionaryData> map = DictionaryUtil.getCache().get(cacheKey, Map.class);
        assert map != null;
        map.values().removeIf(n -> dictionaryData.getId().equals(n.getId()));
        map.remove(removeKey);
    }

    public void update(DictionaryData dictionaryData) {
        dictionaryDataManager.update(dictionaryData);
        delete(dictionaryData);
        add(dictionaryData);
    }
}
