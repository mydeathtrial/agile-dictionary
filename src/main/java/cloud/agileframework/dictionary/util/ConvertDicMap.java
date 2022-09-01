package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.constant.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 佟盟
 * 日期 2021-03-24 19:46
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ConvertDicMap extends ConvertDicName {
    static final String CODE_FORMAT = "%s$$%s";

    public static <T extends Map<String, ?>> List<Map<String, Object>> coverMapDictionary(List<T> list,
                                                                                          String dictionaryCode,
                                                                                          String suffix,
                                                                                          String column) {
        return coverMapDictionary(list,
                new String[]{dictionaryCode},
                suffix,
                new String[]{column}, Constant.AgileAbout.DIC_SPLIT);
    }

    /**
     * 集合类型转换字典码工具，转换为List/Map类型
     *
     * @param list           要进行转换的集合
     * @param dictionaryCode 要使用的字典码
     * @param suffix         转换出来的字典值存储的字段后缀
     * @param column         转换字段集
     * @param <T>            泛型
     * @return 返回List/Map类型，增加_text字段
     */
    public static <T extends Map<String, ?>> List<Map<String, Object>> coverMapDictionary(List<T> list,
                                                                                          String dictionaryCode,
                                                                                          String suffix,
                                                                                          String column,
                                                                                          String splitChar) {
        return coverMapDictionary(list,
                new String[]{dictionaryCode},
                suffix,
                new String[]{column}, splitChar);
    }

    public static <T extends Map<String, ?>> List<Map<String, Object>> coverMapDictionary(List<T> list,
                                                                                          String[] dictionaryCodes,
                                                                                          String suffix,
                                                                                          String[] columns) {
        return coverMapDictionary(list,
                dictionaryCodes,
                suffix,
                columns, Constant.AgileAbout.DIC_SPLIT);
    }

    public static <T extends Map<String, ?>> List<Map<String, Object>> coverMapDictionary(List<T> list,
                                                                                          String[] dictionaryCodes,
                                                                                          String suffix,
                                                                                          String[] columns,
                                                                                          String splitChar) {
        if (dictionaryCodes == null || columns == null || dictionaryCodes.length != columns.length) {
            throw new IllegalArgumentException("dictionaryCodes and columns should be the same length");
        }
        List<Map<String, Object>> result = new ArrayList<>(list.size());
        for (T o : list) {
            result.add(coverMapDictionary(o,
                    dictionaryCodes,
                    suffix,
                    columns,
                    splitChar)
            );
        }
        return result;
    }

    public static <T extends Map<String, ?>> Map<String, Object> coverMapDictionary(T o,
                                                                                    String[] dictionaryCodes,
                                                                                    String suffix,
                                                                                    String[] columns) {
        return coverMapDictionary(o, dictionaryCodes, suffix, columns, Constant.AgileAbout.DIC_SPLIT);
    }

    /**
     * 对象转换字典
     *
     * @param o               pojo或map对象
     * @param dictionaryCodes 要转换的columns对应的字典码，其长度与columns长度应该保持一致，一一对应关系
     * @param suffix          转换后的字典值存放到结果集map中的key值后缀
     * @param columns         要转换的pojo属性名或map的key值数组
     * @param <T>             泛型
     * @return 转换后的Map结果数据
     */
    public static <T extends Map<String, ?>> Map<String, Object> coverMapDictionary(T o,
                                                                                    String[] dictionaryCodes,
                                                                                    String suffix,
                                                                                    String[] columns,
                                                                                    String splitChar) {
        Map<String, Object> map = (Map<String, Object>) o;
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            map.put(column + suffix, coverDicName(dictionaryCodes[i] + splitChar + map.get(column)));
        }
        return map;
    }

}
