package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryEngine;
import cloud.agileframework.dictionary.cache.DictionaryCacheUtil;
import cloud.agileframework.dictionary.cache.RegionEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 佟盟
 * 日期 2021-03-24 19:37
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ConvertBase {
    public ConvertBase() {
    }

    /**
     * 取字典
     *
     * @param fullIndex    全字典值或全字典码
     * @param cacheType    缓存类型，值缓存或码缓存
     * @param errorMessage 错误消息
     * @param splitChar    分隔符
     * @return 字典对象
     */
    static DictionaryDataBase getDictionary(String fullIndex,
                                            DictionaryEngine.CacheType cacheType,
                                            String errorMessage,
                                            String splitChar) {
        return getDictionary(Constant.AgileAbout.DIC_DATASOURCE, fullIndex, cacheType, errorMessage, splitChar);
    }

    /**
     * 根据全路径字典值或字典码，查找缓存中的字典数据
     *
     * @param datasource   数据源
     * @param fullIndex    全路径字典值或字典码
     * @param cacheType    缓存类型
     * @param errorMessage 日志
     * @param splitChar    分隔符
     * @return 字典数据
     */
    static DictionaryDataBase getDictionary(String datasource,
                                            String fullIndex,
                                            DictionaryEngine.CacheType cacheType,
                                            String errorMessage,
                                            String splitChar) {
        if (StringUtils.isEmpty(fullIndex)) {
            return null;
        }
        // 字典分隔符同义转换为点号
        fullIndex = fullIndex.replace(splitChar, Constant.AgileAbout.DIC_SPLIT);

        DictionaryDataBase entity = null;
        try {
            if (DictionaryEngine.CacheType.CODE_CACHE == cacheType) {
                entity = DictionaryCacheUtil.getDictionaryCache(datasource).getByFullIndex(datasource, RegionEnum.CODE_MEMORY, fullIndex);
            } else if (DictionaryEngine.CacheType.NAME_CACHE == cacheType) {
                entity = DictionaryCacheUtil.getDictionaryCache(datasource).getByFullIndex(datasource, RegionEnum.NAME_MEMORY, fullIndex);
            } else {
                entity = DictionaryCacheUtil.getDictionaryCache(datasource).getByFullIndex(datasource, RegionEnum.ID_MEMORY, fullIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 直接从缓存中查找对应字典码
        if (entity == null) {
           throw new TranslateException(String.format(errorMessage,fullIndex));
        }

        return entity;
    }
}
