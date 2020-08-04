package com.agile.common.dictionary;

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
    List<DictionaryData> all();

    /**
     * 新增字典
     *
     * @param dictionaryData 字典
     */
    void add(DictionaryData dictionaryData);

    /**
     * 删除字典
     *
     * @param code 字典码
     */
    void delete(String code);

    /**
     * 更新字典
     *
     * @param dictionaryData 字典数据
     */
    void update(DictionaryData dictionaryData);
}
