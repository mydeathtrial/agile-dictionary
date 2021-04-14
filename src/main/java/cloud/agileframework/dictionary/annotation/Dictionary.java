package cloud.agileframework.dictionary.annotation;

import cloud.agileframework.dictionary.DictionaryEngine;

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

    String NULL = "null";

    /**
     * 字典码
     */
    String dicCode() default "";

    /**
     * 指向字典字段
     */
    String[] fieldName();

    /**
     * 是否翻译出全路径字典值
     */
    boolean isFull() default false;

    /**
     * 全路径字典值分隔符
     */
    String split() default ".";

    /**
     * 字典转换方向
     */
    DirectionType directionType() default DirectionType.CODE_TO_NAME;

    /**
     * 为空时默认值
     */
    String defaultValue() default NULL;

    /**
     * 是否是主键
     *
     * 如果是主键，则直接调用findById
     */
    boolean id() default false;

    /**
     * 数据源标识
     */
    String dataSource() default DictionaryEngine.DICTIONARY_DATA_CACHE;
}
