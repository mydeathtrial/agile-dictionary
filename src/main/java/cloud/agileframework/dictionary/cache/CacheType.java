package cloud.agileframework.dictionary.cache;

/**
 * 字典缓存介质类型
 */
public enum CacheType {
    /**
     * 内存形式
     */
    MEMORY,
    /**
     * SpringCache方式
     */
    SPRING,
    /**
     * agile-cache二级缓存方式
     */
    AGILE_CACHE
}
