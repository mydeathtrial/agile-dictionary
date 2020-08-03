# agile-dictionary

简介：字典通用工具集，该组件具备字典数据启动加载、复杂翻译、缓存同步能力、支持redis等自定义缓存方式、字典注解、自定义字典分隔符、全路径字典值码翻译，结合持久层组件，可为持久层能力扩展自动翻译字典功能；字典默认使用内存方式提供数据存储，用户可通过向spring容器中注入自定义DictionaryDataManager接口实现自定义持久化数据方式

##使用方法：
1、手动翻译：工具类com.agile.common.util.DictionaryUtil，该工具类包含相当丰富的字典翻译方法，用于开发人员在特定场景中主动调用实现按需翻译。
2、自动翻译：自定翻译指用于pojo类属性上添加com.agile.common.annotation.Dictionary字典注解，通过DictionaryUtil.cover方法或内嵌至持久层组件中，实现无感翻译。

##组件启动条件：
1、开启配置：agile.dictionary.enable=true

##工具部分常用方法汇总：
```
    /**
     * 转换字典对象
     *
     * @param fullCode 全路径字典码,如status.yes
     * @return bean
     */
    public static DictionaryData coverDicBean(String fullCode)


    /**
     * 转换字典对象
     *
     * @param fullCode  全路径字典码,如status-yes
     * @param splitChar 自定义分隔符,如-
     * @return bean
     */
    public static DictionaryData coverDicBean(String fullCode, String splitChar)

    /**
     * 转换字典对象
     *
     * @param fullName 全路径字典值
     * @return bean
     */
    public static DictionaryData coverDicBeanByFullName(String fullName)

    /**
     * 转换字典对象
     *
     * @param fullName  全路径字典值
     * @param splitChar 自定义分隔符
     * @return bean
     */
    public static DictionaryData coverDicBeanByFullName(String fullName, String splitChar) 

    /**
     * 根据父级树形字典码与子树形name获取字典
     *
     * @param parentCode 字典
     * @param name       子字典值
     * @return 字典数据
     */
    public static DictionaryData coverDicBeanByParent(String parentCode, String name) 

    /**
     * 编码转字典值
     *
     * @param fullCodes 全路径字典码，支持包含逗号分隔的多全路径字典码，转换结果为逗号分隔非全路径字典值
     * @return 字典值
     */
    public static String coverDicName(String fullCodes) 

    /**
     * 编码转字典值
     *
     * @param fullCodes   全路径字典码，支持包含逗号分隔的多全路径字典码，转换结果为逗号分隔非全路径字典值
     * @param defaultName 当未找到对应字典时，默认翻译结果
     * @return 字典值
     */
    public static String coverDicName(String fullCodes, String defaultName)

    /**
     * 根据父全路径字典码，与子字典码集，转子字典值集
     *
     * @param parentCode 父全路径字典码
     * @param codes      子字典码集合，逗号分隔
     * @return 非全路径子字典值集合，逗号分隔
     */
    public static String coverDicNameByParent(String parentCode, String codes)

    /**
     * 根据父级字典与子字典(多，逗号分隔)，转换字典值
     *
     * @param parentCode   父全路径字典码
     * @param codes        子字典码集合，逗号分隔
     * @param defaultValue 默认值
     * @return 非全路径子字典值集合，逗号分隔
     */
    public static String coverDicNameByParent(String parentCode, String codes, String defaultValue)

    /**
     * 根据父级字典与子字典(多，逗号分隔)，转换字典值
     *
     * @param parentCode   父全路径字典码
     * @param codes        子字典码集合，逗号分隔
     * @param defaultValue 默认值
     * @param isFull       是否全路径模式翻译
     * @param splitChar    自定义分隔符
     * @return 逗号分隔字典值
     */
    public static String coverDicNameByParent(String parentCode, String codes, String defaultValue, boolean isFull, String splitChar)

    /**
     * 编码转字典值
     *
     * @param fullCodes   全路径字典码，支持包含逗号分隔的多全路径字典码，转换结果为逗号分隔非全路径字典值
     * @param defaultName 未找到字典时默认返回值
     * @param isFull      true 全路径名，false 字典值
     * @param splitChar   自定义分隔符
     * @return 字典值
     */
    public static String coverDicName(String fullCodes, String defaultName, boolean isFull, String splitChar)

    /**
     * 编码转字典编码
     *
     * @param fullNames 全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码集合
     * @return 字典码
     */
    public static String coverDicCode(String fullNames) 

    /**
     * 编码转字典码
     * @param defaultCode 未找到字典时默认返回值
     * @param fullNames 全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码集
     * @return 字典码
     */
    public static String coverDicCode(String fullNames, String defaultCode) 

    /**
     * 根据父全路径字典值，与子字典值集，转子字典码集
     *
     * @param parentName 父全路径字典值
     * @param names      子字典值集合，逗号分隔
     * @return 非全路径子字典码集合，逗号分隔
     */
    public static String coverDicCodeByParent(String parentName, String names)

    /**
     * 根据父级字典值与子字典值(多，逗号分隔)，转换字典码
     *
     * @param parentName   父全路径字典值
     * @param names        子字典值集合，逗号分隔
     * @param defaultValue 默认值
     * @return 非全路径子字典码集合，逗号分隔
     */
    public static String coverDicCodeByParent(String parentName, String names, String defaultValue)

    /**
     * 根据父级字典值与子字典值(多，逗号分隔)，转换字典码
     *
     * @param parentName   父全路径字典值
     * @param names        子字典值集合，逗号分隔
     * @param defaultValue 默认值
     * @param isFull       是否全路径模式翻译
     * @return 逗号分隔字典码
     */
    public static String coverDicCodeByParent(String parentName, String names, String defaultValue, boolean isFull, String splitChar)

    /**
     * 编码转字典值
     *
     * @param fulNames 全路径字典值，支持包含逗号分隔的多全路径字典值，转换结果为逗号分隔非全路径字典码
     * @param isFull   true 全路径名，false 字典值
     * @return 字典码
     */
    public static String coverDicCode(String fulNames, String defaultName, boolean isFull, String splitChar)

    /**
     * 集合类型转换字典码工具，转换为List/Map类型
     *
     * @param list           要进行转换的集合
     * @param dictionaryCode 要使用的字典码
     * @param suffix         转换出来的字典值存储的字段后缀
     * @param column         转换字段集
     * @param <T>            泛型
     * @return 返回List/Map类型，增加_text字段
     * @throws NoSuchFieldException   没有这个字段
     * @throws IllegalAccessException 非法访问
     */
    public static <T> List<Map<String, Object>> coverMapDictionary(List<T> list, String dictionaryCode, String suffix, String column)

    /**
     * 对象转换字典
     *
     * @param o               pojo或map对象
     * @param dictionaryCodes 要转换的columns对应的字典码，其长度与columns长度应该保持一致，一一对应关系
     * @param suffix          转换后的字典值存放到结果集map中的key值后缀
     * @param columns         要转换的pojo属性名或map的key值数组
     * @param <T>             泛型
     * @return 转换后的Map结果数据
     * @throws NoSuchFieldException   异常
     * @throws IllegalAccessException 异常
     */
    public static <T> Map<String, Object> coverMapDictionary(T o, String[] dictionaryCodes, String suffix, String[] columns)

    /**
     * 集合类型转换字典码工具，转换为List/T类型
     *
     * @param list            要进行转换的集合
     * @param dictionaryCodes 要使用的字典码
     * @param columns         转换字段集
     * @param <T>             泛型
     * @return 返回List/Map类型，字典码字段自动被转换为字典值
     */
    public static <T> List<T> coverBeanDictionary(List<T> list, String[] dictionaryCodes, String[] columns, String[] textColumns, String[] defaultValues) 

    /**
     * 字典自动转换，针对Dictionary注解进行解析
     *
     * @param o   目标数据
     * @param <T> 泛型
     */
    public static <T> void cover(T o) 

    /**
     * 字典自动转换，针对Dictionary注解进行解析
     *
     * @param list 目标数据集
     * @param <T>  泛型
     */
    public static <T> void cover(List<T> list)
```

