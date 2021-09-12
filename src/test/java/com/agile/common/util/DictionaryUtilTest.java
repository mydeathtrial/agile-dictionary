package com.agile.common.util;

import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryEngine;
import cloud.agileframework.dictionary.MemoryDictionaryManager;
import cloud.agileframework.dictionary.util.DictionaryUtil;
import com.agile.App;
import com.agile.DictionaryDataMemory;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.SerializationUtils;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class DictionaryUtilTest {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MemoryDictionaryManager manager;

    @Test
    public void getCache() {
        SortedSet<DictionaryDataBase> code = manager.sync().all();
        Assert.assertNotNull(code);
    }

    @Test
    public void coverDicBean() {
        DictionaryDataBase dic1 = DictionaryUtil.coverDicBean("sex.boy");
        Assert.assertEquals(dic1.getName(),"男");
        DictionaryDataBase dic2 = DictionaryUtil.coverDicBean("sex#boy", "#");
        Assert.assertEquals(dic2.getName(),"男");
        IntStream.range(0, 10)
                .forEach(a -> new Thread(() -> Assert.assertEquals(DictionaryUtil.coverDicBean("sex.boy").getName(),"男")).start());
    }

    @Test
    public void coverDicBeanByFullName() {
        DictionaryDataBase dic1 = DictionaryUtil.coverDicBeanByFullName("性别.男");
        Assert.assertEquals(dic1.getFullCode(),"sex$SPLIT$boy");
    }

    @Test
    public void testCoverDicBeanByFullName() {
        DictionaryDataBase dic1 = DictionaryUtil.coverDicBeanByFullName("性别|男", "|");
        Assert.assertEquals(dic1.getFullCode(),"sex$SPLIT$boy");
    }

    @Test
    public void coverDicBeanByParent() {
        DictionaryDataBase dic1 = DictionaryUtil.coverDicBeanByParent("sex", "男");
        Assert.assertEquals(dic1.getFullCode(),"sex$SPLIT$boy");
    }

    @Test
    public void coverDicName() {
        String name = DictionaryUtil.coverDicName("sex");
        Assert.assertEquals(name,"性别");
        String name2 = DictionaryUtil.coverDicName("sex.boy");
        Assert.assertEquals(name2,"男");
        String name3 = DictionaryUtil.coverDicName("sex.no", "未知");
        Assert.assertEquals(name3,"未知");
    }

    @Test
    public void coverDicNameByParent() {
        String name = DictionaryUtil.coverDicNameByParent("sex", "boy,girl");
        Assert.assertEquals(name,"男,女");
        String name2 = DictionaryUtil.coverDicNameByParent("sex", "neutral,boy,girl", "中性");
        Assert.assertEquals(name2,"中性,男,女");
        String name3 = DictionaryUtil.coverDicNameByParent("sex", "neutral,boy,girl", "中性", true, "#");
        Assert.assertEquals(name3,"性别#中性,性别#男,性别#女");
    }

    @Test
    public void coverDicCode() {
        String code = DictionaryUtil.coverDicCode("性别.男,性别.女");
        Assert.assertEquals(code,"boy,girl");
        String code2 = DictionaryUtil.coverDicCode("性别.男,性别.女,性别.中性", "no");
        Assert.assertEquals(code2,"boy,girl,no");
        String code3 = DictionaryUtil.coverDicCode("性别|男,性别|女,性别|中性", "no", true, "|");
        Assert.assertEquals(code3,"sex|boy,sex|girl,no");
    }

    @Test
    public void coverDicCodeByParent() {
        String code = DictionaryUtil.coverDicCodeByParent("性别", "男,女");
        Assert.assertEquals(code,"boy,girl");
        String code2 = DictionaryUtil.coverDicCodeByParent("性别", "男,女,中性", "no");
        Assert.assertEquals(code2,"boy,girl,no");
        String code3 = DictionaryUtil.coverDicCodeByParent("性别", "男,女,中性", "no", true, "|");
        Assert.assertEquals(code3,"sex|boy,sex|girl,sex|no");
    }

    @Test
    public void coverMapDictionary() {
        List<Map<String, Object>> list = Lists.newArrayList();
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("code", "boy");
        list.add(map);

        HashMap<String, Object> map2 = Maps.newHashMap();
        map2.put("code", "girl");
        list.add(map2);

        Map<String, Object> o = DictionaryUtil.coverMapDictionary(map, new String[]{"sex"}, "_value", new String[]{"code"});
        Assert.assertEquals(o.get("code_value"),"男");

        List<Map<String, Object>> toList1 = DictionaryUtil.coverMapDictionary(list, new String[]{"sex"}, "_value", new String[]{"code"});
        Assert.assertArrayEquals(toList1.stream().map(a -> a.get("code_value")).sorted().toArray(), Arrays.stream(new String[]{"男","女"}).sorted().toArray());

        List<Map<String, Object>> toList2 = DictionaryUtil.coverMapDictionary(list, "sex", "_value", "code");
        Assert.assertArrayEquals(toList2.stream().map(a -> a.get("code_value")).sorted().toArray(), Arrays.stream(new String[]{"男","女"}).sorted().toArray());
    }

    @Test
    public void cover() {
        Data2 o2 = Data2.builder().country("7").city("8").region("9,9").build();
        Data2 o3 = Data2.builder().country("7").city("8").region("9").build();
        o2.setData2(o3);
        DictionaryUtil.cover(o2);
        Assert.assertEquals(o2.getCountryValue(),"中国");
        Assert.assertEquals(o2.getCityValue(),"黑龙江");
        Assert.assertArrayEquals(o2.getRegionValue().stream().sorted().toArray(), Arrays.stream(new String[]{"中国.黑龙江.哈尔滨","中国.黑龙江.哈尔滨"}).sorted().toArray());

        Data5 o4 = Data5.builder().code(SexEnum.boy).build();
        DictionaryUtil.cover(o4);
        Assert.assertEquals(o4.getText(),"男");
    }

    @Test
    public void time() {
        ArrayList<Object> list = Lists.newArrayList();
        IntStream.range(0, 1000).forEach(a -> list.add(Data3.builder().status("sex.boy").build()));
        long start = System.currentTimeMillis();
        DictionaryUtil.cover(list);
        long end = System.currentTimeMillis();
        //计算每秒转化次数
        double count = (BigDecimal.valueOf(1000 * 1000).divide(BigDecimal.valueOf(end - start), RoundingMode.CEILING).doubleValue());
        System.out.println(count);
        Assert.assertTrue(count>6000);
    }

    @Before
    public void init(){
        manager.sync().add(new DictionaryDataMemory("1", null, "性别", "sex",3));
        Assert.assertEquals(DictionaryUtil.coverDicName("sex"),"性别");
        manager.sync().add(new DictionaryDataMemory("2", null, "对错", "isTrue",3));
        Assert.assertEquals(DictionaryUtil.coverDicName("isTrue"),"对错");
        manager.sync().add(new DictionaryDataMemory("3", "1", "男", "boy",6));
        Assert.assertEquals(DictionaryUtil.coverDicName("sex.boy"),"男");
        manager.sync().add(new DictionaryDataMemory("4", "1", "女", "girl",5));
        Assert.assertEquals(DictionaryUtil.coverDicName("sex.girl"),"女");
        manager.sync().add(new DictionaryDataMemory("5", "2", "对", "1",2));
        Assert.assertEquals(DictionaryUtil.coverDicName("isTrue.1"),"对");
        manager.sync().add(new DictionaryDataMemory("6", "2", "错", "2",8));
        Assert.assertEquals(DictionaryUtil.coverDicName("isTrue.2"),"错");
        manager.sync().add(new DictionaryDataMemory("7", null, "中国", "7",9));
        Assert.assertEquals(DictionaryUtil.coverDicName("7"),"中国");
        manager.sync().add(new DictionaryDataMemory("8", "7", "黑龙江", "8",0));
        Assert.assertEquals(DictionaryUtil.coverDicName("7.8"),"黑龙江");
        manager.sync().add(new DictionaryDataMemory("9", "8", "哈尔滨", "9",1));
        Assert.assertEquals(DictionaryUtil.coverDicName("7.8.9"),"哈尔滨");
    }

    @Test
    public void add() {
        final DictionaryDataBase dictionaryData = new DictionaryDataMemory("31", "3", boy1Name(), "boy1");
        manager.sync().add(dictionaryData);
        Assert.assertEquals("新增失败",DictionaryUtil.coverDicName("sex.boy.boy1"), boy1Name());

        DictionaryDataBase sexDic = DictionaryUtil.coverDicBean("sex");
        DictionaryDataBase boyDic = sexDic.getChildren()
                .stream()
                .filter(a -> "sex.boy".equals(a.getFullCode().replace(DictionaryEngine.DEFAULT_SPLIT_CHAR, ".")))
                .findFirst().orElseThrow(RuntimeException::new);
        DictionaryDataBase boy1Dic = boyDic.getChildren()
                .stream()
                .filter(a -> "sex.boy.boy1".equals(a.getFullCode().replace(DictionaryEngine.DEFAULT_SPLIT_CHAR, ".")))
                .findFirst().orElseThrow(RuntimeException::new);
        Assert.assertEquals("新增失败",boy1Dic.getName(), boy1Name());

        final String updatedName = "男-1";
        dictionaryData.setName(updatedName);
        dictionaryData.setCode("boy-1");
        manager.sync().update(dictionaryData);
        Assert.assertEquals("更新失败",DictionaryUtil.coverDicName("sex.boy.boy-1"), updatedName);

        sexDic = DictionaryUtil.coverDicBean("sex");
        boyDic = sexDic.getChildren()
                .stream()
                .filter(a -> "sex.boy".equals(a.getFullCode().replace(DictionaryEngine.DEFAULT_SPLIT_CHAR, ".")))
                .findFirst().orElseThrow(RuntimeException::new);
        boy1Dic = boyDic.getChildren()
                .stream()
                .filter(a -> "sex.boy.boy-1".equals(a.getFullCode().replace(DictionaryEngine.DEFAULT_SPLIT_CHAR, ".")))
                .findFirst().orElseThrow(RuntimeException::new);
        Assert.assertEquals("更新失败",boy1Dic.getName(),updatedName);

        DictionaryDataBase a = DictionaryUtil.findById(manager.dataSource(), "9");
        final String value = "tudou";
        a.setName(value);
        a.setCode("1212");
        manager.sync().update(a);
        Assert.assertEquals("更新失败",DictionaryUtil.coverDicName("7.8.1212"), value);

        DictionaryDataBase dic7 = DictionaryUtil.coverDicBean("7");
        DictionaryDataBase dic8 = dic7.getChildren()
                .stream()
                .filter(n -> "7.8".equals(n.getFullCode().replace(DictionaryEngine.DEFAULT_SPLIT_CHAR, ".")))
                .findFirst().orElseThrow(RuntimeException::new);
        DictionaryDataBase dic9 = dic8.getChildren()
                .stream()
                .filter(n -> "7.8.1212".equals(n.getFullCode().replace(DictionaryEngine.DEFAULT_SPLIT_CHAR, ".")))
                .findFirst().orElseThrow(RuntimeException::new);
        Assert.assertEquals("更新失败",dic9.getName(),value);

        final String fullCode = DictionaryUtil.findById(manager.dataSource(), dictionaryData.getId()).getFullCode();
        manager.sync().delete(fullCode);
        Assert.assertNull("删除失败",DictionaryUtil.coverDicBean(fullCode));

        DictionaryDataBase parent = DictionaryUtil.findById(manager.dataSource(), dictionaryData.getParentId());
        boolean isHave = parent.getChildren()
                .stream().anyMatch(n -> fullCode.equals(n.getFullCode()));
        Assert.assertFalse("删除失败",isHave);
    }

    private String boy1Name() {
        return "男1";
    }

    @Test
    public void tree() {
        SortedSet<DictionaryDataBase> a = manager.sync().tree();
        Assert.assertFalse(a.isEmpty());
    }

    static {
        MemoryDictionaryManager.cache().add(new DictionaryDataMemory("1", null, "性别", "sex",3));
        MemoryDictionaryManager.cache().add(new DictionaryDataMemory("2", null, "对错", "isTrue",3));
        MemoryDictionaryManager.cache().add(new DictionaryDataMemory("3", "1", "男", "boy",6));
        MemoryDictionaryManager.cache().add(new DictionaryDataMemory("4", "1", "女", "girl",5));
        MemoryDictionaryManager.cache().add(new DictionaryDataMemory("5", "2", "对", "1",2));
        MemoryDictionaryManager.cache().add(new DictionaryDataMemory("6", "2", "错", "2",8));
        MemoryDictionaryManager.cache().add(new DictionaryDataMemory("7", null, "中国", "7",9));
        MemoryDictionaryManager.cache().add(new DictionaryDataMemory("8", "7", "黑龙江", "8",0));
        MemoryDictionaryManager.cache().add(new DictionaryDataMemory("9", "8", "哈尔滨", "9",1));
    }
}