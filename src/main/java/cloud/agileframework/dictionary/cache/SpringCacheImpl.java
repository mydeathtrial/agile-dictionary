package cloud.agileframework.dictionary.cache;

import cloud.agileframework.dictionary.DictionaryDataBase;
import com.google.common.collect.Maps;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.Map;

/**
 * Spring方式的缓存介质，通过向Spring容器注入CacheManager实现字典数据缓存控制
 */
public class SpringCacheImpl implements DictionaryCache {
    private final CacheManager cacheManager;

    public SpringCacheImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 初始化缓存数据
     * @param regionEnum 缓存区域
     * @param cacheData 经过计算后的字典缓存数据，这部分的key类型需要与regionEnum对应
     */
    public void initData(String datasource, RegionEnum regionEnum, Map<String, DictionaryDataBase> cacheData) throws NotFoundCacheException {
        Cache regionCache = getCache(datasource);
        regionCache.put(regionEnum.name(),cacheData);
    }

    /**
     * 根据缓存区域获取redis缓存
     * @param datasource 字典类型，对应不同的缓存区域
     * @return 缓存
     * @throws NotFoundCacheException 无法获取对应的缓存
     */
    private Cache getCache(String datasource) throws NotFoundCacheException {
        Cache regionCache = cacheManager.getCache(datasource);
        if(regionCache == null){
            throw new NotFoundCacheException("Unable to get Dictionary's cache object");
        }
        return regionCache;
    }

    /**
     * 根据缓存区域，获取经过计算的字典缓存数据
     * @param regionEnum 缓存区域
     * @return 经过计算的字典缓存数据
     */
    public Map<String, DictionaryDataBase> getDataByRegion(String datasource, RegionEnum regionEnum) throws NotFoundCacheException {
        Cache regionCache = getCache(datasource);
        Cache.ValueWrapper result = regionCache.get(regionEnum.name());
        if(result == null){
            return Maps.newHashMap();
        }
        return (Map<String, DictionaryDataBase>)result.get();
    }

    @Override
    public void add(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException {
        Map<String, DictionaryDataBase> codeData = getDataByRegion(datasource, RegionEnum.CODE_MEMORY);
        if(codeData == null){
            codeData = Maps.newHashMap();
        }
        codeData.put(dictionaryData.getFullCode(), dictionaryData);
        initData(datasource,RegionEnum.CODE_MEMORY,codeData);

        Map<String, DictionaryDataBase> nameData = getDataByRegion(datasource, RegionEnum.NAME_MEMORY);
        if(nameData == null){
            nameData = Maps.newHashMap();
        }
        nameData.put(dictionaryData.getFullName(), dictionaryData);
        initData(datasource,RegionEnum.NAME_MEMORY,nameData);
    }

    @Override
    public void delete(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException {
        Map<String, DictionaryDataBase> codeMap = getDataByRegion(datasource, RegionEnum.CODE_MEMORY);
        codeMap.remove(dictionaryData.getFullCode());
        initData(datasource,RegionEnum.CODE_MEMORY,codeMap);

        Map<String, DictionaryDataBase> nameMap = getDataByRegion(datasource, RegionEnum.NAME_MEMORY);
        nameMap.remove(dictionaryData.getFullName());
        initData(datasource,RegionEnum.NAME_MEMORY,nameMap);
    }
}
