package cloud.agileframework.dictionary;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.dictionary.cache.NotFoundCacheException;
import cloud.agileframework.dictionary.cache.RegionEnum;
import cloud.agileframework.dictionary.util.ConvertDicBean;
import cloud.agileframework.dictionary.util.DictionaryUtil;
import cloud.agileframework.dictionary.util.TranslateException;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
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

    public SyncProxy sync() {
        return new SyncProxy();
    }

    D findOne(String id) {
        try {
            return (D) cache().getByFullIndex(dataSource(), RegionEnum.ID_MEMORY, id);
        } catch (NotFoundCacheException e) {
            e.printStackTrace();
        }
        return null;
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
                    .collect(Collectors.toCollection(ConcurrentSkipListSet::new));
        }

        /**
         * 新增并广播
         *
         * @param dictionaryData 字典
         */
        public void add(D dictionaryData) {
            synchronized (this) {
                if (dictionaryData.getId() != null && Objects.equals(dictionaryData.getId(), dictionaryData.getParentId())) {
                    throw new IllegalArgumentException("父主键与主键不能相同");
                }
                addData(dictionaryData);
                cache().refresh(dataSource());
            }
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
                dictionaryData.setFullCode(parent.getFullCode() + Constant.AgileAbout.DIC_SPLIT + dictionaryData.getCode());
                dictionaryData.setFullName(parent.getFullName() + Constant.AgileAbout.DIC_SPLIT + dictionaryData.getName());
                dictionaryData.setFullId(parent.getFullId() + Constant.AgileAbout.DIC_SPLIT + dictionaryData.getId());
            } else {
                dictionaryData.setFullCode(dictionaryData.getCode());
                dictionaryData.setFullName(dictionaryData.getName());
                dictionaryData.setFullId(dictionaryData.getId());
            }

            try {
                cache().addAndRefresh(dataSource(), dictionaryData);
            } catch (NotFoundCacheException e) {
                e.printStackTrace();
            }
        }

        /**
         * 根据全路径字典码删除字典，默认分隔符
         *
         * @param fullCode 全路径字典码
         */
        public void delete(String fullCode) {
            D dictionaryData = (D) ConvertDicBean.coverDicBean(dataSource(), fullCode, Constant.AgileAbout.DIC_SPLIT);
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
            delete(fullCode.replace(split, Constant.AgileAbout.DIC_SPLIT));
        }

        /**
         * 删除字典，并广播
         *
         * @param dictionaryData 字典
         */
        public void delete(D dictionaryData) {
            synchronized (this) {
                deleteData(dictionaryData);
                cache().refresh(dataSource());
            }
        }

        /**
         * 删除字典，不广播
         *
         * @param dictionaryData 字典
         */
        private void deleteData(D dictionaryData) {
            if (dictionaryData == null) {
                return;
            }
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
            try {
                cache().deleteAndRefresh(dataSource(), oldData);
            } catch (NotFoundCacheException e) {
                e.printStackTrace();
            }
        }

        /**
         * 更新字典，并广播
         *
         * @param dictionaryData 字典
         */
        public void update(D dictionaryData) {
            synchronized (this) {
                if (Objects.equals(dictionaryData.getId(), dictionaryData.getParentId())) {
                    throw new IllegalArgumentException("父主键与主键不能相同");
                }
                updateData(dictionaryData, AbstractDictionaryDataManager.this::update, true, false);
                cache().refresh(dataSource());
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
                cache().refresh(dataSource());
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
            String[] requireField = {"code", "name", "sort", "parentId", "fullCode", "fullName", "fullId"};

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
            String[] exclude = {"code", "name", "sort", "parentId", "fullCode", "fullName", "fullId", "children"};

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
            D parent = findOne(oldData.getParentId());
            try {
                cache().refreshLeaf(dataSource(), oldData, parent);
                cache().refreshToRoot(dataSource(), parent);
            } catch (NotFoundCacheException e) {
                e.printStackTrace();
            }
        }


        void deleteMemory(D oldData) {
            oldData.getChildren().forEach(a -> deleteMemory((D) a));

            //先清除旧的数据
            try {
                cache().deleteAndRefresh(dataSource(), oldData);
            } catch (NotFoundCacheException e) {
                e.printStackTrace();
            }
        }

    }

}
