package com.agile.common.util;

import com.agile.App;
import com.agile.common.annotation.Dictionary;
import com.agile.common.cache.AgileCache;
import com.agile.common.dictionary.DictionaryData;
import com.agile.common.dictionary.DictionaryDataManager;
import com.agile.common.dictionary.DictionaryDataManagerProxy;
import com.agile.common.dictionary.DictionaryEngine;
import com.agile.common.dictionary.MemoryDictionaryData;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class DictionaryUtilTest {

    private final Logger logger = LoggerFactory.getLogger(DictionaryUtilTest.class);

    @Autowired
    private DictionaryDataManagerProxy manager;

    @Before
    public void init() {
        manager.add(new MemoryDictionaryData("1", null, "状态", "status"));
        manager.add(new MemoryDictionaryData("2", null, "对错", "isTrue"));
        manager.add(new MemoryDictionaryData("3", "1", "状态1", "1"));
        manager.add(new MemoryDictionaryData("4", "1", "状态2", "2"));
        manager.add(new MemoryDictionaryData("5", "2", "对", "1"));
        manager.add(new MemoryDictionaryData("6", "2", "错", "2"));
    }

    @Test
    public void getCache() {
        AgileCache cache = DictionaryUtil.getCache();
        Map code = cache.get(DictionaryEngine.CODE_CACHE, Map.class);
        logger.info(code.toString());
    }

    @Test
    public void coverDicBean() {
        DictionaryData dic1 = DictionaryUtil.coverDicBean("status.1");
        DictionaryData dic2 = DictionaryUtil.coverDicBean("status#1", "#");
        logger.info(dic1.getName());
        logger.info(dic2.getName());
    }

    @Test
    public void coverDicBeanByFullName() {
        DictionaryData dic1 = DictionaryUtil.coverDicBeanByFullName("状态.状态1");
        logger.info(dic1.getFullCode());
    }

    @Test
    public void testCoverDicBeanByFullName() {
        DictionaryData dic1 = DictionaryUtil.coverDicBeanByFullName("状态|状态1", "|");
        logger.info(dic1.getFullCode());
    }

    @Test
    public void coverDicBeanByParent() {
        DictionaryData dic1 = DictionaryUtil.coverDicBeanByParent("status", "状态1");
        logger.info(dic1.getFullName());
    }

    @Test
    public void coverDicName() {
        String name = DictionaryUtil.coverDicName("status");
        logger.info(name);
        String name2 = DictionaryUtil.coverDicName("status.1");
        logger.info(name2);
        String name3 = DictionaryUtil.coverDicName("status.3", "tudou");
        logger.info(name3);
    }

    @Test
    public void coverDicNameByParent() {
        String name = DictionaryUtil.coverDicNameByParent("status", "1,2");
        logger.info(name);
        String name2 = DictionaryUtil.coverDicNameByParent("status", "0,1,2", "tudou");
        logger.info(name2);
        String name3 = DictionaryUtil.coverDicNameByParent("status", "0,1,2", "tudou", true, "#");
        logger.info(name3);
    }

    @Test
    public void coverDicCode() {
        String code = DictionaryUtil.coverDicCode("状态.状态1,状态.状态2");
        logger.info(code);
        String code2 = DictionaryUtil.coverDicCode("状态.状态1,状态.状态2,状态.状态3", "default");
        logger.info(code2);
        String code3 = DictionaryUtil.coverDicCode("状态|状态1,状态|状态2,状态|状态3", "default", true, "|");
        logger.info(code3);
    }

    @Test
    public void coverDicCodeByParent() {
        String code = DictionaryUtil.coverDicCodeByParent("状态", "状态1,状态2");
        logger.info(code);
        String code2 = DictionaryUtil.coverDicCodeByParent("状态", "状态1,状态2,状态3", "default");
        logger.info(code2);
        String code3 = DictionaryUtil.coverDicCodeByParent("状态", "状态1,状态2,状态3", "default", true, "|");
        logger.info(code3);
    }

    public static class TuDou {
        private Integer status;
        private String text;

        public TuDou(Integer status) {
            this.status = status;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "TuDou{" +
                    "status=" + status +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    @Test
    public void coverMapDictionary() throws NoSuchFieldException, IllegalAccessException {
        List<Object> list = Lists.newArrayList();
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("status", "1");
        list.add(map);

        HashMap<String, Object> map2 = Maps.newHashMap();
        map2.put("status", "2");
        list.add(map2);

        list.add(new TuDou(2));

        Map<String, Object> o = DictionaryUtil.coverMapDictionary(map, new String[]{"status"}, "_value", new String[]{"status"});
        logger.info(o.toString());

        List<Map<String, Object>> toList1 = DictionaryUtil.coverMapDictionary(list, new String[]{"status"}, "_value", new String[]{"status"});
        logger.info(toList1.toString());

        List<Map<String, Object>> toList2 = DictionaryUtil.coverMapDictionary(list, "status", "_value", "status");
        logger.info(toList2.toString());
    }

    @Test
    public void coverBeanDictionary() throws NoSuchFieldException, IllegalAccessException {
        List<Object> list = Lists.newArrayList();
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("status", "1");
        list.add(map);

        HashMap<String, Object> map2 = Maps.newHashMap();
        map2.put("status", "2");
        list.add(map2);

        list.add(new TuDou(2));
        list.add(new TuDou(3));

        TuDou o = DictionaryUtil.coverBeanDictionary(new TuDou(2), new String[]{"status"}, new String[]{"status"}, new String[]{"text"});
        logger.info(o.toString());

        TuDou o1 = DictionaryUtil.coverBeanDictionary(new TuDou(3), new String[]{"status"}, new String[]{"status"}, new String[]{"text"}, new String[]{"default"});
        logger.info(o1.toString());

        List<Object> list1 = DictionaryUtil.coverBeanDictionary(list, "status", "status", "text");
        logger.info(list1.toString());

        List<Object> list2 = DictionaryUtil.coverBeanDictionary(list, new String[]{"status"}, new String[]{"status"}, new String[]{"text"}, new String[]{"default"});
        logger.info(list2.toString());
    }

    public static class TuDou2 {
        private String status;
        @Dictionary(fieldName = "status", isFull = true)
        private String text;

        public TuDou2(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "TuDou{" +
                    "status=" + status +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    @Test
    public void cover() {
        Object o = new TuDou2("status.1");
        DictionaryUtil.cover(o);
        System.out.println(o.toString());

        ArrayList<Object> list = Lists.newArrayList();
        list.add(new TuDou2("status.1"));
        list.add(new TuDou2("status.2"));
        DictionaryUtil.cover(list);
        System.out.println(list.toString());
    }
}