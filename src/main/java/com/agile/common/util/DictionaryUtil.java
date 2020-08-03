package com.agile.common.util;

import com.agile.common.annotation.Dictionary;
import com.agile.common.cache.AgileCache;
import com.agile.common.dictionary.Constant;
import com.agile.common.dictionary.DictionaryData;
import com.agile.common.dictionary.DictionaryEngine;
import com.agile.common.util.clazz.ClassUtil;
import com.agile.common.util.clazz.TypeReference;
import com.agile.common.util.object.ObjectUtil;
import com.agile.common.util.string.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.agile.common.dictionary.DictionaryEngine.DEFAULT_SPLIT_CHAR;

/**
 * @author 佟盟
 * @version 1.0SPLIT_CHAR
 * 日期 2019/3/6 15:48
 * 描述 字典工具
 * @since 1.0
 */
public final class DictionaryUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryUtil.class);
    private static final String DEFAULT_CACHE_NAME = "dictionary-cache";
    private static final String CODE_FORMAT = "%s.%s";
    private static final String DEFAULT_NAME = "$DEFAULT_NAME";

    private DictionaryUtil() {
    }

    public static AgileCache getCache() {
        return CacheUtil.getCache(DEFAULT_CACHE_NAME);
    }

    /**
     * 转换字典对象
     *
     * @param fullCode 全路径字典码
     * @return bean
     */
    public static DictionaryData coverDicBean(String fullCode) {
        return getDictionary(fullCode, DictionaryEngine.CODE_CACHE, "not found dictionary of fullCode {}", Constant.RegularAbout.SPOT);
    }

    /**
     * 转换字典对象
     *
     * @param fullCode  全路径字典码
     * @param splitChar 自定义分隔符
     * @return bean
     */
    public static DictionaryData coverDicBean(String fullCode, String splitChar) {
        return getDictionary(fullCode, DictionaryEngine.CODE_CACHE, "not found dictionary of fullCode {}", splitChar);
    }

    /**
     * 转换字典对象
     *
     * @param fullName 全路径字典值
     * @return bean
     */
    public static DictionaryData coverDicBeanByFullName(String fullName) {
        return getDictionary(fullName, DictionaryEngine.NAME_CACHE, "not found dictionary of fullName {}", Constant.RegularAbout.SPOT);
    }


    /**
     * 转换字典对象
     *
     * @param fullName  全路径字典值
     * @param splitChar 自定义分隔符
     * @return bean
     */
    public static DictionaryData coverDicBeanByFullName(String fullName, String splitChar) {
        return getDictionary(fullName, DictionaryEngine.NAME_CACHE, "not found dictionary of fullName {}", splitChar);
    }

    /**
     * 根据父级树形字典码与子树形name获取字典
     *
     * @param parentCode 字典
     * @param name       子字典值
     * @return 字典数据
     */
    public static DictionaryData coverDicBeanByParent(String parentCode, String name) {
        if (StringUtils.isEmpty(parentCode) || StringUtils.isEmpty(name)) {
            return null;
        }
        DictionaryData dic = coverDicBean(parentCode);
        if (dic == null) {
            return null;
        }

        return coverDicBeanByFullName(dic.getFullName() + Constant.RegularAbout.SPOT + name);
    }

    /**
     * 根据全路径字典值或字典码，查找缓存中的字典数据
     *
     * @param fullIndex    全路径字典值或字典码
     * @param cacheKey     缓存key值
     * @param errorMessage 日志
     * @return 字典数据
     */
    private static DictionaryData getDictionary(String fullIndex, String cacheKey, String errorMessage, String splitChar) {
        if (StringUtils.isEmpty(fullIndex)) {
            return null;
        }
        // 字典分隔符同义转换为点号
        fullIndex = fullIndex.replace(splitChar, DEFAULT_SPLIT_CHAR);

        // 直接从缓存中查找对应字典码
        DictionaryData entity = getCache().getFromMap(cacheKey, fullIndex, DictionaryData.class);
        if (entity == null) {
            LOGGER.error(errorMessage, fullIndex);
            return null;
        }
        return entity;
    }

    /**
     * 编码转字典值
     *
     * @param fullCodes 全路径字典码，支持包含逗号分隔的多全路径字典码，转换结果为逗号分隔非全路径字典值
     * @return 字典值
     */
    public static String coverDicName(String fullCodes) {
        return coverDicName(fullCodes, DEFAULT_NAME, false, Constant.RegularAbout.SPOT);
    }

    /**
     * 编码转字典值
     *
     * @param fullCodes   全路径字典码，支持包含逗号分隔的多全路径字典码，转换结果为逗号分隔非全路径字典值
     * @param defaultName 当未找到对应字典时，默认翻译结果
     * @return 字典值
     */
    public static String coverDicName(String fullCodes, String defaultName) {
        return coverDicName(fullCodes, defaultName, false, Constant.RegularAbout.SPOT);
    }


    /**
     * 根据父全路径字典码，与子字典码集，转子字典值集
     *
     * @param parentCode 父全路径字典码
     * @param codes      子字典码集合，逗号分隔
     * @return 非全路径子字典值集合，逗号分隔
     */
    public static String coverDicNameByParent(String parentCode, String codes) {
        return coverDicNameByParent(parentCode, codes, null, false, Constant.RegularAbout.SPOT);
    }

    /**
     * 根据父级字典与子字典(多，逗号分隔)，转换字典值
     *
     * @param parentCode   父全路径字典码
     * @param codes        子字典码集合，逗号分隔
     * @param defaultValue 默认值
     * @return 非全路径子字典值集合，逗号分隔
     */
    public static String coverDicNameByParent(String parentCode, String codes, String defaultValue) {
        return coverDicNameByParent(parentCode, codes, defaultValue, false, Constant.RegularAbout.SPOT);
    }

    /**
     * 根据父级字典与子字典(多，逗号分隔)，转换字典值
     *
     * @param parentCode   父全路径字典码
     * @param codes        子字典码集合，逗号分隔
     * @param defaultValue 默认值
     * @param isFull       是否全路径模式翻译
     * @param splitChar    自定义分隔符
     * @return 逗号分隔字典值
     */
    public static String coverDicNameByParent(String parentCode, String codes, String defaultValue, boolean isFull, String splitChar) {
        if (StringUtils.isBlank(parentCode) || StringUtils.isBlank(codes)) {
            return defaultValue;
        }

        codes = parentCode + splitChar + codes;
        codes = codes.replace(Constant.RegularAbout.COMMA, Constant.RegularAbout.COMMA + parentCode + Constant.RegularAbout.SPOT);
        return coverDicName(codes, defaultValue, isFull, splitChar);
    }


    /**
     * 编码转字典值
     *
     * @param fullCodes   全路径字典码，支持包含逗号分隔的多全路径字典码，转换结果为逗号分隔非全路径字典值
     * @param defaultName 未找到字典时默认返回值
     * @param isFull      true 全路径名，false 字典值
     * @param splitChar   自定义分隔符
     * @return 字典值
     */
    public static String coverDicName(String fullCodes, String defaultName, boolean isFull, String splitChar) {
        if (fullCodes == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Arrays.stream(fullCodes.split(Constant.RegularAbout.COMMA)).forEach(c -> {
            DictionaryData targetEntity = coverDicBean(c);
            if (builder.length() > 0) {
                builder.append(Constant.RegularAbout.COMMA);
            }
            if (targetEntity == null) {
                if (DEFAULT_NAME.equals(defaultName)) {
                    builder.append(StringUtil.getSplitLastAtomic(c, splitChar));
                } else if (defaultName != null) {
                    builder.append(defaultName);
                }
            } else {
                if (isFull) {
                    builder.append(targetEntity.getFullName().replace(DEFAULT_SPLIT_CHAR, splitChar));
                } else {
                    builder.append(targetEntity.getName().replace(DEFAULT_SPLIT_CHAR, splitChar));
                }
            }
        });
        return builder.toString();
    }


    /**
     * 编码转字典编码
     *
     * @param fullNames 全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码集合
     * @return 字典码
     */
    public static String coverDicCode(String fullNames) {
        return coverDicCode(fullNames, DEFAULT_NAME, false, Constant.RegularAbout.SPOT);
    }

    /**
     * 编码转字典码
     * @param defaultCode 未找到字典时默认返回值
     * @param fullNames 全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码集
     * @return 字典码
     */
    public static String coverDicCode(String fullNames, String defaultCode) {
        return coverDicCode(fullNames, defaultCode, false, Constant.RegularAbout.SPOT);
    }


    /**
     * 根据父全路径字典值，与子字典值集，转子字典码集
     *
     * @param parentName 父全路径字典值
     * @param names      子字典值集合，逗号分隔
     * @return 非全路径子字典码集合，逗号分隔
     */
    public static String coverDicCodeByParent(String parentName, String names) {
        return coverDicCodeByParent(parentName, names, null, false, Constant.RegularAbout.SPOT);
    }

    /**
     * 根据父级字典值与子字典值(多，逗号分隔)，转换字典码
     *
     * @param parentName   父全路径字典值
     * @param names        子字典值集合，逗号分隔
     * @param defaultValue 默认值
     * @return 非全路径子字典码集合，逗号分隔
     */
    public static String coverDicCodeByParent(String parentName, String names, String defaultValue) {
        return coverDicCodeByParent(parentName, names, defaultValue, false, Constant.RegularAbout.SPOT);
    }

    /**
     * 根据父级字典值与子字典值(多，逗号分隔)，转换字典码
     *
     * @param parentName   父全路径字典值
     * @param names        子字典值集合，逗号分隔
     * @param defaultValue 默认值
     * @param isFull       是否全路径模式翻译
     * @return 逗号分隔字典码
     */
    public static String coverDicCodeByParent(String parentName, String names, String defaultValue, boolean isFull, String splitChar) {
        if (StringUtils.isBlank(parentName) || StringUtils.isBlank(names)) {
            return defaultValue;
        }

        names = parentName + splitChar + names;
        names = names.replace(Constant.RegularAbout.COMMA, Constant.RegularAbout.COMMA + parentName + splitChar);
        return coverDicCode(names, defaultValue, isFull, splitChar);
    }


    /**
     * 编码转字典值
     *
     * @param fulNames 全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码
     * @param isFull   true 全路径名，false 字典值
     * @return 字典码
     */
    public static String coverDicCode(String fulNames, String defaultName, boolean isFull, String splitChar) {
        if (fulNames == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Arrays.stream(fulNames.split(Constant.RegularAbout.COMMA)).forEach(c -> {
            DictionaryData targetEntity = coverDicBeanByFullName(c, splitChar);
            if (builder.length() > 0) {
                builder.append(Constant.RegularAbout.COMMA);
            }
            if (targetEntity == null) {
                if (DEFAULT_NAME.equals(defaultName)) {
                    builder.append(StringUtil.getSplitLastAtomic(c, splitChar));
                } else if (defaultName != null) {
                    builder.append(defaultName);
                }
            } else {
                if (isFull) {
                    builder.append(targetEntity.getFullCode().replace(DEFAULT_SPLIT_CHAR, splitChar));
                } else {
                    builder.append(targetEntity.getCode().replace(DEFAULT_SPLIT_CHAR, splitChar));
                }
            }
        });
        return builder.toString();
    }

    //---------------------------------------------------------------------------------

    /**
     * 集合类型转换字典码工具，转换为List/Map类型
     *
     * @param list           要进行转换的集合
     * @param dictionaryCode 要使用的字典码
     * @param suffix         转换出来的字典值存储的字段后缀
     * @param column         转换字段集
     * @param <T>            泛型
     * @return 返回List/Map类型，增加_text字段
     * @throws NoSuchFieldException   没有这个字段
     * @throws IllegalAccessException 非法访问
     */
    public static <T> List<Map<String, Object>> coverMapDictionary(List<T> list, String dictionaryCode, String suffix, String column) throws NoSuchFieldException, IllegalAccessException {
        return coverMapDictionary(list, new String[]{dictionaryCode}, suffix, new String[]{column});
    }

    public static <T> List<Map<String, Object>> coverMapDictionary(List<T> list, String[] dictionaryCodes, String suffix, String[] columns) throws NoSuchFieldException, IllegalAccessException {
        if (dictionaryCodes == null || columns == null || dictionaryCodes.length != columns.length) {
            throw new IllegalArgumentException("dictionaryCodes and columns should be the same length");
        }
        List<Map<String, Object>> result = new ArrayList<>(list.size());
        for (T o : list) {
            result.add(coverMapDictionary(o, dictionaryCodes, suffix, columns));
        }
        return result;
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
     * @throws NoSuchFieldException   异常
     * @throws IllegalAccessException 异常
     */
    public static <T> Map<String, Object> coverMapDictionary(T o, String[] dictionaryCodes, String suffix, String[] columns) throws NoSuchFieldException, IllegalAccessException {
        Map<String, Field> cache;
        Class<?> clazz = o.getClass();
        if (Map.class.isAssignableFrom(clazz)) {
            Map<String, Object> map = (Map<String, Object>) o;
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                map.put(column + suffix, coverDicName(String.format(CODE_FORMAT, dictionaryCodes[i], map.get(column))));
            }
            return map;
        } else {
            cache = initField(clazz, columns);
            Map<String, Object> map = ObjectUtil.to(o, new TypeReference<Map<String, Object>>() {
            });
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                Field field = cache.get(column);
                Object value = field.get(o);
                map.put(column + suffix, coverDicName(String.format(CODE_FORMAT, dictionaryCodes[i], value)));
            }
            return map;
        }

    }

    public static <T> List<T> coverBeanDictionary(List<T> list, String dictionaryCode, String column, String textColumn) throws IllegalAccessException, NoSuchFieldException {
        return coverBeanDictionary(list, new String[]{dictionaryCode}, new String[]{column}, new String[]{textColumn}, null);
    }

    public static <T> T coverBeanDictionary(T o, String[] dictionaryCodes, String[] columns, String[] textColumns) throws IllegalAccessException, NoSuchFieldException {
        return coverBeanDictionary(o, dictionaryCodes, columns, textColumns, null);
    }

    public static <T> T coverBeanDictionary(T o, String[] dictionaryCodes, String[] columns, String[] textColumns, String[] defaultValues) throws IllegalAccessException, NoSuchFieldException {
        Map<String, Field> cache;
        Class<?> clazz = o.getClass();
        if (Map.class.isAssignableFrom(clazz)) {
            Map<String, Object> map = (Map<String, Object>) o;
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                String textColumn = textColumns[i];
                if (defaultValues == null || defaultValues.length <= i) {
                    map.put(textColumn, coverDicName(dictionaryCodes[i], String.valueOf(map.get(column))));
                } else {
                    String defaultValue = defaultValues[i];
                    map.put(textColumn, coverDicNameByParent(dictionaryCodes[i], String.valueOf(map.get(column)), defaultValue));
                }

            }
            return (T) map;
        } else {
            cache = initField(clazz, columns);
            cache.putAll(initField(clazz, textColumns));

            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                String textColumn = textColumns[i];
                Field field = cache.get(column);
                Field textField = cache.get(textColumn);
                Object value = field.get(o);
                if (defaultValues == null || defaultValues.length <= i) {
                    textField.set(o, coverDicNameByParent(dictionaryCodes[i], String.valueOf(value)));
                } else {
                    String defaultValue = defaultValues[i];
                    textField.set(o, coverDicNameByParent(dictionaryCodes[i], String.valueOf(value), defaultValue));
                }

            }
            return o;
        }
    }

    /**
     * 集合类型转换字典码工具，转换为List/T类型
     *
     * @param list            要进行转换的集合
     * @param dictionaryCodes 要使用的字典码
     * @param columns         转换字段集
     * @param <T>             泛型
     * @return 返回List/Map类型，字典码字段自动被转换为字典值
     */
    public static <T> List<T> coverBeanDictionary(List<T> list, String[] dictionaryCodes, String[] columns, String[] textColumns, String[] defaultValues) throws IllegalAccessException, NoSuchFieldException {
        List<T> result = new ArrayList<>(list.size());
        for (T o : list) {
            result.add(coverBeanDictionary(o, dictionaryCodes, columns, textColumns, defaultValues));
        }
        return result;
    }

    /**
     * 初始化转换字段集合
     *
     * @param clazz   类型
     * @param columns 字段名集合
     */
    private static Map<String, Field> initField(Class<?> clazz, String... columns) throws NoSuchFieldException {
        Map<String, Field> cache = new HashMap<>(columns.length);
        for (String column : columns) {
            if (!cache.containsKey(column)) {
                Field field = clazz.getDeclaredField(column);
                field.setAccessible(true);
                cache.put(column, field);
            }
        }
        return cache;
    }


    /**
     * 字典自动转换，针对Dictionary注解进行解析
     *
     * @param o   目标数据
     * @param <T> 泛型
     */
    public static <T> void cover(T o) {
        if (ObjectUtils.isEmpty(o)) {
            return;
        }
        Set<ClassUtil.Target<Dictionary>> targets = ClassUtil.getAllEntityAnnotation(o.getClass(), Dictionary.class);
        targets.forEach(target -> {
            Dictionary dictionary = target.getAnnotation();
            Member member = target.getMember();
            String parentDicCode = dictionary.dicCode();
            String linkColumn = dictionary.fieldName();
            boolean isFull = dictionary.isFull();
            String split = dictionary.split();
            Field field;
            if (member instanceof Method) {
                String fieldName = StringUtil.toLowerName(member.getName().substring(Constant.NumberAbout.THREE));
                field = ClassUtil.getField(o.getClass(), fieldName);
                if (ObjectUtils.isEmpty(field)) {
                    return;
                }
            } else if (member instanceof Field) {
                field = (Field) member;
            } else {
                return;
            }
            parseNodeField(parentDicCode, linkColumn, isFull, split, field, o);
        });
    }

    /**
     * 字典自动转换，针对Dictionary注解进行解析
     *
     * @param list 目标数据集
     * @param <T>  泛型
     */
    public static <T> void cover(List<T> list) {
        if (ObjectUtils.isEmpty(list)) {
            return;
        }
        list.parallelStream().forEach(DictionaryUtil::cover);
    }

    /**
     * 翻译一个对象的一个字段
     *
     * @param parentDicCode 父级字典码
     * @param linkColumn    关联的存储字典码的字段
     * @param isFull        是否需要全路径字典值
     * @param field         翻译后存储字典值的字段
     * @param node          对象
     * @param <T>           对象类型
     */
    private static <T> void parseNodeField(String parentDicCode, String linkColumn, boolean isFull, String split, Field field, T node) {
        Object code = ObjectUtil.getFieldValue(node, linkColumn);

        // 处理布尔类型
        String codeStr;
        if (ObjectUtils.isEmpty(code)) {
            return;
        } else if (code instanceof Boolean) {
            codeStr = (Boolean) code ? "1" : "0";
        } else {
            codeStr = code.toString();
        }

        // 全路径字典码
        String fullCode;
        if (ObjectUtils.isEmpty(parentDicCode)) {
            fullCode = codeStr;
        } else {
            fullCode = parentDicCode + Constant.RegularAbout.SPOT + codeStr;
        }

        // 全路径字典值
        String targetName;
        if (isFull) {
            targetName = coverDicName(fullCode, DEFAULT_NAME, true, split);
        } else {
            targetName = coverDicName(fullCode);
        }

        // 赋值
        ObjectUtil.setValue(node, field, targetName);
    }
}
