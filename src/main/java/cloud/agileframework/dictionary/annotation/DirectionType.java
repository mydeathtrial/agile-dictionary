package cloud.agileframework.dictionary.annotation;

/**
 * @author 佟盟
 * 日期 2020-09-23 10:24
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public enum DirectionType {
    /**
     * 字典值转字典码
     */
    NAME_TO_CODE,
    /**
     * 字典码转字典值
     */
    CODE_TO_NAME,
    /**
     * 主键转名
     */
    ID_TO_NAME,
    /**
     * 主键转码
     */
    ID_TO_CODE,
    /**
     * 名转主键
     */
    NAME_TO_ID,
    /**
     * 码转主键
     */
    CODE_TO_ID
}
