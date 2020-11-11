# agile-dictionary ： 字典解析器
[![spring-boot](https://img.shields.io/badge/Spring--boot-LATEST-green)](https://img.shields.io/badge/spring-LATEST-green)
[![maven](https://img.shields.io/badge/build-maven-green)](https://img.shields.io/badge/build-maven-green)
## 它有什么作用

* **启动自动加载**
集成该组件后，字典数据加载将伴随spring应用启动，将持久层字典数据加载如缓存，以便提高缓存查询性能

* **缓存同步**
集成缓存组件agile-cache可实现缓存方式切换`spring.cache.type`，如redis、ehcache、memory等方式，缓存使用请参照缓存组件https://gitee.com/agile-framework/agile-cache

* **复杂翻译**
通过提供工具类cloud.agileframework.dictionary.util.DictionaryUtil，实现诸多复杂字典翻译方式，如字典码与字典值相互转换，根据父子字典信息翻译、全路径字典值/码翻译、字典注解解析
集合数据字典翻译、自定义字典分隔符、指定默认值等等一系列工具

* **字典注解**
用于pojo类属性上添加cloud.agileframework.dictionary.annotation.Dictionary字典注解，通过DictionaryUtil.cover方法或内嵌至持久层组件中，实现无感翻译。
agile-jpa组件中已集成该组件实现无感翻译。

* **自定义字典分隔符**
字典分隔符指针对全路径字典值/码的分级标识符，如`状态`字典有子值`开`与`关`两种类型，则`开`与`关`字典值分别对应`状态/开`与`状态/关`，其中斜杠`/`就是字典分隔符，该分隔符可在调用翻译工具或字典
翻译注解中指定，且对字典值/码不存在任何特殊符号限制。

* **支持枚举、整数、字符串等类型数据**
字典支持使用经常用于判断得整型、字符串、枚举等类型作为转换源，枚举类型会取枚举项name值作为转换源，可以参照源码中测试用例

* **全路径字典值码翻译**
除当前字典值/码翻译以外，还提供携带所有父级字典值/码以字典分隔符标记的全路径字典翻译，如`状态`字典有子值`开`与`关`两种类型，则`开`与`关`为当前字典值，全路径字典值则分别对应`状态/开`与`状态/关`
字典码亦是如此

* **级联字典值码翻译**
以“国家”、“城市”、“地区”一类的级联字典为例子，其数据结构中往往同时存在这三个属性，翻译子字典时需要依赖父字典值。在字典注解中提供了该功能的支持，用法如下：
country代表国家、city代表城市、region代表地区。
```
    private String country;
    private String city;
    private String region;
        
    @Dictionary(fieldName = {"country"})
    private String countryValue;
    @Dictionary(fieldName = {"country","city"})
    private String cityValue;
    @Dictionary(fieldName = {"country","city","region"})
    private String regionValue;
```

* **正向/反向翻译支持**
注解及工具均支持字典码与字典值之间的互转，默认注解方式的转换方向为字典码转字典值，可通过`directionType`转换方向，如：
```
    private String countryName;
    
    @Dictionary(fieldName = "countryName", directionType = DirectionType.NameToCode)
    private String countryCode;
```

* **自定义持久化数据方式**
字典的持久化方式默认直接使用内存形式，用户可以通过实现`cloud.agileframework.dictionary.DictionaryDataManager`，实现自定义的字典数据持久化方式，如使用mysql存储字典数据，字典的数据结构
需符合接口`cloud.agileframework.dictionary.DictionaryData`规范，当切换mysql时，开发人员只需要将字典表的orm映射继承自该接口，并实现接口方法即可完成mysql切换。

* **组件开关**
内置组件开启，配置：agile.dictionary.enable=true，默认为开启，用户也可使用springboot组件排除方式实现排除加载
-------
## 快速入门
开始你的第一个项目是非常容易的。

#### 步骤 1: 下载包
您可以从[最新稳定版本]下载包(https://github.com/mydeathtrial/agile-dictionary/releases).
该包已上传至maven中央仓库，可在pom中直接声明引用

以版本agile-dictionary-2.0.6.jar为例。
#### 步骤 2: 添加maven依赖
```
//声明中央仓库
<repository>
    <id>cent</id>
    <url>https://repo1.maven.org/maven2/</url>
</repository>
//添加依赖
<dependency>
    <groupId>cloud.agileframework</groupId>
    <artifactId>agile-dictionary</artifactId>
    <version>2.0.6</version>
</dependency>
```
#### 步骤 3: 开箱即用

##### 工具部分常用方法汇总：
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

##### 字典注解使用：cloud.agileframework.dictionary.annotation.Dictionary
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


##### 自定义持久化方式
DictionaryDataManager：字典数据管理器（类似xxxService，用于提供持久层交互API，如字典的增删改查操作）
DictionaryDataBase：字典数据结构化基类（类似于xxxDo，一般用于数据库字典表的ORM映射）
默认的持久化方式为内存形式，当开发人员需要自定义持久化方式时，可直接实现或继承以上两个接口，并将实现类注入到spring容器中即可
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

##### 缓存同步
直接调用DictionaryDataManagerProxy的增删改方法，例如：
```
private class YourBean {

    @Autowird
    private DictionaryDataManagerProxy manager;

    public void add(){
        //MemoryDictionaryData为内存方式字典，该处可改为自定义DictionaryData实现
        manager.add(new MemoryDictionaryData("1", null, "状态", "status"));
    }

    public void delete(){
        //MemoryDictionaryData为内存方式字典，该处可改为自定义DictionaryData实现
        manager.delete("1");
        //方式2
        manager.delete(new MemoryDictionaryData("1", null, "状态", "status"));
    }

    public void update(){
        //MemoryDictionaryData为内存方式字典，该处可改为自定义DictionaryData实现
        manager.update(new MemoryDictionaryData("1", null, "状态", "status"));
    }
}
```
##### 字典注解
字典注解支持定义翻译依据字典、翻译方向、全路径翻译、翻译分隔符等内容
```java
public @interface Dictionary {
    /**
     * 字典码
     */
    String dicCode() default "";

    /**
     * 指向字典字段
     */
    String[] fieldName();

    /**
     * 是否翻译出全路径字典值
     */
    boolean isFull() default false;

    /**
     * 全路径字典值分隔符
     */
    String split() default ".";

    /**
     * 字典转换方向
     */
    DirectionType directionType() default DirectionType.CodeToName;
}
```