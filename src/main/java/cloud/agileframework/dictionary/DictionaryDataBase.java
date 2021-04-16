package cloud.agileframework.dictionary;

import cloud.agileframework.common.util.collection.TreeBase;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * @author 佟盟
 * 日期 2020/7/31 19:45
 * 描述 内存字典
 * @version 1.0
 * @since 1.0
 */
@Setter
@ToString(callSuper = true)
public class DictionaryDataBase extends TreeBase<String, DictionaryDataBase> {

    @Getter
    private String code;
    @Getter
    private String name;
    private String fullName;
    private String fullCode;

    public DictionaryDataBase() {
        super();
    }

    public DictionaryDataBase(String id, String parentId, String name, String code) {
        super();
        setId(id);
        setParentId(parentId);
        setName(name);
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFullCode() {
        return fullCode;
    }

    @Override
    public String getParentId() {
        return super.getParentId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DictionaryDataBase)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DictionaryDataBase that = (DictionaryDataBase) o;
        return Objects.equals(getCode(), that.getCode()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getCode(), getName());
    }
}
