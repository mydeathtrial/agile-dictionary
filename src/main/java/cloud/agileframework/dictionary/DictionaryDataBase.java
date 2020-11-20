package cloud.agileframework.dictionary;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * @author 佟盟
 * 日期 2020/7/31 19:45
 * 描述 内存字典
 * @version 1.0
 * @since 1.0
 */
@Data
public class DictionaryDataBase implements DictionaryData {
    private String id;
    private String parentId;
    private String name;
    private String code;
    private String fullName;
    private String fullCode;
    private List<DictionaryData> children = Lists.newArrayList();

    public DictionaryDataBase() {
    }

    public DictionaryDataBase(String id, String parentId, String name, String code) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.code = code;
    }

    @Override
    public String getFullName() {
        if (fullName == null) {
            return name;
        }
        return fullName;
    }

    @Override
    public String getFullCode() {
        if (fullCode == null) {
            return code;
        }
        return fullCode;
    }

    @Override
    public String toString() {
        return "DictionaryData{" +
                "id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", fullName='" + fullName + '\'' +
                ", fullCode='" + fullCode + '\'' +
                ", children=" + children +
                '}';
    }
}