##字典注解使用：com.agile.common.annotation.Dictionary
```
    public static class TuDou {
        /**
         * 字典码
         */
        private String status;

        /**
         * 翻译过后的字典值
         */
        @Dictionary(fieldName = "status", isFull = true)
        private String text;
        ...
    }
```

该对象或该对象集合，可直接充当参数，直接调用DictionaryUtil.cover方法进行翻译，字典翻译器会自动根据status属性与注解，将翻译的字典值结果装填到text属性中。


##自定义持久化方式
DictionaryDataManager：字典数据管理器（类似xxxService，用于提供持久层交互API，如字典的增删改查操作）
DictionaryData：字典数据结构化接口（类似于xxxDo，一般用于数据库字典表的ORM映射）
默认的持久化方式为内存形式，当开发人员需要自定义持久化方式时，可直接实现以上两个接口，并将实现类注入到spring容器中即可
```
public interface DictionaryData extends Serializable {
    /**
     * 字典唯一标识
     *
     * @return 字典唯一标识
     */
    String getId();

    /**
     * 字典父级标识
     *
     * @return 字典父级标识
     */
    String getParentId();

    /**
     * 字典编码
     *
     * @return 字典编码
     */
    String getCode();

    /**
     * 字典显示名
     *
     * @return 字典显示名
     */
    String getName();

    /**
     * 全路径字典码
     *
     * @return 全路径字典码
     */
    String getFullCode();

    /**
     * 全路径字典码
     *
     * @param fullCode 全路径字典码
     */
    void setFullCode(String fullCode);

    /**
     * 全路径字典名
     *
     * @return 全路径字典名
     */
    String getFullName();

    /**
     * 全路径字典名
     *
     * @param fullName 全路径字典名
     */
    void setFullName(String fullName);

    /**
     * 子字典集
     *
     * @return 子字典集
     */
    List<DictionaryData> getChildren();


}
public interface DictionaryDataManager {
    /**
     * 获取所有字典数据
     * @return 字典数据集合
     */
    List<DictionaryData> all();

    /**
     * 新增字典
     * @param dictionaryData 字典
     */
    void add(DictionaryData dictionaryData);

    /**
     * 删除字典
     * @param code 字典码
     */
    void delete(String code);

    /**
     * 更新字典
     * @param dictionaryData 字典数据
     */
    void update(DictionaryData dictionaryData);
}
```

##缓存同步
直接调用DictionaryDataManagerProxy的增删改方法，例如：
```
private class YourBean {

    @Autowird
    private DictionaryDataManagerProxy manager;

    public void add(){
        //MemoryDictionaryData为内存方式字典，该处可改为自定义DictionaryData实现
        manager.add(new MemoryDictionaryData("1", null, "状态", "status"));
    }

    public void add(){
        //MemoryDictionaryData为内存方式字典，该处可改为自定义DictionaryData实现
        manager.delete("1");
        //方式2
        manager.delete(new MemoryDictionaryData("1", null, "状态", "status"));
    }

    public void add(){
        //MemoryDictionaryData为内存方式字典，该处可改为自定义DictionaryData实现
        manager.update(new MemoryDictionaryData("1", null, "状态", "status"));
    }
}
```