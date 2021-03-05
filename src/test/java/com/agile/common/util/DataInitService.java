package com.agile.common.util;

import cloud.agileframework.dictionary.MemoryDictionaryManager;
import com.agile.DictionaryDataMemory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author 佟盟
 * 日期 2021-02-01 15:20
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Component
public class DataInitService extends MemoryDictionaryManager implements InitializingBean {
    @Override
    public void afterPropertiesSet() {
        add(new DictionaryDataMemory("1", null, "性别", "sex"));
        add(new DictionaryDataMemory("2", null, "对错", "isTrue"));
        add(new DictionaryDataMemory("3", "1", "男", "boy"));
        add(new DictionaryDataMemory("4", "1", "女", "girl"));
        add(new DictionaryDataMemory("5", "2", "对", "1"));
        add(new DictionaryDataMemory("6", "2", "错", "2"));
        add(new DictionaryDataMemory("7", null, "中国", "7"));
        add(new DictionaryDataMemory("8", "7", "黑龙江", "8"));
        add(new DictionaryDataMemory("9", "8", "哈尔滨", "9"));
    }
}
