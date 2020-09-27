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
public class Data4 {
    private String country;
    private String city;
    private String region;

    @Dictionary(fieldName = {"country"}, directionType = DirectionType.NameToCode)
    private Integer countryValue;
    @Dictionary(fieldName = {"country", "city"}, directionType = DirectionType.NameToCode)
    private Integer cityValue;
    @Dictionary(fieldName = {"country", "city", "region"}, directionType = DirectionType.NameToCode)
    private Integer regionValue;
}
