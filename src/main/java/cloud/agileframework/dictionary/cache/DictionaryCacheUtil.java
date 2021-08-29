package cloud.agileframework.dictionary.cache;

public class DictionaryCacheUtil {
    private static DictionaryCache dictionaryCache;

    public static void setDictionaryCache(DictionaryCache dictionaryCache) {
        DictionaryCacheUtil.dictionaryCache = dictionaryCache;
    }

    public static DictionaryCache getDictionaryCache() {
        return dictionaryCache;
    }
}
