package com.agile.common.util;

import cloud.agileframework.dictionary.annotation.Dictionary;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author 佟盟
 * 日期 2020-09-27 15:19
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@ToString
public class Data5 {
    private SexEnum code;
    @Dictionary(fieldName = "code", dicCode = "sex")
    private String text;
}
