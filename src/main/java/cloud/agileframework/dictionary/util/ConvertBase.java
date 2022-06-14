package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.util.collection.TreeUtil;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryEngine;
import cloud.agileframework.dictionary.cache.DictionaryCacheUtil;
import cloud.agileframework.dictionary.cache.RegionEnum;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeSet;
import java.util.stream.Collectors;

import static cloud.agileframework.dictionary.DictionaryEngine.DEFAULT_SPLIT_CHAR;

/**
 * @author 佟盟
 * 日期 2021-03-24 19:37
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
class ConvertBase {

    static final Logger LOGGER = LoggerFactory.getLogger(DictionaryUtil.class);
    static final String DEFAULT_NAME = "$Dictionary.DEFAULT_NAME";

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
        return getDictionary(DictionaryEngine.DICTIONARY_DATA_CACHE, fullIndex, cacheType, errorMessage, splitChar);
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
        fullIndex = fullIndex.replace(splitChar, DEFAULT_SPLIT_CHAR);

        DictionaryDataBase entity = null;
        try {
            if (DictionaryEngine.CacheType.CODE_CACHE == cacheType) {
                entity = DictionaryCacheUtil.getDictionaryCache().getByFullIndex(datasource, RegionEnum.CODE_MEMORY, fullIndex);
            } else if (DictionaryEngine.CacheType.NAME_CACHE == cacheType) {
                entity = DictionaryCacheUtil.getDictionaryCache().getByFullIndex(datasource, RegionEnum.NAME_MEMORY, fullIndex);
            } else {
                entity = DictionaryCacheUtil.getDictionaryCache().getByFullIndex(datasource, RegionEnum.ID_MEMORY, fullIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 直接从缓存中查找对应字典码
        if (entity == null) {
            LOGGER.error(errorMessage, fullIndex);
            return null;
        }

        return entity;
    }

    /**
     * 刷新字典entity数据
     *
     * @param datasource 数据源
     * @param entity     字典
     * @return 刷新后的字典
     */
    public static <A extends DictionaryDataBase> A refresh(String datasource, A entity) {
        if (entity == null) {
            return null;
        }
        String fullIndex = entity.getFullCode();
        //创建子
        TreeSet<DictionaryDataBase> children = DictionaryCacheUtil.getDictionaryCache().likeByFullIndex(datasource, RegionEnum.CODE_MEMORY, fullIndex);

        //初始化全字典值与字典码默认值
        children.forEach(dic -> {
            dic.setFullCode(dic.getCode());
            dic.setFullName(dic.getName());
            dic.setFullId(dic.getId());
        });
        children.add(entity);
        TreeUtil.createTree(children,
                entity.getParentId(),
                DEFAULT_SPLIT_CHAR,
                "fullName", "fullCode", "fullId"
        );
        String id = entity.getId();

        entity.setChildren(children.stream()
                .filter(n -> id.equals(n.getParentId()))
                .collect(Collectors.toCollection(Sets::newTreeSet))
        );
        return entity;
    }
}
