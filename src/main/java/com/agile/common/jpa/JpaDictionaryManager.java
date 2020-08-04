package com.agile.common.jpa;

import com.agile.common.dictionary.DictionaryManager;
import com.agile.common.util.DictionaryUtil;

/**
 * @author 佟盟
 * 日期 2020/8/4 9:42
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class JpaDictionaryManager implements DictionaryManager {
    @Override
    public void cover(Object o) {
        DictionaryUtil.cover(o);
    }
}
