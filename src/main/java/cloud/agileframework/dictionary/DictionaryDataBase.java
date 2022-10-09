package cloud.agileframework.dictionary;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.collection.TreeBase;
import cloud.agileframework.common.util.pinyin.PinYinUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

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
@SuperBuilder(toBuilder = true)
public class DictionaryDataBase extends TreeBase<String, DictionaryDataBase> {

    @Getter
    private String code;
    @Getter
    private String name;
    private String fullName;
    private String fullCode;
    private String fullId;

    public DictionaryDataBase() {
        super();
    }

    public DictionaryDataBase(String id, String parentId, String name, String code) {
        super();
        setId(id);
        setParentId(parentId);
        this.name = name;
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFullCode() {
        return fullCode;
    }

    public String getFullId() {
        return fullId;
    }

    public String getFullName(String splitChar) {
        return fullName.replace(Constant.AgileAbout.DIC_SPLIT, splitChar);
    }

    public String getFullCode(String splitChar) {
        return fullCode.replace(Constant.AgileAbout.DIC_SPLIT, splitChar);
    }

    public String getFullId(String splitChar) {
        return fullId.replace(Constant.AgileAbout.DIC_SPLIT, splitChar);
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

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public String getParentId() {
        return super.getParentId();
    }

    private String pinYin;

    public String getPinYin() {
        if (pinYin == null) {
            pinYin = PinYinUtil.converterToFirstSpell(getName());
        }
        return pinYin;
    }

    @Override
    public int compareTo(DictionaryDataBase o) {
        if (Objects.equals(this, o)) {
            return 0;
        }
        if (o == null) {
            return 1;
        }
        int a = o.getSort() == null ? 0 : o.getSort();
        int b = getSort() == null ? 0 : getSort();
        int i = b - a;
        if (i == 0) {
            i = StringUtils.compare(getPinYin(),
                    PinYinUtil.converterToFirstSpell(o.getPinYin()));
        }
        if (i == 0) {
            i = StringUtils.compare(getCode(), o.getCode());
        }
        return i == 0 ? 1 : i;
    }
}
