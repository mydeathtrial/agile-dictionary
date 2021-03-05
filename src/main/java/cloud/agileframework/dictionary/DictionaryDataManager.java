package cloud.agileframework.dictionary;

import java.util.List;

/**
 * @author 佟盟
 * 日期 2020/7/30 19:44
 * 描述 字典数据管理器
 * @version 1.0
 * @since 1.0
 */
public interface DictionaryDataManager {
    /**
     * 获取所有字典数据
     *
     * @return 字典数据集合
     */
    List<DictionaryDataBase> all();

    /**
     * 新增字典
     *
     * @param dictionaryDataBase 字典
     */
    <D extends DictionaryDataBase> void add(D dictionaryDataBase);

    /**
     * 删除字典
     *
     * @param dictionaryDataBase 字典
     */
    <D extends DictionaryDataBase> void delete(D dictionaryDataBase);

    /**
     * 更新字典
     *
     * @param dictionaryDataBase 字典数据
     */
    <D extends DictionaryDataBase> void update(D dictionaryDataBase);
}
