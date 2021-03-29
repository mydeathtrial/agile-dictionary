package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.string.StringUtil;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryEngine;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static cloud.agileframework.dictionary.DictionaryEngine.DEFAULT_SPLIT_CHAR;

/**
 * @author 佟盟
 * 日期 2021-03-24 19:40
 * 描述 翻译出字典值
 * @version 1.0
 * @since 1.0
 */
class ConvertDicName extends ConvertDicCode {
    /**
     * 编码转字典值
     *
     * @param fullCodes 全路径字典码，支持包含逗号分隔的多全路径字典码，转换结果为逗号分隔非全路径字典值
     * @return 字典值
     */
    public static String coverDicName(String fullCodes) {
        return coverDicName(DictionaryEngine.DICTIONARY_DATA_CACHE,
                fullCodes,
                DEFAULT_NAME,
                false,
                Constant.RegularAbout.SPOT);
    }

    /**
     * 编码转字典值
     *
     * @param fullCodes   全路径字典码，支持包含逗号分隔的多全路径字典码，转换结果为逗号分隔非全路径字典值
     * @param defaultName 当未找到对应字典时，默认翻译结果
     * @return 字典值
     */
    public static String coverDicName(String fullCodes, String defaultName) {
        return coverDicName(DictionaryEngine.DICTIONARY_DATA_CACHE,
                fullCodes,
                defaultName,
                false,
                Constant.RegularAbout.SPOT);
    }

    /**
     * 编码转字典值
     *
     * @param datasource  数据源
     * @param fullCodes   全路径字典码，支持包含逗号分隔的多全路径字典码，转换结果为逗号分隔非全路径字典值
     * @param defaultName 当未找到对应字典时，默认翻译结果
     * @return 字典值
     */
    public static String coverDicName(String datasource, String fullCodes, String defaultName) {
        return coverDicName(datasource,
                fullCodes,
                defaultName,
                false,
                Constant.RegularAbout.SPOT);
    }


    /**
     * 根据父全路径字典码，与子字典码集，转子字典值集
     *
     * @param parentCode 父全路径字典码
     * @param codes      子字典码集合，逗号分隔
     * @return 非全路径子字典值集合，逗号分隔
     */
    public static String coverDicNameByParent(String parentCode, String codes) {
        return coverDicNameByParent(DictionaryEngine.DICTIONARY_DATA_CACHE,
                parentCode,
                codes,
                DEFAULT_NAME,
                false,
                Constant.RegularAbout.SPOT);
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
        return coverDicNameByParent(DictionaryEngine.DICTIONARY_DATA_CACHE,
                parentCode,
                codes,
                defaultValue,
                false,
                Constant.RegularAbout.SPOT);
    }

    public static String coverDicNameByParent(String parentCode, String codes, String defaultValue, boolean isFull, String splitChar) {
        return coverDicNameByParent(DictionaryEngine.DICTIONARY_DATA_CACHE, parentCode, codes, defaultValue, isFull, splitChar);
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
    public static String coverDicNameByParent(String datasource, String parentCode, String codes, String defaultValue, boolean isFull, String splitChar) {
        if (StringUtils.isBlank(parentCode) || StringUtils.isBlank(codes)) {
            return defaultValue;
        }

        DictionaryDataBase parent = coverDicBean(datasource, parentCode, splitChar);
        if (parent == null) {
            return defaultValue;
        }
        if (isFull) {
            defaultValue = parent.getFullName() + splitChar + defaultValue;
        }

        String finalDefaultValue = defaultValue;

        return Arrays.stream(codes.split(Constant.RegularAbout.COMMA))
                .map(code -> coverDicName(datasource, parent.getFullCode() + splitChar + code, finalDefaultValue, isFull, splitChar))
                .collect(Collectors.joining(Constant.RegularAbout.COMMA));
    }

    public static String coverDicName(String fullCodes, String defaultName, boolean isFull, String splitChar) {
        return coverDicName(DictionaryEngine.DICTIONARY_DATA_CACHE, fullCodes, defaultName, isFull, splitChar);
    }

    /**
     * 编码转字典值
     *
     * @param datasource  数据源
     * @param fullCodes   全路径字典码，支持包含逗号分隔的多全路径字典码，转换结果为逗号分隔非全路径字典值
     * @param defaultName 未找到字典时默认返回值
     * @param isFull      true 全路径名，false 字典值
     * @param splitChar   自定义分隔符
     * @return 字典值
     */
    public static String coverDicName(String datasource, String fullCodes, String defaultName, boolean isFull, String splitChar) {
        if (fullCodes == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Arrays.stream(fullCodes.split(Constant.RegularAbout.COMMA)).forEach(c -> {
            DictionaryDataBase targetEntity = coverDicBean(datasource, c, splitChar);
            if (builder.length() > 0) {
                builder.append(Constant.RegularAbout.COMMA);
            }
            if (targetEntity == null) {
                if (DEFAULT_NAME.equals(defaultName)) {
                    builder.append(StringUtil.getSplitByStrLastAtomic(c, splitChar));
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
}
