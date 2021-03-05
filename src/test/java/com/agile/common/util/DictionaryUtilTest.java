package com.agile.common.util;

import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryDataManagerProxy;
import cloud.agileframework.dictionary.DictionaryEngine;
import cloud.agileframework.dictionary.util.DictionaryUtil;
import com.agile.App;
import com.agile.DictionaryDataMemory;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class DictionaryUtilTest {

    @Autowired
    private DictionaryDataManagerProxy manager;

    @Test
    public void getCache() {
        List<DictionaryDataBase> code = DictionaryEngine.getAllMemory();
        log.info(code.toString());
    }

    @Test
    public void coverDicBean() {
        DictionaryDataBase dic1 = DictionaryUtil.coverDicBean("sex.boy");
        DictionaryDataBase dic2 = DictionaryUtil.coverDicBean("sex#boy", "#");
        log.info(dic1.getName());
        log.info(dic2.getName());
        IntStream.range(0, 10).forEach(a -> {
            new Thread() {
                @Override
                public void run() {
                    log.info(getId() + DictionaryUtil.coverDicBean("sex.boy").getName());
                }
            }.run();
        });
    }

    @Test
    public void coverDicBeanByFullName() {
        DictionaryDataBase dic1 = DictionaryUtil.coverDicBeanByFullName("性别.男");
        log.info(dic1.getFullCode());
    }

    @Test
    public void testCoverDicBeanByFullName() {
        DictionaryDataBase dic1 = DictionaryUtil.coverDicBeanByFullName("性别|男", "|");
        log.info(dic1.getFullCode());
    }

    @Test
    public void coverDicBeanByParent() {
        DictionaryDataBase dic1 = DictionaryUtil.coverDicBeanByParent("sex", "男");
        log.info(dic1.getFullName());
    }

    @Test
    public void coverDicName() {
        String name = DictionaryUtil.coverDicName("sex");
        log.info(name);
        String name2 = DictionaryUtil.coverDicName("sex.boy");
        log.info(name2);
        String name3 = DictionaryUtil.coverDicName("sex.no", "未知");
        log.info(name3);
    }

    @Test
    public void coverDicNameByParent() {
        String name = DictionaryUtil.coverDicNameByParent("sex", "boy,girl");
        log.info(name);
        String name2 = DictionaryUtil.coverDicNameByParent("sex", "neutral,boy,girl", "中性");
        log.info(name2);
        String name3 = DictionaryUtil.coverDicNameByParent("sex", "neutral,boy,girl", "中性", true, "#");
        log.info(name3);
    }

    @Test
    public void coverDicCode() {
        String code = DictionaryUtil.coverDicCode("性别.男,性别.女");
        log.info(code);
        String code2 = DictionaryUtil.coverDicCode("性别.男,性别.女,性别.中性", "no");
        log.info(code2);
        String code3 = DictionaryUtil.coverDicCode("性别|男,性别|女,性别|中性", "no", true, "|");
        log.info(code3);
    }

    @Test
    public void coverDicCodeByParent() {
        String code = DictionaryUtil.coverDicCodeByParent("性别", "男,女");
        log.info(code);
        String code2 = DictionaryUtil.coverDicCodeByParent("性别", "男,女,中性", "no");
        log.info(code2);
        String code3 = DictionaryUtil.coverDicCodeByParent("性别", "男,女,中性", "no", true, "|");
        log.info(code3);
    }

    @Test
    public void coverMapDictionary() throws NoSuchFieldException, IllegalAccessException {
        List<Object> list = Lists.newArrayList();
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("code", "boy");
        list.add(map);

        HashMap<String, Object> map2 = Maps.newHashMap();
        map2.put("code", "girl");
        list.add(map2);

        list.add(Data5.builder().code(SexEnum.boy).build());

        Map<String, Object> o = DictionaryUtil.coverMapDictionary(map, new String[]{"sex"}, "_value", new String[]{"code"});
        log.info(o.toString());

        List<Map<String, Object>> toList1 = DictionaryUtil.coverMapDictionary(list, new String[]{"sex"}, "_value", new String[]{"code"});
        log.info(toList1.toString());

        List<Map<String, Object>> toList2 = DictionaryUtil.coverMapDictionary(list, "sex", "_value", "code");
        log.info(toList2.toString());
    }

    @Test
    public void coverBeanDictionary() throws NoSuchFieldException, IllegalAccessException {
        List<Object> list = Lists.newArrayList();
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("sex", "boy");
        list.add(map);

        HashMap<String, Object> map2 = Maps.newHashMap();
        map2.put("sex", "no");
        list.add(map2);

        list.add(Data1.builder().sex("boy").build());
        list.add(Data1.builder().sex("no").build());

        Data1 o = DictionaryUtil.coverBeanDictionary(Data1.builder().sex("boy").build(), new String[]{"sex"}, new String[]{"sex"}, new String[]{"text"});
        log.info(o.toString());

        Data1 o1 = DictionaryUtil.coverBeanDictionary(Data1.builder().sex("no").build(), new String[]{"sex"}, new String[]{"sex"}, new String[]{"text"}, new String[]{"default"});
        log.info(o1.toString());

        List<Object> list1 = DictionaryUtil.coverBeanDictionary(list, "sex", "sex", "text");
        log.info(list1.toString());

        List<Object> list2 = DictionaryUtil.coverBeanDictionary(list, new String[]{"sex"}, new String[]{"sex"}, new String[]{"text"}, new String[]{"default"});
        log.info(list2.toString());
    }

    @Test
    public void cover() {
        Data3 o = Data3.builder().status("sex.boy").build();
        DictionaryUtil.cover(o);
        System.out.println(o.toString());

        Data2 o2 = Data2.builder().country(7).city(8).region(9).build();
        DictionaryUtil.cover(o2);
        System.out.println(o2.toString());

        Data4 o3 = Data4.builder().country("中国").city("黑龙江").region("哈尔滨").build();
        DictionaryUtil.cover(o3);
        System.out.println(o3.toString());

        Data5 o4 = Data5.builder().code(SexEnum.boy).build();
        DictionaryUtil.cover(o4);
        System.out.println(o4.toString());

    }

    @Test
    public void time() {
        long start = System.currentTimeMillis();
        ArrayList<Object> list = Lists.newArrayList();
        IntStream.range(0, 1000).forEach(a -> list.add(Data3.builder().status("sex.boy").build()));
        DictionaryUtil.cover(list);
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void add() throws IOException {
        final DictionaryDataBase dictionaryData = new DictionaryDataMemory("31", "3", "男1", "boy1");
        manager.add(dictionaryData);
    }
}