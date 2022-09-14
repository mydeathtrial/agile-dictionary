package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.DataException;

/**
 * 数据翻译异常
 */
public class TranslateException extends DataException {
    public TranslateException() {
        super();
    }

    public TranslateException(String message) {
        super(message);
    }

    public TranslateException(String message, Throwable cause) {
        super(message, cause);
    }

    public TranslateException(Throwable cause) {
        super(cause);
    }

    protected TranslateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
