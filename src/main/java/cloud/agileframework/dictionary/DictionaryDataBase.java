package cloud.agileframework.dictionary;

import cloud.agileframework.common.util.collection.TreeBase;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2020/7/31 19:45
 * 描述 内存字典
 * @version 1.0
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
public class DictionaryDataBase extends TreeBase<String> implements Serializable {

    private String code;
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
            return super.getName();
        }
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCode() {
        return code;
    }

    public String getFullCode() {
        if (fullCode == null) {
            return code;
        }
        return fullCode;
    }

    public void setFullCode(String fullCode) {
        this.fullCode = fullCode;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public List<DictionaryDataBase> getChildren() {
        return (List<DictionaryDataBase>) super.getChildren();
    }

    @Override
    public String toString() {
        return "DictionaryDataBase{" +
                "id='" + super.getId() + '\'' +
                ", parentId='" + super.getParentId() + '\'' +
                ", name='" + super.getName() + '\'' +
                ", code='" + code + '\'' +
                ", fullName='" + fullName + '\'' +
                ", fullCode='" + fullCode + '\'' +
                ", children=" + super.getChildren() +
                '}';
    }
}
