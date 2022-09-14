package cloud.agileframework.dictionary.cache;


import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.collection.TreeUtil;
import cloud.agileframework.dictionary.DictionaryDataBase;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

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
        Map<String, DictionaryDataBase> fullIdMap = Maps.newHashMap();

        treeSet.forEach(dic -> {
            codeMap.put(dic.getFullCode(), dic);
            nameMap.put(dic.getFullName(), dic);
            idMap.put(dic.getId(), dic);
            fullIdMap.put(dic.getFullId(), dic);
        });

        initData(datasource, RegionEnum.CODE_MEMORY, codeMap);
        initData(datasource, RegionEnum.NAME_MEMORY, nameMap);
        initData(datasource, RegionEnum.ID_MEMORY, idMap);
        initData(datasource, RegionEnum.FULL_ID_MEMORY, fullIdMap);
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

        Map<String, DictionaryDataBase> dataByRegion = getDataByRegion(datasource, RegionEnum.ID_MEMORY);
        dataByRegion.entrySet().stream().filter(a->a.getValue()==null).forEach(a-> System.out.println(a.getKey()));
        return new ConcurrentSkipListSet<>(dataByRegion.values());
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
    default ConcurrentSkipListSet<DictionaryDataBase> likeByFullIndex(String datasource, RegionEnum regionEnum, String fullIndex) {
        Map<String, DictionaryDataBase> data = null;
        try {
            data = getDataByRegion(datasource, regionEnum);
        } catch (NotFoundCacheException e) {
            e.printStackTrace();
        }
        if (data == null) {
            return new ConcurrentSkipListSet<>();
        }

        return data.entrySet()
                .stream()
                .filter(node -> node.getKey().startsWith(fullIndex + Constant.AgileAbout.DIC_SPLIT))
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(ConcurrentSkipListSet::new));
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
            newFullCode = parent.getFullCode() + Constant.AgileAbout.DIC_SPLIT + entity.getCode();
            newFullName = parent.getFullName() + Constant.AgileAbout.DIC_SPLIT + entity.getName();
            newFullId = parent.getFullId() + Constant.AgileAbout.DIC_SPLIT + entity.getId();
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
        String fullIndex = entity.getFullId();
        //创建子
        ConcurrentSkipListSet<DictionaryDataBase> children = likeByFullIndex(datasource, RegionEnum.FULL_ID_MEMORY, fullIndex);

        //初始化全字典值与字典码默认值
        children.forEach(dic -> {
            dic.setFullCode(dic.getCode());
            dic.setFullName(dic.getName());
            dic.setFullId(dic.getId());
        });
        children.add(entity);
        TreeUtil.createTree(children,
                entity.getParentId(),
                Constant.AgileAbout.DIC_SPLIT,
                "fullName", "fullCode", "fullId"
        );
        String id = entity.getId();

        entity.setChildren(children.stream()
                .filter(n -> id.equals(n.getParentId()))
                .collect(Collectors.toCollection(ConcurrentSkipListSet::new))
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
        return (D) getDataByRegion(datasource, RegionEnum.ID_MEMORY)
                .get(id);
    }

    default void refresh(String datasource) {
//        SortedSet<DictionaryDataBase> treeSet;
//        try {
//            treeSet = getDataByDatasource(datasource);
//        } catch (NotFoundCacheException e) {
//            throw new RuntimeException(e);
//        }
//
//        //初始化全字典值与字典码默认值
//        treeSet.forEach(dic -> {
//            dic.setFullCode(dic.getCode());
//            dic.setFullName(dic.getName());
//            dic.setFullId(dic.getId());
//        });
//
//        //构建树形结构，过程中重新计算全字典值与全字典码
//        TreeUtil.createTree(treeSet,
//                DictionaryEngine.getDictionaryDataManagerMap(datasource).rootParentId(),
//                DEFAULT_SPLIT_CHAR,
//                "fullName", "fullCode", "fullId"
//        );
//        try {
//            initData(datasource,treeSet);
//        } catch (NotFoundCacheException e) {
//            throw new RuntimeException(e);
//        }
    }
}
