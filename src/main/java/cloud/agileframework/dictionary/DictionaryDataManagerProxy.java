package cloud.agileframework.dictionary;

import cloud.agileframework.dictionary.util.DictionaryUtil;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author 佟盟
 * 日期 2020/8/3 16:27
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class DictionaryDataManagerProxy {    private final DictionaryDataManager dictionaryDataManager;

    public DictionaryDataManagerProxy(DictionaryDataManager dictionaryDataManager) {
        this.dictionaryDataManager = dictionaryDataManager;
    }

    public List<DictionaryData> all() {
        return dictionaryDataManager.all();
    }

    public void add(DictionaryData dictionaryData) {
        dictionaryDataManager.add(dictionaryData);
        add(DictionaryEngine.CODE_CACHE, dictionaryData, DictionaryData::getFullCode);
        add(DictionaryEngine.NAME_CACHE, dictionaryData, DictionaryData::getFullName);
    }

    private void add(String cacheKey, DictionaryData dictionaryData, Function<DictionaryData, String> function) {
        Map<String, DictionaryData> map = DictionaryUtil.getCache().get(cacheKey, Map.class);
        assert map != null;
        DictionaryData parent = map.values().stream()
                .filter(n -> n.getId().equals(dictionaryData.getParentId()))
                .findFirst().orElse(null);

        if (parent != null) {
            dictionaryData.setFullCode(parent.getFullCode() + DictionaryEngine.DEFAULT_SPLIT_CHAR + dictionaryData.getCode());
            dictionaryData.setFullName(parent.getFullName() + DictionaryEngine.DEFAULT_SPLIT_CHAR + dictionaryData.getName());

            DictionaryUtil.getCache().addToMap(cacheKey, function.apply(dictionaryData), dictionaryData);

            parent.getChildren().add(dictionaryData);
            refreshParent(cacheKey, parent, function);

            //更新子
            List<DictionaryData> children = dictionaryData.getChildren();
            if (children == null) {
                children = Lists.newArrayList();
            }
            children.forEach(this::add);
        } else {
            dictionaryData.setFullCode(dictionaryData.getCode());
            dictionaryData.setFullName(dictionaryData.getName());
            DictionaryUtil.getCache().addToMap(cacheKey, function.apply(dictionaryData), dictionaryData);
        }


    }

    public void delete(String fullCode) {
        DictionaryData dictionaryData = DictionaryUtil.coverDicBean(fullCode);
        delete(dictionaryData);
    }

    public void delete(DictionaryData dictionaryData) {

        final List<DictionaryData> children = dictionaryData.getChildren();
        if (children != null && !children.isEmpty()) {
            children.forEach(this::delete);
        }

        dictionaryDataManager.delete(dictionaryData);
        delete(DictionaryEngine.CODE_CACHE, dictionaryData, DictionaryData::getFullCode);
        delete(DictionaryEngine.NAME_CACHE, dictionaryData, DictionaryData::getFullName);
    }

    private void delete(String cacheKey, DictionaryData dictionaryData, Function<DictionaryData, String> function) {
        DictionaryUtil.getCache().removeFromMap(cacheKey, function.apply(dictionaryData));

        Map<String, DictionaryData> map = DictionaryUtil.getCache().get(cacheKey, Map.class);
        assert map != null;
        DictionaryData parent = map.values().stream()
                .filter(n -> n.getId().equals(dictionaryData.getParentId()))
                .findFirst().orElse(null);

        if (parent == null) {
            return;
        }
        //遍历更新父节点
        parent.getChildren().removeIf(dic -> dic.getId().equals(dictionaryData.getId()));
        refreshParent(cacheKey, parent, function);
    }

    public void update(DictionaryData dictionaryData) {
        dictionaryDataManager.update(dictionaryData);
        updateCache(dictionaryData);
    }

    private void updateCache(DictionaryData dictionaryData) {
        update(DictionaryEngine.CODE_CACHE, dictionaryData, DictionaryData::getFullCode);
        update(DictionaryEngine.NAME_CACHE, dictionaryData, DictionaryData::getFullName);
    }

    private void update(String cacheKey, DictionaryData self, Function<DictionaryData, String> function) {
        //先删掉旧的
        DictionaryUtil.getCache().removeFromMap(cacheKey, function.apply(self));

        //更新子
        updateChild(cacheKey, self, function);

        //更新父
        Map<String, DictionaryData> map = DictionaryUtil.getCache().get(cacheKey, Map.class);
        assert map != null;
        DictionaryData parent = map.values().stream()
                .filter(n -> n.getId().equals(self.getParentId()))
                .findFirst().orElse(null);
        if (parent != null) {
            self.setFullCode(parent.getFullCode() + DictionaryEngine.DEFAULT_SPLIT_CHAR + self.getCode());
            self.setFullName(parent.getFullName() + DictionaryEngine.DEFAULT_SPLIT_CHAR + self.getName());

            final List<DictionaryData> brothers = parent.getChildren();
            brothers.removeIf(dic -> dic.getId().equals(self.getId()));
            brothers.add(self);
            refreshParent(cacheKey, parent, function);
        } else {
            self.setFullCode(self.getCode());
            self.setFullName(self.getName());
        }

        //缓存同步当前节点
        DictionaryUtil.getCache().addToMap(cacheKey, function.apply(self), self);
    }

    private void refreshParent(String cacheKey, DictionaryData dictionary, Function<DictionaryData, String> function) {
        if (dictionary == null) {
            return;
        }

        //缓存同步
        DictionaryUtil.getCache().addToMap(cacheKey, function.apply(dictionary), dictionary);

        Map<String, DictionaryData> map = DictionaryUtil.getCache().get(cacheKey, Map.class);
        assert map != null;
        DictionaryData parent = map.values().stream()
                .filter(n -> n.getId().equals(dictionary.getParentId()))
                .findFirst().orElse(null);

        if (parent != null) {
            final List<DictionaryData> brothers = parent.getChildren();
            brothers.removeIf(dic -> dic.getId().equals(dictionary.getId()));
            brothers.add(dictionary);
            refreshParent(cacheKey, parent, function);
        }
    }

    private void updateChild(String cacheKey, DictionaryData parent, Function<DictionaryData, String> function) {
        if (parent == null) {
            return;
        }
        parent.getChildren().forEach(child -> {
            child.setFullCode(parent.getFullCode() + DictionaryEngine.DEFAULT_SPLIT_CHAR + child.getCode());
            child.setFullCode(parent.getFullName() + DictionaryEngine.DEFAULT_SPLIT_CHAR + child.getName());
            updateChild(cacheKey, child, function);
            //缓存同步
            DictionaryUtil.getCache().addToMap(cacheKey, function.apply(parent), parent);
        });
    }
}
