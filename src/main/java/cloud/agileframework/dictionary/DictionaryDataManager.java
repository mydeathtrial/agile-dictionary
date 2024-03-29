package cloud.agileframework.dictionary;

import java.util.List;

/**
 * @author 佟盟
 * 日期 2020/7/30 19:44
 * 描述 字典数据管理器
 * @version 1.0
 * @since 1.0
 */
public interface DictionaryDataManager<D extends DictionaryDataBase> {
    /**
     * 获取所有字典数据
     *
     * @return 字典数据集合
     */
    List<D> all();

    /**
     * 新增字典
     *
     * @param dictionaryDataBase 字典
     */
    void add(D dictionaryDataBase);

    /**
     * 删除字典
     *
     * @param dictionaryDataBase 字典
     */
    void delete(D dictionaryDataBase);

    /**
     * 更新字典
     *
     * @param dictionaryDataBase 字典数据
     */
    default D update(D dictionaryDataBase) {
        return updateOfNotNull(dictionaryDataBase);
    }

    /**
     * 更新字典，只更新非空字段
     *
     * @param dictionaryDataBase 字典数据
     */
    D updateOfNotNull(D dictionaryDataBase);

    /**
     * 唯一的数据源标识
     *
     * @return 用于Convert注解中dataSource声明，将数据按照数据源标识划分成不同存储区域
     */
    default String dataSource() {
        return DictionaryEngine.DICTIONARY_DATA_CACHE;
    }

    /**
     * 取根节点数据的parentId值
     *
     * @return 默认返回空
     */
    default String rootParentId() {
        return null;
    }
}
