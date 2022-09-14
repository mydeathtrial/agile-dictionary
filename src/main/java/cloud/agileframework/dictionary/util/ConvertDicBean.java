package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryEngine;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2021-03-24 19:00
 * 描述 转换出字典对象
 * @version 1.0
 * @since 1.0
 */
public class ConvertDicBean extends ConvertBase {

    public static final String NOT_FOUND_DICTIONARY_OF_FULL_NAME = "not found dictionary of fullName %s";
    public static final String NOT_FOUND_DICTIONARY_OF_FULL_CODE = "not found dictionary of fullCode %s";

    /**
     * 转换字典对象
     *
     * @param fullCode 全路径字典码
     * @return bean
     */
    public static DictionaryDataBase coverDicBean(String fullCode) {
        return getDictionary(Constant.AgileAbout.DIC_DATASOURCE,
                fullCode,
                DictionaryEngine.CacheType.CODE_CACHE,
                NOT_FOUND_DICTIONARY_OF_FULL_CODE,
                Constant.AgileAbout.DIC_SPLIT);
    }

    /**
     * 转换字典对象
     *
     * @param fullCode  全路径字典码
     * @param splitChar 自定义分隔符
     * @return bean
     */
    public static DictionaryDataBase coverDicBean(String fullCode, String splitChar) {
        return getDictionary(Constant.AgileAbout.DIC_DATASOURCE,
                fullCode,
                DictionaryEngine.CacheType.CODE_CACHE,
                NOT_FOUND_DICTIONARY_OF_FULL_CODE,
                splitChar);
    }

    /**
     * 转换字典对象
     *
     * @param datasource 数据源
     * @param fullCode   全路径字典码
     * @param splitChar  自定义分隔符
     * @return bean
     */
    public static DictionaryDataBase coverDicBean(String datasource, String fullCode, String splitChar) {
        return getDictionary(datasource,
                fullCode,
                DictionaryEngine.CacheType.CODE_CACHE,
                NOT_FOUND_DICTIONARY_OF_FULL_CODE,
                splitChar);
    }


    /**
     * 转换字典对象
     *
     * @param fullName 全路径字典值
     * @return bean
     */
    public static DictionaryDataBase coverDicBeanByFullName(String fullName) {
        return getDictionary(Constant.AgileAbout.DIC_DATASOURCE,
                fullName,
                DictionaryEngine.CacheType.NAME_CACHE,
                NOT_FOUND_DICTIONARY_OF_FULL_NAME,
                Constant.AgileAbout.DIC_SPLIT);
    }


    /**
     * 转换字典对象
     *
     * @param fullName  全路径字典值
     * @param splitChar 自定义分隔符
     * @return bean
     */
    public static DictionaryDataBase coverDicBeanByFullName(String fullName, String splitChar) {
        return getDictionary(Constant.AgileAbout.DIC_DATASOURCE,
                fullName,
                DictionaryEngine.CacheType.NAME_CACHE,
                NOT_FOUND_DICTIONARY_OF_FULL_NAME,
                splitChar);
    }

    /**
     * 转换字典对象
     *
     * @param datasource 数据源
     * @param fullName   全路径字典值
     * @param splitChar  自定义分隔符
     * @return bean
     */
    public static DictionaryDataBase coverDicBeanByFullName(String datasource, String fullName, String splitChar) {
        return getDictionary(datasource,
                fullName,
                DictionaryEngine.CacheType.NAME_CACHE,
                NOT_FOUND_DICTIONARY_OF_FULL_NAME,
                splitChar);
    }


    public static DictionaryDataBase coverDicBeanByParent(String parentCode, String name) {
        return coverDicBeanByParent(Constant.AgileAbout.DIC_DATASOURCE,
                parentCode,
                name);
    }

    /**
     * 根据父级树形字典码与子树形name获取字典
     *
     * @param parentCode 字典
     * @param name       子字典值
     * @return 字典数据
     */
    public static DictionaryDataBase coverDicBeanByParent(String datasource, String parentCode, String name) {
        if (StringUtils.isEmpty(parentCode) || StringUtils.isEmpty(name)) {
            return null;
        }
        DictionaryDataBase dic = coverDicBean(datasource, parentCode, Constant.AgileAbout.DIC_SPLIT);
        if (dic == null) {
            return null;
        }

        return getDictionary(datasource,
                dic.getFullName() + Constant.AgileAbout.DIC_SPLIT + name,
                DictionaryEngine.CacheType.NAME_CACHE,
                NOT_FOUND_DICTIONARY_OF_FULL_NAME,
                Constant.AgileAbout.DIC_SPLIT);
    }

    /**
     * 处理当期望转换失败时，翻译为空值的情况
     *
     * @param value 值
     * @return 翻译结果值
     */
    protected static String parseNullValue(String value) {
        if (value == null) {
            return null;
        }
        if (!value.contains(Constant.AgileAbout.DIC_TRANSLATE_FAIL_NULL_VALUE)) {
            return value;
        }
        if (!value.contains(Constant.RegularAbout.COMMA)) {
            return null;
        } else {
            String[] array = StringUtils.split(value, Constant.RegularAbout.COMMA);
            boolean allNull = Arrays.stream(array).allMatch(Constant.AgileAbout.DIC_TRANSLATE_FAIL_NULL_VALUE::equals);

            if (allNull) {
                return null;
            }
            return Arrays.stream(array).map(node -> {
                if (Constant.AgileAbout.DIC_TRANSLATE_FAIL_NULL_VALUE.equals(node)) {
                    return Constant.RegularAbout.BLANK;
                }
                return node;
            }).collect(Collectors.joining(Constant.RegularAbout.COMMA));
        }
    }
}
