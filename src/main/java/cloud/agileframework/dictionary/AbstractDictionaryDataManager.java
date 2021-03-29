package cloud.agileframework.dictionary;

import cloud.agileframework.cache.sync.OpType;
import cloud.agileframework.cache.sync.SyncCache;
import cloud.agileframework.cache.util.CacheUtil;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.dictionary.util.DictionaryUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2021-03-29 14:04
 * 描述 抽象字典管理器
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractDictionaryDataManager implements DictionaryDataManager {
    @Autowired
    private SyncCache syncCache;

    public SyncProxy sync(){
        return new SyncProxy();
    }

    public class SyncProxy{
        /**
         * 取所有字典，查询所有字典
         *
         * @return 所有字典，并携带子
         */
        public List<DictionaryDataBase> all() {
            List<DictionaryDataBase> list = CacheUtil.getCache(AbstractDictionaryDataManager.this.dataSource())
                    .get(DictionaryEngine.ALL_MEMORY, new TypeReference<List<DictionaryDataBase>>() {
                    });
            if(list == null || list.isEmpty()){
                list = AbstractDictionaryDataManager.this.all();
            }
            return list;
        }

        /**
         * 查询字典树，仅保留跟节点
         *
         * @return 根节点字典树
         */
        public List<DictionaryDataBase> tree() {
            return all()
                    .stream()
                    .filter(dic -> String.valueOf(dic.getParentId())
                            .equals(DictionaryEngine.ROOT_VALUE))
                    .collect(Collectors.toList());
        }

        /**
         * 新增并广播
         *
         * @param dictionaryData 字典
         */
        public void add(DictionaryDataBase dictionaryData) {
            synchronized(this){
                addData(dictionaryData);
                sync();
            }
        }

        private void sync() {
            syncCache.sync(AbstractDictionaryDataManager.this.dataSource(), DictionaryEngine.ALL_MEMORY, ()-> null, OpType.WRITE);
            syncCache.sync(AbstractDictionaryDataManager.this.dataSource(), DictionaryEngine.CODE_MEMORY, ()-> null, OpType.WRITE);
            syncCache.sync(AbstractDictionaryDataManager.this.dataSource(), DictionaryEngine.NAME_MEMORY, ()-> null, OpType.WRITE);
        }

        /**
         * 新增，不广播
         *
         * @param dictionaryData 字典
         */
        private void addData(DictionaryDataBase dictionaryData) {
            //判断是否为更新
            DictionaryDataBase self = all().stream()
                    .filter(n -> n.getId().equals(dictionaryData.getId()))
                    .findFirst().orElse(null);

            if (self != null) {
                updateCache(dictionaryData);
                return;
            }
            AbstractDictionaryDataManager.this.add(dictionaryData);
            addCache(dictionaryData);

            //新增子
            Optional.ofNullable(dictionaryData.getChildren()).ifPresent(children -> children.forEach(this::addData));
        }

        /**
         * 操作字典内存，新增
         *
         * @param dictionaryData 字典
         */
        private void addCache(DictionaryDataBase dictionaryData) {
            //更新父
            DictionaryDataBase parent = all().stream()
                    .filter(n -> n.getId().equals(dictionaryData.getParentId()))
                    .findFirst().orElse(null);

            if (parent != null) {
                dictionaryData.setFullCode(parent.getFullCode() + DictionaryEngine.DEFAULT_SPLIT_CHAR + dictionaryData.getCode());
                dictionaryData.setFullName(parent.getFullName() + DictionaryEngine.DEFAULT_SPLIT_CHAR + dictionaryData.getName());

                parent.getChildren().add(dictionaryData);
            } else {
                dictionaryData.setFullCode(dictionaryData.getCode());
                dictionaryData.setFullName(dictionaryData.getName());
            }

            all().add(dictionaryData);
        }

        /**
         * 根据全路径字典码删除字典，默认分隔符
         *
         * @param fullCode 全路径字典码
         */
        public void delete(String fullCode) {
            DictionaryDataBase dictionaryData = DictionaryUtil.coverDicBean(fullCode);
            if (dictionaryData == null) {
                throw new NoSuchElementException(String.format("Did not find dictionary [%s]", fullCode));
            }
            delete(dictionaryData);
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
        public void delete(DictionaryDataBase dictionaryData) {
            synchronized(this){
                deleteData(dictionaryData);
                sync();
            }
        }

        /**
         * 删除字典，不广播
         *
         * @param dictionaryData 字典
         */
        private void deleteData(DictionaryDataBase dictionaryData) {
            //先遍历删除子
            Optional.ofNullable(dictionaryData.getChildren()).ifPresent(children -> children.forEach(this::deleteData));

            AbstractDictionaryDataManager.this.delete(dictionaryData);
            deleteCache(dictionaryData);
        }

        /**
         * 操作内容删除字典
         *
         * @param dictionaryData 字典
         */
        private void deleteCache(DictionaryDataBase dictionaryData) {
            all().remove(dictionaryData);

            //遍历更新父节点
            all().stream()
                    .filter(n -> n.getId().equals(dictionaryData.getParentId()))
                    .findFirst().ifPresent(parent -> parent.getChildren().removeIf(dic -> dic.getId().equals(dictionaryData.getId())));
        }

        /**
         * 更新字典，并广播
         *
         * @param dictionaryData 字典
         */
        public void update(DictionaryDataBase dictionaryData) {
            synchronized(this){
                updateData(dictionaryData);
                sync();
            }
        }

        /**
         * 更新字典，不广播
         *
         * @param dictionaryData 字典
         */
        private void updateData(DictionaryDataBase dictionaryData) {
            //判断是否为新增
            DictionaryDataBase self = all().stream()
                    .filter(n -> n.getId().equals(dictionaryData.getId()))
                    .findFirst().orElse(null);

            if (self == null) {
                addData(dictionaryData);
                return;
            }

            AbstractDictionaryDataManager.this.update(dictionaryData);
            updateCache(dictionaryData);

            //更新增子
            Optional.ofNullable(dictionaryData.getChildren()).ifPresent(children -> children.forEach(this::updateData));
        }

        /**
         * 操作内存，更新字典
         *
         * @param dictionaryData 字典
         */
        private void updateCache(DictionaryDataBase dictionaryData) {
            //更新自己
            DictionaryDataBase oldSelf = all().stream()
                    .filter(n -> n.getId().equals(dictionaryData.getId()))
                    .findFirst().orElse(null);
            if (oldSelf == null) {
                return;
            }
            oldSelf.setCode(dictionaryData.getCode());
            oldSelf.setName(dictionaryData.getName());
            oldSelf.setChildren(dictionaryData.getChildren());
            DictionaryDataBase parent = all().stream()
                    .filter(n -> n.getId().equals(dictionaryData.getParentId()))
                    .findFirst().orElse(null);
            if (parent != null) {
                oldSelf.setFullCode(parent.getFullCode() + DictionaryEngine.DEFAULT_SPLIT_CHAR + dictionaryData.getCode());
                oldSelf.setFullName(parent.getFullName() + DictionaryEngine.DEFAULT_SPLIT_CHAR + dictionaryData.getName());
            } else {
                oldSelf.setFullCode(dictionaryData.getCode());
                oldSelf.setFullName(dictionaryData.getName());
            }
        }
    }
}
