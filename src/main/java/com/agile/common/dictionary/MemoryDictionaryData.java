package com.agile.common.dictionary;

import java.util.List;

/**
 * @author 佟盟
 * 日期 2020/7/31 19:45
 * 描述 内存字典
 * @version 1.0
 * @since 1.0
 */
public class MemoryDictionaryData implements DictionaryData {
    private String id;
    private String parentId;
    private String name;
    private String code;
    private String fullName;
    private String fullCode;
    private List<DictionaryData> children;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFullCode() {
        if(fullCode == null){
            return code;
        }
        return fullCode;
    }

    @Override
    public void setFullCode(String fullCode) {
        this.fullCode = fullCode;
    }

    @Override
    public String getFullName() {
        if(fullName == null){
            return name;
        }
        return fullName;
    }

    @Override
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public List<DictionaryData> getChildren() {
        return children;
    }

    public MemoryDictionaryData() {
    }

    public MemoryDictionaryData(String id, String parentId, String name, String code) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.code = code;
    }

    @Override
    public String toString() {
        return "MemoryDictionaryData{" +
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
