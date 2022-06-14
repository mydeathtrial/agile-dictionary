package cloud.agileframework.dictionary.cache;


import cloud.agileframework.common.util.collection.TreeUtil;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryEngine;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static cloud.agileframework.dictionary.DictionaryEngine.DEFAULT_SPLIT_CHAR;

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
        Map<String, DictionaryDataBase> idMap = Maps.newHashMap();

        treeSet.forEach(dic -> {
            codeMap.put(dic.getFullCode(), dic);
            nameMap.put(dic.getFullName(), dic);
            idMap.put(dic.getId(), dic);
        });

        initData(datasource, RegionEnum.CODE_MEMORY, codeMap);
        initData(datasource, RegionEnum.NAME_MEMORY, nameMap);
        initData(datasource, RegionEnum.ID_MEMORY, idMap);
    }


    /**
     * @param datasource 字典分类
     * @param regionEnum 缓存区域
     * @param cacheData  缓存的树形结构字典数据
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
    default SortedSet<DictionaryDataBase> getDataByDatasource(String datasource) throws NotFoundCacheException {
        return new TreeSet<>(getDataByRegion(datasource, RegionEnum.CODE_MEMORY).values());
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
     * 根据fullCode或者fullName提取字典
     *
     * @param datasource 字典类型
     * @param regionEnum 字典区域类型
     * @param fullIndex  fullCode或者fullName
     * @return 字典
     */
    default TreeSet<DictionaryDataBase> likeByFullIndex(String datasource, RegionEnum regionEnum, String fullIndex) {
        Map<String, DictionaryDataBase> data = null;
        try {
            data = getDataByRegion(datasource, regionEnum);
        } catch (NotFoundCacheException e) {
            e.printStackTrace();
        }
        if (data == null) {
            return Sets.newTreeSet();
        }

        return data.entrySet()
                .stream()
                .filter(node -> node.getKey().startsWith(fullIndex + DictionaryEngine.DEFAULT_SPLIT_CHAR))
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(Sets::newTreeSet));
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
     *
     * @param datasource     字典分类
     * @param dictionaryData 要删除的字典数据
     * @throws NotFoundCacheException
     */
    void delete(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException;

    default void addAndRefresh(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException {
        add(datasource, dictionaryData);
        String parentId = dictionaryData.getParentId();
        if (parentId != null) {
            DictionaryDataBase parent = findById(datasource, parentId);
            refreshToRoot(datasource, parent);
        }
    }

    default void deleteAndRefresh(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException {
        delete(datasource, dictionaryData);
        String parentId = dictionaryData.getParentId();
        if (parentId != null) {
            DictionaryDataBase parent = findById(datasource, parentId);
            refreshToRoot(datasource, parent);
        }
    }

    default <A extends DictionaryDataBase> void refreshLeaf(String datasource, A entity, A parent) throws NotFoundCacheException {
        if (entity == null) {
            return;
        }
        String newFullCode;
        String newFullName;
        String newFullId;

        if (parent != null) {
            newFullCode = parent.getFullCode() + DEFAULT_SPLIT_CHAR + entity.getCode();
            newFullName = parent.getFullName() + DEFAULT_SPLIT_CHAR + entity.getName();
            newFullId = parent.getFullId() + DEFAULT_SPLIT_CHAR + entity.getId();
        } else {
            newFullCode = entity.getCode();
            newFullName = entity.getName();
            newFullId = entity.getId();
        }

        entity.setFullCode(newFullCode);
        entity.setFullName(newFullName);
        entity.setFullId(newFullId);

        add(datasource, entity);
        for (DictionaryDataBase child : entity.getChildren()) {
            refreshLeaf(datasource, (A) child, entity);
        }
    }

    /**
     * 刷新从entity节点到父级的所有节点
     *
     * @param datasource 数据源
     * @param entity     节点
     * @param <A>        泛型
     * @throws NotFoundCacheException 没有找到缓存
     */
    default <A extends DictionaryDataBase> void refreshToRoot(String datasource, A entity) throws NotFoundCacheException {
        if (entity == null) {
            return;
        }
        String fullIndex = entity.getFullCode();
        //创建子
        TreeSet<DictionaryDataBase> children = likeByFullIndex(datasource, RegionEnum.CODE_MEMORY, fullIndex);

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
        //刷新缓存
        add(datasource, entity);

        //刷新父节点
        String parentId = entity.getParentId();
        if (parentId != null) {
            DictionaryDataBase parent = findById(datasource, parentId);
            refreshToRoot(datasource, parent);
        }
    }

    default <D extends DictionaryDataBase> D findById(String datasource, String id) throws NotFoundCacheException {
        return (D) DictionaryCacheUtil.getDictionaryCache()
                .getDataByRegion(datasource, RegionEnum.ID_MEMORY)
                .get(id);
    }
}
