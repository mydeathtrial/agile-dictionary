package cloud.agileframework.dictionary.cache;

import cloud.agileframework.cache.support.AgileCache;
import cloud.agileframework.cache.sync.SyncCache;
import cloud.agileframework.cache.util.CacheUtil;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.dictionary.DictionaryDataBase;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;

import java.util.Map;

/**
 * 借助agile-cache二级缓存组件实现字典缓存控制
 */
public class AgileCacheImpl implements DictionaryCache {

    @Autowired
    private SyncCache syncCache;

    @Override
    public void initData(String datasource, RegionEnum regionEnum, Map<String, DictionaryDataBase> cacheData) throws NotFoundCacheException {
        Map<RegionEnum, Map<String, DictionaryDataBase>> data = Maps.newHashMap();
        data.put(regionEnum, cacheData);
        CacheUtil.put(datasource, data);
    }

    @Override
    public Map<String, DictionaryDataBase> getDataByRegion(String datasource, RegionEnum regionEnum) throws NotFoundCacheException {
        AgileCache cache = CacheUtil.getCache(datasource);
        if (cache == null) {
            throw new NotFoundCacheException("Unable to get dictionary's cache");
        }
        Cache.ValueWrapper result = cache.get(regionEnum.name());
        if (result == null) {
            return Maps.newHashMap();
        }

        Map<String, DictionaryDataBase> data = cache.get(regionEnum.name(), new TypeReference<Map<String, DictionaryDataBase>>() {
        });
        if (data == null) {
            return Maps.newHashMap();
        }
        return data;
    }

    @Override
    public DictionaryDataBase getByFullIndex(String datasource, RegionEnum regionEnum, String fullIndex) throws NotFoundCacheException {
        AgileCache cache = CacheUtil.getCache(datasource);
        if (cache == null) {
            throw new NotFoundCacheException("Unable to get dictionary's cache");
        }
        return cache.getFromMap(regionEnum.name(), fullIndex, DictionaryDataBase.class);
    }

    @Override
    public void add(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException {
        AgileCache cache = CacheUtil.getCache(datasource);
        if (cache == null) {
            throw new NotFoundCacheException("Unable to get dictionary's cache");
        }
        cache.addToMap(RegionEnum.CODE_MEMORY.name(), dictionaryData.getFullCode(), dictionaryData);
        cache.addToMap(RegionEnum.NAME_MEMORY.name(), dictionaryData.getFullName(), dictionaryData);
    }

    @Override
    public void delete(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException {
        AgileCache cache = CacheUtil.getCache(datasource);
        if (cache == null) {
            throw new NotFoundCacheException("Unable to get dictionary's cache");
        }
        cache.removeFromMap(RegionEnum.CODE_MEMORY.name(), dictionaryData.getFullName());
        cache.removeFromMap(RegionEnum.NAME_MEMORY.name(), dictionaryData.getFullCode());
    }
}
