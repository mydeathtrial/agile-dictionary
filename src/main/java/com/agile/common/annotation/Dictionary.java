package com.agile.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 佟盟
 * 日期 2020/3/17 11:16
 * 描述 字典翻译注解
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dictionary {
    /**
     * 字典码
     */
    String dicCode() default "";

    /**
     * 指向字典字段
     */
    String fieldName();

    /**
     * 是否翻译出全路径字典值
     */
    boolean isFull() default false;

    /**
     * 全路径字典值分隔符
     */
    String split() default ".";
}
