package cloud.agileframework.dictionary;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 佟盟
 * 日期 2020-11-10 17:24
 * 描述 字典开关
 * @version 1.0
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "agile.dictionary")
public class DictionaryProperties {
    /**
     * Agile字典支持
     */
    private boolean enable = true;
}
