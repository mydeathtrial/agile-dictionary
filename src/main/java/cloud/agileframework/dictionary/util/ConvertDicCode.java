package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.string.StringUtil;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryEngine;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static cloud.agileframework.dictionary.DictionaryEngine.DEFAULT_SPLIT_CHAR;

/**
 * @author 佟盟
 * 日期 2021-03-24 19:43
 * 描述 转换字典码
 * @version 1.0
 * @since 1.0
 */
class ConvertDicCode extends ConvertDicBean {
    /**
     * 编码转字典编码
     *
     * @param fullNames 全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码集合
     * @return 字典码
     */
    public static String coverDicCode(String fullNames) {
        return coverDicCode(DictionaryEngine.DICTIONARY_DATA_CACHE,
                fullNames,
                DEFAULT_NAME,
                false,
                Constant.RegularAbout.SPOT);
    }

    /**
     * 编码转字典码
     *
     * @param defaultCode 未找到字典时默认返回值
     * @param fullNames   全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码集
     * @return 字典码
     */
    public static String coverDicCode(String fullNames, String defaultCode) {
        return coverDicCode(DictionaryEngine.DICTIONARY_DATA_CACHE,
                fullNames,
                defaultCode,
                false,
                Constant.RegularAbout.SPOT);
    }

    /**
     * 编码转字典码
     *
     * @param datasource  数据源
     * @param defaultCode 未找到字典时默认返回值
     * @param fullNames   全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码集
     * @return 字典码
     */
    public static String coverDicCode(String datasource, String fullNames, String defaultCode) {
        return coverDicCode(datasource,
                fullNames,
                defaultCode,
                false,
                Constant.RegularAbout.SPOT);
    }

    /**
     * 编码转字典码
     *
     * @param isFull      是否翻译出全字典码
     * @param defaultCode 未找到字典时默认返回值
     * @param fullNames   全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码集
     * @param splitChar   分隔符
     * @return 字典码
     */
    public static String coverDicCode(String fullNames, String defaultCode, boolean isFull, String splitChar) {
        return coverDicCode(DictionaryEngine.DICTIONARY_DATA_CACHE,
                fullNames,
                defaultCode,
                isFull,
                splitChar);
    }

    /**
     * 根据父节点code与节点名（可包含逗号）翻译code
     *
     * @param datasource 数据源
     * @param parentCode 父节点code
     * @param names      节点名（可包含逗号）
     * @return code（可包含逗号）
     */
    public static String coverDicCodeByParentCode(String datasource, String parentCode, String names) {
        return Arrays.stream(names.split(Constant.RegularAbout.COMMA))
                .map(name -> coverDicBeanByParent(datasource, parentCode, name))
                .filter(Objects::nonNull)
                .map(DictionaryDataBase::getCode)
                .collect(Collectors.joining(Constant.RegularAbout.COMMA));
    }

    /**
     * 根据父节点code与节点名（可包含逗号）翻译code
     *
     * @param parentCode 父节点code
     * @param names      节点名（可包含逗号）
     * @return code（可包含逗号）
     */
    public static String coverDicCodeByParentCode(String parentCode, String names) {
        return coverDicCodeByParentCode(DictionaryEngine.DICTIONARY_DATA_CACHE, parentCode, names);
    }

    /**
     * 根据父全路径字典值，与子字典值集，转子字典码集
     *
     * @param parentName 父全路径字典值
     * @param names      子字典值集合，逗号分隔
     * @return 非全路径子字典码集合，逗号分隔
     */
    public static String coverDicCodeByParent(String parentName, String names) {
        return coverDicCodeByParent(parentName, names, DEFAULT_NAME, false, Constant.RegularAbout.SPOT);
    }

    /**
     * 根据父级字典值与子字典值(多，逗号分隔)，转换字典码
     *
     * @param parentName  父全路径字典值
     * @param names       子字典值集合，逗号分隔
     * @param defaultCode 默认值
     * @return 非全路径子字典码集合，逗号分隔
     */
    public static String coverDicCodeByParent(String parentName, String names, String defaultCode) {
        return coverDicCodeByParent(parentName, names, defaultCode, false, Constant.RegularAbout.SPOT);
    }

    /**
     * 根据父级字典值与子字典值(多，逗号分隔)，转换字典码
     *
     * @param parentName  父全路径字典值
     * @param names       子字典值集合，逗号分隔
     * @param defaultCode 默认值
     * @param isFull      是否翻译出全字典码
     * @param splitChar   分隔符
     * @return 非全路径子字典码集合，逗号分隔
     */
    public static String coverDicCodeByParent(String parentName, String names, String defaultCode, boolean isFull, String splitChar) {
        return coverDicCodeByParent(DictionaryEngine.DICTIONARY_DATA_CACHE,
                parentName,
                names,
                defaultCode,
                isFull,
                splitChar);
    }

    /**
     * 根据父级字典值与子字典值(多，逗号分隔)，转换字典码
     *
     * @param parentName  父全路径字典值
     * @param names       子字典值集合，逗号分隔
     * @param defaultCode 默认值
     * @param isFull      是否全路径模式翻译
     * @return 逗号分隔字典码
     */
    public static String coverDicCodeByParent(String datasource, String parentName, String names, String defaultCode, boolean isFull, String splitChar) {
        if (StringUtils.isBlank(parentName) || StringUtils.isBlank(names)) {
            return defaultCode;
        }

        DictionaryDataBase parent = getDictionary(datasource,
                parentName,
                DictionaryEngine.CacheType.NAME_CACHE,
                NOT_FOUND_DICTIONARY_OF_FULL_NAME,
                splitChar);

        if (parent == null) {
            return defaultCode;
        }
        if (isFull) {
            defaultCode = parent.getFullName() + splitChar + defaultCode;
        }
        String finalDefaultCode = defaultCode;
        return Arrays.stream(names.split(Constant.RegularAbout.COMMA))
                .map(code -> coverDicCode(datasource,
                        parent.getFullName() + splitChar + code,
                        finalDefaultCode,
                        isFull,
                        splitChar))
                .collect(Collectors.joining(Constant.RegularAbout.COMMA));
    }

    /**
     * 编码转字典值
     *
     * @param datasource  数据源
     * @param defaultCode 默认字典吗
     * @param splitChar   分隔符
     * @param fulNames    全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码
     * @param isFull      true 全路径名，false 字典值
     * @return 字典码
     */
    public static String coverDicCode(String datasource, String fulNames, String defaultCode, boolean isFull, String splitChar) {
        if (fulNames == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Arrays.stream(fulNames.split(Constant.RegularAbout.COMMA)).forEach(c -> {
            DictionaryDataBase targetEntity = coverDicBeanByFullName(datasource, c, splitChar);
            if (builder.length() > 0) {
                builder.append(Constant.RegularAbout.COMMA);
            }
            if (targetEntity == null) {
                if (DEFAULT_NAME.equals(defaultCode)) {
                    builder.append(StringUtil.getSplitByStrLastAtomic(c, splitChar));
                } else if (defaultCode != null) {
                    builder.append(defaultCode);
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
}
