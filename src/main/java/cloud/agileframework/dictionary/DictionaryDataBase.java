package cloud.agileframework.dictionary;

import cloud.agileframework.common.util.collection.TreeBase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2020/7/31 19:45
 * 描述 内存字典
 * @version 1.0
 * @since 1.0
 */
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DictionaryDataBase extends TreeBase<String> implements Serializable {

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
        if (fullName == null) {
            return getName();
        }
        return fullName;
    }

    public String getFullCode() {
        if (fullCode == null) {
            return code;
        }
        return fullCode;
    }

    @Override
    public String getParentId() {
        return super.getParentId();
    }

    @Override
    public List<DictionaryDataBase> getChildren() {
        return (List<DictionaryDataBase>) super.getChildren();
    }
}
