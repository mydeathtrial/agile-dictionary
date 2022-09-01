package com.agile.common.util;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.dictionary.annotation.Dictionary;
import cloud.agileframework.dictionary.annotation.DictionaryField;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2020-09-27 14:50
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@ToString
public class Data2 implements Serializable {
    //CHINA
    private String country;

    //HLJ
    private String city;

    //HRB
    private String region;

    @Dictionary(fieldName = {"country"})
    private String countryValue;

    @Dictionary(fieldName = {"country", "city"}, split = ".", defaultValue = Constant.AgileAbout.DIC_TRANSLATE_FAIL_VALUE)
    private String cityValue;

    //中国/黑龙江/哈尔滨
    //哈尔滨
    @Dictionary(fieldName = {"country", "city", "region"}, isFull = true, split = ".", defaultValue = "qqq")
    private List<String> regionValue;

    @DictionaryField
    private Data2 data2;
}
