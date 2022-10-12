package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.string.StringUtil;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.annotation.DirectionType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ConvertConf {
    @Builder.Default
    private String dicCode = "";
    @Builder.Default
    private boolean isFull = false;
    @Builder.Default
    private String split = Constant.AgileAbout.DIC_SPLIT;
    @Builder.Default
    private DirectionType directionType = DirectionType.CODE_TO_NAME;
    @Builder.Default
    private String defaultValue = Constant.AgileAbout.DIC_TRANSLATE_FAIL_NULL_VALUE;
    @Builder.Default
    private String dataSource = Constant.AgileAbout.DIC_DATASOURCE;
    private String ref;
    private String toRef;
    private static Map<String, String> dicCoverCache;

    public String getToRef() {
        return toRef == null ? ref : toRef;
    }

    public String parseString(String fullIndex) {
        return ConvertConf.parseString(this, fullIndex);
    }

    public static String parseString(ConvertConf conf, String fullIndex) {
        if (conf == null || fullIndex == null) {
            return null;
        }

        boolean isFull = conf.isFull();
        String split = conf.getSplit();
        // 翻译后值
        String targetName;
        final String threadCacheKey = fullIndex + conf.hashCode();
        if (dicCoverCache != null && dicCoverCache.containsKey(threadCacheKey)) {
            targetName = dicCoverCache.get(threadCacheKey);
            if (Constant.AgileAbout.DIC_TRANSLATE_FAIL_NULL_VALUE.equals(targetName)) {
                targetName = null;
            }
        } else {
            String defaultValue = conf.getDefaultValue();
            StringBuilder builder = new StringBuilder();
            for (String c : fullIndex.split(Constant.RegularAbout.COMMA)) {
                DictionaryDataBase targetEntity = null;

                try {
                    switch (conf.getDirectionType()) {
                        case CODE_TO_NAME:
                        case CODE_TO_ID:
                            targetEntity = ConvertDicBean.coverDicBean(conf.getDataSource(), c, split);
                            break;
                        case NAME_TO_CODE:
                        case NAME_TO_ID:
                            targetEntity = ConvertDicBean.coverDicBeanByFullName(conf.getDataSource(), c, split);
                            break;
                        case ID_TO_NAME:
                        case ID_TO_CODE:
                            targetEntity = DictionaryUtil.findById(conf.getDataSource(), c);
                            break;
                        default:
                    }
                } catch (TranslateException e) {
                    if (defaultValue == null) {
                        throw e;
                    }
                }
                if (builder.length() > 0) {
                    builder.append(Constant.RegularAbout.COMMA);
                }
                if (targetEntity == null) {
                    if (Constant.AgileAbout.DIC_TRANSLATE_FAIL_VALUE.equals(defaultValue)) {
                        builder.append(StringUtil.getSplitByStrLastAtomic(c, split));
                    } else if (defaultValue != null) {
                        builder.append(defaultValue);
                    }
                } else {
                    if (isFull) {
                        switch (conf.getDirectionType()) {
                            case CODE_TO_NAME:
                            case ID_TO_NAME:
                                builder.append(targetEntity.getFullName(split));
                                break;
                            case CODE_TO_ID:
                            case NAME_TO_ID:
                                builder.append(targetEntity.getFullId(split));
                                break;
                            case NAME_TO_CODE:
                            case ID_TO_CODE:
                                builder.append(targetEntity.getFullCode(split));
                                break;
                            default:
                        }

                    } else {
                        switch (conf.getDirectionType()) {
                            case CODE_TO_NAME:
                            case ID_TO_NAME:
                                builder.append(targetEntity.getName());
                                break;
                            case CODE_TO_ID:
                            case NAME_TO_ID:
                                builder.append(targetEntity.getId());
                                break;
                            case NAME_TO_CODE:
                            case ID_TO_CODE:
                                builder.append(targetEntity.getCode());
                                break;
                            default:
                        }
                    }
                }
            }

            targetName = builder.toString();
            if (Constant.AgileAbout.DIC_TRANSLATE_FAIL_NULL_VALUE.equals(defaultValue)) {
                targetName = ConvertDicBean.parseNullValue(targetName);
            }

            if (dicCoverCache != null && targetName == null) {
                dicCoverCache.put(threadCacheKey, Constant.AgileAbout.DIC_TRANSLATE_FAIL_NULL_VALUE);
            } else if (dicCoverCache != null) {
                dicCoverCache.put(threadCacheKey, targetName);
            }
        }
        return targetName;
    }
}
