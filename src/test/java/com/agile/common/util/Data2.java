package com.agile.common.util;

import cloud.agileframework.dictionary.annotation.Dictionary;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

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
public class Data2 {
    //CHINA
    private Integer country;

    //HLJ
    private Integer city;

    //HRB
    private Integer region;

    @Dictionary(fieldName = {"country"})
    private String countryValue;

    @Dictionary(fieldName = {"country", "city"})
    private String cityValue;

    //中国/黑龙江/哈尔滨
    //哈尔滨
    @Dictionary(fieldName = {"country", "city", "region"}, isFull = true)
    private String regionValue;
}
