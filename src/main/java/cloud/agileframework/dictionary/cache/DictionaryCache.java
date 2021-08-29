package cloud.agileframework.dictionary.cache;


import cloud.agileframework.dictionary.DictionaryDataBase;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.SerializationUtils;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public interface DictionaryCache {

    /**
     * 初始化缓存数据
     *
     * @param datasource 字典类型
     * @param treeSet    经过计算后的树形结构字典缓存数据，这部分的key类型需要与regionEnum对应
     */
    default void initData(String datasource, SortedSet<DictionaryDataBase> treeSet) throws NotFoundCacheException {
        //做缓存同步
        Map<String, DictionaryDataBase> codeMap = Maps.newHashMap();
        Map<String, DictionaryDataBase> nameMap = Maps.newHashMap();

        treeSet.forEach(dic -> {
            codeMap.put(dic.getFullCode(), dic);
            nameMap.put(dic.getFullName(), dic);
        });

        initData(datasource, RegionEnum.CODE_MEMORY, codeMap);
        initData(datasource, RegionEnum.NAME_MEMORY, nameMap);
    }


    /**
     *
     * @param datasource 字典分类
     * @param regionEnum 缓存区域
     * @param cacheData 缓存的树形结构字典数据
     * @throws NotFoundCacheException 没找到缓存介质
     */
    void initData(String datasource, RegionEnum regionEnum, Map<String, DictionaryDataBase> cacheData) throws NotFoundCacheException;

    /**
     * 根据缓存区域，获取缓存的树形结构字典数据
     *
     * @param datasource 字典类型
     * @param regionEnum 缓存区域
     * @return 缓存的树形结构字典数据
     */
    Map<String, DictionaryDataBase> getDataByRegion(String datasource, RegionEnum regionEnum) throws NotFoundCacheException;

    /**
     * 根据缓存区域，获取缓存的树形结构字典数据，注意此处为clone数据，不是原始数据
     *
     * @param datasource 字典类型
     * @return 缓存的树形结构字典数据
     */
    default SortedSet<DictionaryDataBase> getDataByDatasource(String datasource) throws NotFoundCacheException{
        return SerializationUtils.clone(new TreeSet<>(getDataByRegion(datasource, RegionEnum.CODE_MEMORY).values()));
    }

    /**
     * 根据fullCode或者fullName提取字典
     *
     * @param datasource 字典类型
     * @param regionEnum 字典区域类型
     * @param fullIndex  fullCode或者fullName
     * @return 字典
     * @throws NotFoundCacheException 未找到缓存介质
     */
    default DictionaryDataBase getByFullIndex(String datasource, RegionEnum regionEnum, String fullIndex) throws NotFoundCacheException {
        Map<String, DictionaryDataBase> data = getDataByRegion(datasource, regionEnum);
        if (data == null) {
            return null;
        }
        return data.get(fullIndex);
    }

    /**
     * 新增字典
     *
     * @param datasource     字典分类
     * @param dictionaryData 要新增的字典数据
     * @throws NotFoundCacheException 没找到对应的缓存介质
     */
    void add(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException;

    /**
     * 删除缓存
     * @param datasource 字典分类
     * @param dictionaryData 要删除的字典数据
     * @throws NotFoundCacheException
     */
    void delete(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException;
}
