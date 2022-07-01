package cloud.agileframework.dictionary.cache;

import cloud.agileframework.cache.support.AgileCache;
import cloud.agileframework.cache.util.CacheUtil;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.dictionary.DictionaryDataBase;
import com.google.common.collect.Maps;
import org.springframework.cache.Cache;

import java.util.Map;

/**
 * 借助agile-cache二级缓存组件实现字典缓存控制
 */
public class AgileCacheImpl implements DictionaryCache {
    public static final AgileCacheImpl INSTANT = new AgileCacheImpl();

    private AgileCache getAgileCache(String datasource) {
        return CacheUtil.getCache(datasource);
    }

    @Override
    public void initData(String datasource, RegionEnum regionEnum, Map<String, DictionaryDataBase> cacheData) {
        getAgileCache(datasource).put(regionEnum, cacheData);
    }

    @Override
    public Map<String, DictionaryDataBase> getDataByRegion(String datasource, RegionEnum regionEnum) throws NotFoundCacheException {
        AgileCache cache = getAgileCache(datasource);
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
        AgileCache cache = getAgileCache(datasource);
        if (cache == null) {
            throw new NotFoundCacheException("Unable to get dictionary's cache");
        }
        return cache.getFromMap(regionEnum.name(), fullIndex, DictionaryDataBase.class);
    }

    @Override
    public synchronized void add(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException {
        AgileCache cache = getAgileCache(datasource);
        if (cache == null) {
            throw new NotFoundCacheException("Unable to get dictionary's cache");
        }
        cache.addToMap(RegionEnum.CODE_MEMORY.name(), dictionaryData.getFullCode(), dictionaryData);
        cache.addToMap(RegionEnum.NAME_MEMORY.name(), dictionaryData.getFullName(), dictionaryData);
        cache.addToMap(RegionEnum.FULL_ID_MEMORY.name(), dictionaryData.getFullId(),dictionaryData);
        cache.addToMap(RegionEnum.ID_MEMORY.name(), dictionaryData.getId(),dictionaryData);
    }

    @Override
    public synchronized void delete(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException {
        AgileCache cache = getAgileCache(datasource);
        if (cache == null) {
            throw new NotFoundCacheException("Unable to get dictionary's cache");
        }
        cache.removeFromMap(RegionEnum.NAME_MEMORY.name(), dictionaryData.getFullName());
        cache.removeFromMap(RegionEnum.CODE_MEMORY.name(), dictionaryData.getFullCode());
        cache.removeFromMap(RegionEnum.FULL_ID_MEMORY.name(), dictionaryData.getFullId());
        cache.removeFromMap(RegionEnum.ID_MEMORY.name(), dictionaryData.getId());
    }
}
