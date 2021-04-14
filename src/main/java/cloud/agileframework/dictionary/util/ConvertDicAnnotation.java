package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.common.util.string.StringUtil;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.annotation.Dictionary;
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
        //如果是id，则直接调用主键查询
        final String[] fieldNames = dictionary.fieldName();

        String parentDicCode = dictionary.dicCode();
        boolean isFull = dictionary.isFull();
        String split = dictionary.split();

        // 组装要翻译的内容
        // 处理布尔类型
        List<String> indexes = Arrays.stream(fieldNames).flatMap(column -> {
            Object index = ObjectUtil.getFieldValue(node, column);

            String value;
            // 处理布尔类型
            if (ObjectUtils.isEmpty(index)) {
                return null;
            } else if (index instanceof Boolean) {
                value = Boolean.TRUE.equals(index) ? "1" : "0";
            } else if (index.getClass().isEnum()) {
                value = ((Enum<?>) index).name();
            } else {
                value = index.toString();
            }
            return Arrays.stream(value.split("[,]"));
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (indexes.isEmpty()) {
            final String defaultValue = dictionary.defaultValue();
            if (!Dictionary.NULL.equals(defaultValue)) {
                // 赋值
                ObjectUtil.setValue(node, field, defaultValue);
            }
            return;
        }

        // 组装要翻译的内容
        String fullIndex;
        if (ObjectUtils.isEmpty(parentDicCode)) {
            fullIndex = String.join(split, indexes);
        } else {
            fullIndex = indexes.stream()
                    .map(code -> parentDicCode + split + code)
                    .collect(Collectors.joining(Constant.RegularAbout.COMMA));
        }

        // 翻译后值
        String targetName;

        if (dicCoverCache != null && dicCoverCache.containsKey(fullIndex)) {
            targetName = dicCoverCache.get(fullIndex);
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
                            return dic == null ? defaultValue : dic.getName();
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
                dicCoverCache.put(fullIndex, targetName);
            }
        }

        // 赋值
        ObjectUtil.setValue(node, field, targetName);
    }
}
