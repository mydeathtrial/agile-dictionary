package cloud.agileframework.dictionary;

import cloud.agileframework.cache.sync.OpType;
import cloud.agileframework.cache.sync.SyncCache;
import cloud.agileframework.cache.util.CacheUtil;
import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.dictionary.util.DictionaryUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2021-03-29 14:04
 * 描述 抽象字典管理器
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractDictionaryDataManager<D extends DictionaryDataBase> implements DictionaryDataManager<D> {
    @Autowired
    private SyncCache syncCache;

    public SyncProxy sync() {
        return new SyncProxy();
    }

    public class SyncProxy {
        /**
         * 取所有字典，查询所有字典
         *
         * @return 所有字典，并携带子
         */
        public SortedSet<D> all() {
            SortedSet<DictionaryDataBase> treeSet = DictionaryUtil.findAll(dataSource());
            return (SortedSet<D>) treeSet;
        }

        /**
         * 查询字典树，仅保留跟节点
         *
         * @return 根节点字典树
         */
        public SortedSet<D> tree() {
            final String root = AbstractDictionaryDataManager.this.rootParentId();
            return all()
                    .stream()
                    .filter(dic -> Objects.equals(dic.getParentId(), root))
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        /**
         * 新增并广播
         *
         * @param dictionaryData 字典
         */
        public void add(D dictionaryData) {
            synchronized (this) {
                addData(dictionaryData);
                sync();
            }
        }

        private void sync() {
            syncCache.sync(dataSource(), DictionaryEngine.CODE_MEMORY, () -> null, OpType.WRITE);
            syncCache.sync(dataSource(), DictionaryEngine.NAME_MEMORY, () -> null, OpType.WRITE);
        }

        /**
         * 新增，不广播
         *
         * @param dictionaryData 字典
         */
        private void addData(D dictionaryData) {
            //判断是否为更新
            D self = findOne(dictionaryData.getId());

            if (self != null) {
                updateData(dictionaryData, AbstractDictionaryDataManager.this::update, false, false);
                return;
            }

            AbstractDictionaryDataManager.this.add(dictionaryData);
            addCache(dictionaryData);

            //新增子
            Optional.ofNullable(dictionaryData.getChildren()).ifPresent(children -> children.stream()
                    .map(a -> (D) a)
                    .forEach(this::addData));
        }

        /**
         * 操作字典内存，新增
         *
         * @param dictionaryData 字典
         */
        private void addCache(D dictionaryData) {
            //更新父
            D parent = findOne(dictionaryData.getParentId());

            if (parent != null) {
                dictionaryData.setFullCode(parent.getFullCode() + DictionaryEngine.DEFAULT_SPLIT_CHAR + dictionaryData.getCode());
                dictionaryData.setFullName(parent.getFullName() + DictionaryEngine.DEFAULT_SPLIT_CHAR + dictionaryData.getName());

                parent.getChildren().add(dictionaryData);
            } else {
                dictionaryData.setFullCode(dictionaryData.getCode());
                dictionaryData.setFullName(dictionaryData.getName());
            }

            CacheUtil.getCache(dataSource())
                    .addToMap(DictionaryEngine.CODE_MEMORY, dictionaryData.getFullCode(), dictionaryData);
            CacheUtil.getCache(dataSource())
                    .addToMap(DictionaryEngine.NAME_MEMORY, dictionaryData.getFullName(), dictionaryData);
        }

        /**
         * 根据全路径字典码删除字典，默认分隔符
         *
         * @param fullCode 全路径字典码
         */
        public void delete(String fullCode) {
            D dictionaryData = (D) DictionaryUtil.coverDicBean(dataSource(), fullCode, DictionaryEngine.DEFAULT_SPLIT_CHAR);
            if (dictionaryData == null) {
                throw new NoSuchElementException(String.format("Did not find dictionary [%s]", fullCode));
            }
            delete(dictionaryData);
        }

        public void deleteById(String id) {
            DictionaryDataBase dic = DictionaryUtil.findById(dataSource(), id);
            delete((D) dic);
        }

        /**
         * 根据全路径字典码删除字典，自定义分隔符
         *
         * @param fullCode 全路径字典码
         */
        public void delete(String fullCode, String split) {
            delete(fullCode.replace(split, DictionaryEngine.DEFAULT_SPLIT_CHAR));
        }

        /**
         * 删除字典，并广播
         *
         * @param dictionaryData 字典
         */
        public void delete(D dictionaryData) {
            synchronized (this) {
                deleteData(dictionaryData);
                sync();
            }
        }

        /**
         * 删除字典，不广播
         *
         * @param dictionaryData 字典
         */
        private void deleteData(D dictionaryData) {
            //先遍历删除子
            Optional.ofNullable(dictionaryData.getChildren()).ifPresent(children -> children.stream()
                    .map(a -> (D) a)
                    .forEach(this::deleteData));

            AbstractDictionaryDataManager.this.delete(dictionaryData);
            deleteCache(dictionaryData);
        }

        /**
         * 操作内容删除字典
         *
         * @param dictionaryData 字典
         */
        private void deleteCache(D dictionaryData) {
            DictionaryDataBase oldData = findOne(dictionaryData.getId());
            if (oldData == null) {
                return;
            }
            CacheUtil.getCache(dataSource())
                    .removeFromMap(DictionaryEngine.CODE_MEMORY, oldData.getFullCode());
            CacheUtil.getCache(dataSource())
                    .removeFromMap(DictionaryEngine.NAME_MEMORY, oldData.getFullName());

            //遍历更新父节点
            DictionaryDataBase parent = findOne(oldData.getParentId());
            if (parent != null) {
                parent.getChildren()
                        .removeIf(dic -> dic.getId().equals(oldData.getId()));
            }
        }

        /**
         * 更新字典，并广播
         *
         * @param dictionaryData 字典
         */
        public void update(D dictionaryData) {
            synchronized (this) {
                updateData(dictionaryData, AbstractDictionaryDataManager.this::update, true, false);
                sync();
            }
        }

        /**
         * 更新字典，并广播
         *
         * @param dictionaryData 字典
         */
        public void updateOfNotNull(D dictionaryData) {
            synchronized (this) {
                updateData(dictionaryData, AbstractDictionaryDataManager.this::updateOfNotNull, true, true);
                sync();
            }
        }

        /**
         * 更新字典，不广播
         *
         * @param newData 字典
         */
        private void updateData(D newData, Function<D, D> function, boolean ignoreChildren, boolean ignoreNullField) {
            D oldData = findOne(newData.getId());

            //如果不存在就新增
            if (oldData == null) {
                addData(newData);
                return;
            }

            //调用持久层更新
            newData = function.apply(newData);
            if (newData == null) {
                throw new RuntimeException("sorry!you must return an object");
            }

            //缓存更新
            updateCache(newData, ignoreNullField);
        }

        private void replaceProperties(D newData, DictionaryDataBase oldData, boolean ignoreNullField) {
            //更新必要字段
            String[] requireField = {"code", "name", "sort", "parentId", "fullCode", "fullName"};

            ObjectUtil.copyProperties(newData,
                    oldData,
                    Constant.RegularAbout.BLANK,
                    Constant.RegularAbout.BLANK,
                    requireField,
                    ObjectUtil.ContainOrExclude.INCLUDE,
                    ObjectUtil.Compare.DIFF_SOURCE_NOT_NULL,
                    false);

            //更新必要字段
            final String parentId = newData.getParentId();
            if (parentId == null && !ignoreNullField) {
                oldData.setParentId(null);
            }

            ObjectUtil.Compare compare;
            if (ignoreNullField) {
                compare = ObjectUtil.Compare.DIFF_SOURCE_NOT_NULL;
            } else {
                compare = ObjectUtil.Compare.DIFF;
            }
            String[] exclude = {"code", "name", "sort", "parentId", "fullCode", "fullName", "children"};

            //更新非必要字段
            ObjectUtil.copyProperties(newData,
                    oldData,
                    Constant.RegularAbout.BLANK,
                    Constant.RegularAbout.BLANK,
                    exclude,
                    ObjectUtil.ContainOrExclude.EXCLUDE,
                    compare,
                    false);
        }

        /**
         * 刷新节点的全字典值、字典码，包括子节点
         *
         * @param newData 内存中的字典节点
         */
        private void updateCache(D newData, boolean ignoreNullField) {
            D oldData = findOne(newData.getId());

            //先把旧的都删掉
            deleteMemory(oldData);

            //新数据根据要求向旧数据覆盖树形
            replaceProperties(newData, oldData, ignoreNullField);

            //更新自己
            D parent = findOne(newData.getParentId());

            //再把新的加回来
            insertMemory(oldData,parent);
        }


        void deleteMemory(D oldData) {
            oldData.getChildren().forEach(a -> deleteMemory((D) a));

            //先清除旧的数据
            CacheUtil.getCache(dataSource())
                    .removeFromMap(DictionaryEngine.CODE_MEMORY, oldData.getFullCode());

            //先清除旧的数据
            CacheUtil.getCache(dataSource())
                    .removeFromMap(DictionaryEngine.NAME_MEMORY, oldData.getFullName());
        }

        void insertMemory(D newData, D parent) {
            String newFullCode;
            String newFullName;

            if (parent != null) {
                newFullCode = parent.getFullCode() + DictionaryEngine.DEFAULT_SPLIT_CHAR + newData.getCode();
                newFullName = parent.getFullName() + DictionaryEngine.DEFAULT_SPLIT_CHAR + newData.getName();
            } else {
                newFullCode = newData.getCode();
                newFullName = newData.getName();
            }

            newData.setFullCode(newFullCode);
            newData.setFullName(newFullName);

            //先清除旧的数据
            CacheUtil.getCache(dataSource())
                    .addToMap(DictionaryEngine.CODE_MEMORY, newFullCode, newData);

            //先清除旧的数据
            CacheUtil.getCache(dataSource())
                    .addToMap(DictionaryEngine.NAME_MEMORY, newFullName, newData);

            newData.getChildren().forEach(a -> insertMemory((D) a, newData));
        }

    }

    D findOne(String id) {
        final Map<String, DictionaryDataBase> cacheData = CacheUtil.getCache(dataSource())
                .get(DictionaryEngine.CODE_MEMORY, new TypeReference<Map<String, DictionaryDataBase>>() {
                });
        if(cacheData == null){
            return null;
        }
        return cacheData.values().parallelStream().filter(data -> data.getId().equals(id)).map(a -> (D) a).findFirst().orElse(null);
    }

}
