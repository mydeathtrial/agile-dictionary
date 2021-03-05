package com.agile.common.util;

import cloud.agileframework.dictionary.annotation.Dictionary;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author 佟盟
 * 日期 2020-09-27 14:46
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Builder
@Data
@ToString
public class Data3 {

    private String status;

    @Dictionary(fieldName = "status")
    private String text;
    @Dictionary(fieldName = "status", isFull = true)
    private String fullText;
}
