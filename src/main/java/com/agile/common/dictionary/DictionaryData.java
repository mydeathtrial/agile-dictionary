package com.agile.common.dictionary;

import java.io.Serializable;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2020/7/30 19:56
 * 描述 字典数据
 * @version 1.0
 * @since 1.0
 */
public interface DictionaryData extends Serializable {
    /**
     * 字典唯一标识
     *
     * @return 字典唯一标识
     */
    String getId();

    /**
     * 字典父级标识
     *
     * @return 字典父级标识
     */
    String getParentId();

    /**
     * 字典编码
     *
     * @return 字典编码
     */
    String getCode();

    /**
     * 字典显示名
     *
     * @return 字典显示名
     */
    String getName();

    /**
     * 全路径字典码
     *
     * @return 全路径字典码
     */
    String getFullCode();

    /**
     * 全路径字典码
     *
     * @param fullCode 全路径字典码
     */
    void setFullCode(String fullCode);

    /**
     * 全路径字典名
     *
     * @return 全路径字典名
     */
    String getFullName();

    /**
     * 全路径字典名
     *
     * @param fullName 全路径字典名
     */
    void setFullName(String fullName);

    /**
     * 子字典集
     *
     * @return 子字典集
     */
    List<DictionaryData> getChildren();


}
