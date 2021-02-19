package com.agile.common.util;

import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.MemoryDictionaryManager;
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
        add(new DictionaryDataBase("1", null, "性别", "sex"));
        add(new DictionaryDataBase("2", null, "对错", "isTrue"));
        add(new DictionaryDataBase("3", "1", "男", "boy"));
        add(new DictionaryDataBase("4", "1", "女", "girl"));
        add(new DictionaryDataBase("5", "2", "对", "1"));
        add(new DictionaryDataBase("6", "2", "错", "2"));
        add(new DictionaryDataBase("7", null, "中国", "7"));
        add(new DictionaryDataBase("8", "7", "黑龙江", "8"));
        add(new DictionaryDataBase("9", "8", "哈尔滨", "9"));
    }
}
