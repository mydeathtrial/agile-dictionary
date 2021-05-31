package com.agile;

import cloud.agileframework.dictionary.DictionaryDataBase;

import static cloud.agileframework.dictionary.MemoryDictionaryManager.CACHE;

/**
 * @author 佟盟
 * 日期 2021-03-01 16:43
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class DictionaryDataMemory extends DictionaryDataBase {
    public DictionaryDataMemory(String id, String parentId, String name, String code) {
        super(id, parentId, name, code);
    }

    public DictionaryDataMemory(String id, String parentId, String name, String code,Integer sort) {
        super(id, parentId, name, code);
        setSort(sort);
    }

    public DictionaryDataMemory() {
    }

}
