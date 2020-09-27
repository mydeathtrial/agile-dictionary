package com.agile.common.util;

import cloud.agileframework.dictionary.annotation.Dictionary;
import cloud.agileframework.dictionary.annotation.DirectionType;
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
    private Integer country;
    private Integer city;
    private Integer region;

    @Dictionary(fieldName = {"country"})
    private String countryValue;
    @Dictionary(fieldName = {"country","city"})
    private String cityValue;
    @Dictionary(fieldName = {"country","city","region"})
    private String regionValue;
}
