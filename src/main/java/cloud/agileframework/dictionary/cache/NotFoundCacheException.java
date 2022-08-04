package cloud.agileframework.dictionary.cache;

/**
 * 未找到缓存介质异常
 */
public class NotFoundCacheException extends Exception {
    public NotFoundCacheException() {
    }

    public NotFoundCacheException(String message) {
        super(message);
    }

    public NotFoundCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundCacheException(Throwable cause) {
        super(cause);
    }

    public NotFoundCacheException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
