package cloud.agileframework.dictionary.sync;

import cloud.agileframework.common.util.collection.TreeBase;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryEngine;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import lombok.SneakyThrows;
import org.apache.commons.lang3.math.NumberUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 佟盟
 * 日期 2021-02-19 10:33
 * 描述 缓存同步接口
 * @version 1.0
 * @since 1.0
 */
public interface SyncCache {
    /**
     * 字典数据集缓存使用的默认key
     */
    String SYNC_CACHE_KEY = "dictionary-cache";

    /**
     * 字典数据集缓存使用的默认key
     */
    String SYNC_VERSION_CACHE_KEY = "dictionary-cache-version";

    /**
     * 字典数据集缓存锁使用的默认key
     */
    String SYNC_LOCK_CACHE_KEY = "dictionary-cache-lock";

    /**
     * 版本号
     */
    AtomicInteger VERSION = new AtomicInteger();

    /**
     * 通知发布
     *
     * @param newCacheVersion 新版本
     */
    void notice(int newCacheVersion);

    /**
     * 存
     *
     * @param key   key
     * @param value value
     */
    void put(String key, Object value);

    /**
     * 取
     *
     * @param key key
     * @return 字典数据集
     */
    Object get(String key);

    /**
     * 并发锁
     *
     * @return true取锁成功，false失败
     */
    default boolean lock() {
        return true;
    }

    /**
     * 解锁
     */
    default void unlock() {
    }

    /**
     * 内存同步到缓存
     */
    default void memoryToCache() {
        List<DictionaryDataBase> list = DictionaryEngine.getAllMemory();
        list.forEach(dic -> {
            DictionaryEngine.getCodeMemory().put(dic.getFullCode(), dic);
            DictionaryEngine.getNameMemory().put(dic.getFullName(), dic);
        });


        put(SYNC_CACHE_KEY, JSON.toJSONString(list));
        put(SYNC_VERSION_CACHE_KEY, VERSION.get());
    }

    /**
     * 缓存同步到内存
     *
     * @return 是否同步成功
     */
    default boolean cacheToMemory() {
        //取缓存数据
        Object dicCacheJson = get(SYNC_CACHE_KEY);

        if (dicCacheJson == null) {
            return false;
        }
        List<DictionaryDataBase> cacheData = JSON.parseObject(dicCacheJson.toString(), new TypeReference<List<DictionaryDataBase>>() {
        }.getType(), new ParserConfig() {
            @Override
            public ObjectDeserializer getDeserializer(Type type) {
                if (type instanceof ParameterizedTypeImpl && TreeBase.class.isAssignableFrom(((ParameterizedTypeImpl) type).getRawType())) {
                    return super.getDeserializer(DictionaryDataBase.class);
                }
                return super.getDeserializer(type);
            }
        });

        if (cacheData.isEmpty()) {
            return false;
        }

        //同步到内存
        List<DictionaryDataBase> allMemory = DictionaryEngine.getAllMemory();
        allMemory.clear();
        allMemory.addAll(cacheData);

        cacheData.forEach(dic -> {
            DictionaryEngine.getCodeMemory().put(dic.getFullCode(), dic);
            DictionaryEngine.getNameMemory().put(dic.getFullName(), dic);
        });

        //同步版本号
        final Object cacheVersion = get(SYNC_VERSION_CACHE_KEY);
        if (cacheVersion != null) {
            VERSION.set(Integer.parseInt(cacheVersion.toString()));
        }

        return true;
    }

    /**
     * 接收、订阅
     *
     * @param noticeVersion 缓存版本
     */
    @SneakyThrows
    default void message(int noticeVersion) {
        //提取缓存中的版本号
        final Object cacheVersionData = get(SYNC_VERSION_CACHE_KEY);
        if (cacheVersionData == null || !NumberUtils.isCreatable(cacheVersionData.toString())) {
            //缓存中如果没有版本号，说明系统缓存数据被误删，不同步
            throw new DictionarySyncException("The cached version number was not found");
        }
        int cacheVersion = NumberUtils.toInt(cacheVersionData.toString());
        if (cacheVersion < noticeVersion) {
            //缓存的版本号小于通知的版本号，说明出现缓存数据同步错误，理论上缓存版本号只可能大于通知的版本号
            throw new DictionarySyncException("The version number of the notification does not match the version number of the cache");
        }

        if (cacheVersion <= VERSION.get()) {
            //缓存的版本号小于等于内存版本，不做同步
            return;
        }
        cacheToMemory();
    }

    /**
     * 执行字典数据操作，之后执行缓存与内存之间同步，操作过程中请求操作锁，防止并发
     *
     * @param methodFunction 某操作
     * @param changeData     有没有修改数据
     */
    default void sync(MethodFunction methodFunction, boolean changeData) {
        int count = 10;

        while (count > 0) {

            if (lock()) {
                try {
                    //某操作
                    methodFunction.method();

                    if (changeData) {
                        //升级版本号
                        VERSION.addAndGet(1);

                        //数据发生变化时，内存向缓存同步
                        memoryToCache();

                        //发布
                        notice(VERSION.get());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    unlock();
                }
                return;
            }

            try {
                Thread.sleep(Duration.ofSeconds(2).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            count--;
        }
    }
}
