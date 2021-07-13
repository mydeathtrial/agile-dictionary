package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.common.util.string.StringUtil;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryEngine;
import cloud.agileframework.dictionary.annotation.Dictionary;
import cloud.agileframework.dictionary.annotation.DictionaryField;
import cloud.agileframework.dictionary.annotation.DirectionType;
import com.google.common.collect.Maps;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2021-03-24 19:49
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
class ConvertDicAnnotation extends ConvertDicMap {
    private static Map<String, String> dicCoverCache;

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
        if (Collection.class.isAssignableFrom(o.getClass())) {
            cover((Collection<?>) o);
        }
        Set<ClassUtil.Target<Dictionary>> targets = ClassUtil.getAllEntityAnnotation(o.getClass(), Dictionary.class);
        targets.forEach(target -> {
            Dictionary dictionary = target.getAnnotation();
            Member member = target.getMember();
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
            parseNodeField(dictionary, field, o);
        });

        Set<ClassUtil.Target<DictionaryField>> dictionaryTargets = ClassUtil.getAllEntityAnnotation(o.getClass(), DictionaryField.class);
        dictionaryTargets.forEach(dictionaryTarget -> {
            Member member = dictionaryTarget.getMember();
            String fieldName;
            if (member instanceof Method) {
                fieldName = StringUtil.toLowerName(member.getName().substring(Constant.NumberAbout.THREE));
            } else if (member instanceof Field) {
                fieldName = member.getName();
            } else {
                return;
            }
            Object value = ObjectUtil.getFieldValue(o, fieldName);
            cover(value);
        });
    }

    /**
     * 字典自动转换，针对Dictionary注解进行解析
     *
     * @param collection 目标数据集
     * @param <T>        泛型
     */
    public static <T> void cover(Collection<T> collection) {
        if (ObjectUtils.isEmpty(collection)) {
            return;
        }
        if (dicCoverCache == null) {
            dicCoverCache = Maps.newConcurrentMap();
        }
        Collection<T> c = Collections.synchronizedCollection(collection);
        synchronized (c) {
            c.parallelStream().forEach(ConvertDicAnnotation::cover);
        }
        dicCoverCache.clear();
    }

    /**
     * 翻译一个对象的一个字段
     *
     * @param dictionary 字典注解
     * @param field      翻译后存储字典值的字段
     * @param node       对象
     * @param <T>        对象类型
     */
    private static <T> void parseNodeField(Dictionary dictionary, Field field, T node) {
        //取映射字段
        final String[] fieldNames = dictionary.fieldName();
        if (fieldNames.length == 0) {
            return;
        }

        //当前要处理的字段
        String currentFieldName = fieldNames.length > 1 ? fieldNames[fieldNames.length - 1] : fieldNames[0];
        Object fieldValue = ObjectUtil.getFieldValue(node, currentFieldName);
        if (fieldValue == null) {
            return;
        }

        // 处理字典前缀
        String parentKey = dictionary.dicCode();
        String split = dictionary.split();
        String prefix = ObjectUtils.isEmpty(parentKey) ? "" : parentKey + split;
        // 如果是级联字典
        if (fieldNames.length > 1) {
            String prefix2 = Arrays.stream(fieldNames)
                    .limit(fieldNames.length - 1L)
                    .map(column -> {
                        Object prefixFieldValue = ObjectUtil.getFieldValue(node, column);
                        return toDitKey(prefixFieldValue);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(dictionary.split()));
            prefix = prefix + prefix2 + split;
        }

        Object value;
        String finalPrefix = prefix;
        if (fieldValue.getClass().isArray() || Collection.class.isAssignableFrom(fieldValue.getClass())) {
            List<Object> temp = ObjectUtil.to(fieldValue, new TypeReference<List<Object>>() {
            });

            List<String> tempList = temp.stream().map(ConvertDicAnnotation::toDitKey).map(a -> finalPrefix + a).collect(Collectors.toList());
            value = parseCollection(dictionary, tempList, new TypeReference<>(field.getGenericType()));
        } else {
            final String temp = toDitKey(fieldValue);
            if (temp == null) {
                return;
            }
            String tempString = Arrays.stream(temp.split("[,]")).map(a -> finalPrefix + a).collect(Collectors.joining(Constant.RegularAbout.COMMA));
            value = parseString(dictionary, tempString);
        }

        value = ObjectUtil.to(value, new TypeReference<>(field.getGenericType()));

        // 赋值
        ObjectUtil.setValue(node, field, value);
    }

    /**
     * 处理不同类型值换成字典可以识别的值
     *
     * @param source 要转变的值
     * @return 可以翻译的字符串类型值
     */
    private static String toDitKey(Object source) {
        String target;
        // 处理布尔类型
        if (ObjectUtils.isEmpty(source)) {
            return null;
        } else if (source instanceof String) {
            target = (String) source;
        } else if (source instanceof Boolean) {
            target = Boolean.TRUE.equals(source) ? "1" : "0";
        } else if (source.getClass().isEnum()) {
            target = ((Enum<?>) source).name();
        } else {
            target = ObjectUtil.toString(source);
        }
        return target;
    }

    /**
     * 处理集合类型
     *
     * @param dictionary    字典注解
     * @param fullIndexes   要翻译的集合
     * @param typeReference 要转换的类型
     * @param <A>           泛型
     * @return 转换后的数据
     */
    private static <A> A parseCollection(Dictionary dictionary, List<String> fullIndexes, TypeReference<A> typeReference) {
        List<String> targetNameList = fullIndexes.stream().map(fullIndex -> parseString(dictionary, fullIndex)).collect(Collectors.toList());
        return ObjectUtil.to(targetNameList, typeReference);
    }

    /**
     * 翻译字符串类型
     *
     * @param dictionary 字典注解
     * @param fullIndex  要翻译的字符串类型值
     * @return 翻译后的字符串
     */
    private static String parseString(Dictionary dictionary, String fullIndex) {
        boolean isFull = dictionary.isFull();
        String split = dictionary.split();
        // 翻译后值
        String targetName;
        final String threadCacheKey = fullIndex + dictionary.hashCode();
        if (dicCoverCache != null && dicCoverCache.containsKey(threadCacheKey)) {
            targetName = dicCoverCache.get(threadCacheKey);
        } else {
            if (dictionary.directionType() == DirectionType.CODE_TO_NAME && !dictionary.id()) {
                String defaultValue = dictionary.defaultValue();
                defaultValue = Dictionary.NULL.equals(defaultValue) ? DEFAULT_NAME : null;
                targetName = ConvertDicName.coverDicName(dictionary.dataSource(), fullIndex, defaultValue, isFull, split);
            } else if (dictionary.directionType() == DirectionType.CODE_TO_NAME) {
                targetName = Arrays.stream(fullIndex.split(Constant.RegularAbout.COMMA))
                        .map(id -> {
                            DictionaryDataBase dic = DictionaryUtil.findById(dictionary.dataSource(), id);
                            String defaultValue = dictionary.defaultValue();
                            defaultValue = DEFAULT_NAME.equals(defaultValue) ? id : defaultValue;

                            String result = defaultValue;
                            if (dic == null) {
                                return result;
                            }
                            if (isFull) {
                                result = dic.getFullName().replace(DictionaryEngine.DEFAULT_SPLIT_CHAR, dictionary.split());
                            } else {
                                result = dic.getName();
                            }
                            return result;
                        }).collect(Collectors.joining(Constant.RegularAbout.COMMA));
            } else if (dictionary.directionType() == DirectionType.NAME_TO_CODE && !dictionary.id()) {
                String defaultValue = dictionary.defaultValue();
                defaultValue = Dictionary.NULL.equals(defaultValue) ? DEFAULT_NAME : null;
                targetName = ConvertDicCode.coverDicCode(dictionary.dataSource(), fullIndex, defaultValue, isFull, split);
            } else {
                targetName = Arrays.stream(fullIndex.split(Constant.RegularAbout.COMMA))
                        .map(id -> {
                            DictionaryDataBase dic = DictionaryUtil.findById(dictionary.dataSource(), id);
                            String defaultValue = dictionary.defaultValue();
                            defaultValue = DEFAULT_NAME.equals(defaultValue) ? id : defaultValue;
                            return dic == null ? defaultValue : dic.getCode();
                        }).collect(Collectors.joining(Constant.RegularAbout.COMMA));
            }
            if (dicCoverCache != null) {
                dicCoverCache.put(threadCacheKey, targetName);
            }
        }
        return targetName;
    }
}
