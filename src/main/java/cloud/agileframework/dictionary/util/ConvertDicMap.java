package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.dictionary.annotation.DirectionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2021-03-24 19:46
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ConvertDicMap {
    private ConvertDicMap() {
    }

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
        List<Map<String, Object>> result = new ArrayList<>(list.size());
        for (T o : list) {
            result.add(coverMapDictionary(o,
                    dictionaryCodes,
                    columns,
                    suffix,
                    splitChar)
            );
        }
        return result;
    }

    public static <T extends Map<String, ?>> Map<String, Object> coverMapDictionary(T o,
                                                                                    String[] dictionaryCodes,
                                                                                    String suffix,
                                                                                    String[] columns) {
        return coverMapDictionary(o, dictionaryCodes, columns, suffix, Constant.AgileAbout.DIC_SPLIT);
    }

    public static <T extends Map<String, ?>> Map<String, Object> coverMapDictionary(T o,
                                                                                    String suffix,
                                                                                    String[] columns) {
        return coverMapDictionary(o, null, columns, suffix, Constant.AgileAbout.DIC_SPLIT);
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
                                                                                    String[] columns,
                                                                                    String suffix,
                                                                                    String splitChar) {
        String[] columnNames = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            columnNames[i] = column + suffix;
        }
        return coverMapDictionary(o, dictionaryCodes, columns, columnNames, splitChar);
    }

    public static <T extends Map<String, ?>> Map<String, Object> coverMapDictionary(T o, String[] columns, String[] columnNames, String splitChar) {
        return coverMapDictionary(o, null, columns, columnNames, splitChar);
    }

    public static <T extends Map<String, ?>> Map<String, Object> coverMapDictionary(T o, String[] columns, String[] columnNames) {
        return coverMapDictionary(o, null, columns, columnNames, Constant.AgileAbout.DIC_SPLIT);
    }

    public static <T extends Map<String, ?>> Map<String, Object> coverMapDictionary(T o, String[] columns) {
        return coverMapDictionary(o, null, columns, columns, Constant.AgileAbout.DIC_SPLIT);
    }

    /**
     * 对象转换字典
     *
     * @param o               pojo或map对象
     * @param dictionaryCodes 要转换的columns对应的字典码，其长度与columns长度应该保持一致，一一对应关系
     * @param columnNames     转换后的字典值存放到结果集map中的key值
     * @param columns         要转换的pojo属性名或map的key值数组
     * @param <T>             泛型
     * @return 转换后的Map结果数据
     */
    public static <T extends Map<String, ?>> Map<String, Object> coverMapDictionary(T o,
                                                                                    String[] dictionaryCodes,
                                                                                    String[] columns,
                                                                                    String[] columnNames,
                                                                                    String splitChar) {
        Map<String, Object> map = ObjectUtil.to(o, new TypeReference<Map<String, Object>>() {
        });
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            Object value = map.get(column);
            if (value == null) {
                continue;
            }
            if (dictionaryCodes != null && i < dictionaryCodes.length && dictionaryCodes[i] != null) {
                map.put(columnNames[i], ConvertDicName.coverDicName(dictionaryCodes[i] + splitChar + value));
            } else {
                map.put(columnNames[i], ConvertDicName.coverDicName(value.toString()));
            }
        }

        return map;
    }

    public static <T extends Map<String, ?>> List<Map<String, Object>> coverMapDictionary(List<T> o, String[] columns, String[] columnNames, String splitChar, DirectionType[] directionTypes) {
        return coverMapDictionary(o, null, columns, columnNames, splitChar);
    }

    public static <T extends Map<String, ?>> List<Map<String, Object>> coverMapDictionary(List<T> o, String[] columns, String[] columnNames, DirectionType[] directionTypes) {
        return coverMapDictionary(o, null, columns, columnNames, Constant.AgileAbout.DIC_SPLIT);
    }

    public static <T extends Map<String, ?>> List<Map<String, Object>> coverMapDictionary(List<T> o, String[] columns, DirectionType[] directionTypes) {
        return coverMapDictionary(o, null, columns, columns, Constant.AgileAbout.DIC_SPLIT);
    }


    public static <T extends Map<String, ?>> List<Map<String, Object>> coverMapDictionary(List<T> list,
                                                                                          String[] dictionaryCodes,
                                                                                          String[] columns,
                                                                                          String[] columnNames,
                                                                                          String splitChar) {
        List<Map<String, Object>> result = new ArrayList<>(list.size());
        for (T o : list) {
            result.add(coverMapDictionary(o,
                    dictionaryCodes,
                    columns,
                    columnNames,
                    splitChar)
            );
        }
        return result;
    }

    public static <T extends Map<String, ?>> List<Map<String, Object>> coverMapDictionary(List<T> list, ConvertConf... convertConfigs) {
        List<Map<String, Object>> result = new ArrayList<>(list.size());
        for (T o : list) {
            result.add(coverMapDictionary(o, convertConfigs));
        }
        return result;
    }

    public static <T extends Map<String, ?>> Map<String, Object> coverMapDictionary(T o, Collection<? extends ConvertConf> convertConfigs) {
        Map<String, Object> map = ObjectUtil.to(o, new TypeReference<Map<String, Object>>() {
        });
        for (ConvertConf convertConf : convertConfigs) {
            String value = (String) map.get(convertConf.getRef());
            map.put(convertConf.getToRef(), convertConf.parseString(value));
        }

        return map;
    }

    public static <T extends Map<String, ?>> Map<String, Object> coverMapDictionary(T o, ConvertConf... convertConfigs) {
        return coverMapDictionary(o, Arrays.stream(convertConfigs).collect(Collectors.toList()));
    }
}
