package cloud.agileframework.dictionary.cache;

import com.google.common.collect.Maps;

import java.util.Map;

public class DictionaryCacheUtil {
    private static final Map<String, DictionaryCache> map = Maps.newConcurrentMap();

    public static void setDictionaryCache(String datasource, DictionaryCache dictionaryCache) {
        if (map.get(datasource) != null) {
            throw new IllegalArgumentException(datasource + "缓存设置重复");
        }
        map.put(datasource, dictionaryCache);
    }

    public static DictionaryCache getDictionaryCache(String datasource) throws NotFoundCacheException {
        DictionaryCache cache = map.get(datasource);
        if (cache == null) {
            throw new NotFoundCacheException(datasource + "缓存不存在");
        }
        return cache;
    }
}
