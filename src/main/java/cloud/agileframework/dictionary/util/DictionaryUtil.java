package cloud.agileframework.dictionary.util;

import cloud.agileframework.common.util.collection.TreeUtil;
import cloud.agileframework.dictionary.DictionaryDataBase;
import cloud.agileframework.dictionary.DictionaryEngine;
import cloud.agileframework.dictionary.cache.DictionaryCacheUtil;
import cloud.agileframework.dictionary.cache.NotFoundCacheException;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static cloud.agileframework.dictionary.DictionaryEngine.DEFAULT_SPLIT_CHAR;

/**
 * @author 佟盟
 * @version 1.0SPLIT_CHAR
 * 日期 2019/3/6 15:48
 * 描述 字典工具
 * @since 1.0
 */
public class DictionaryUtil extends ConvertDicAnnotation {

	/**
	 * 根据主键查找字典
	 *
	 * @param datasource 数据源
	 * @param id         主键
	 * @param <D>        泛型
	 * @return 字典
	 */
	public static <D extends DictionaryDataBase> D findById(String datasource, String id) {
		SortedSet<DictionaryDataBase> cacheData = findAll(datasource);
		return Optional.ofNullable(cacheData)
				.orElse(Sets.newTreeSet())
				.stream().filter(data -> data.getId().equals(id)).map(a -> (D) a).findFirst().orElse(null);
	}

	/**
	 * 根据父主键查询字典
	 *
	 * @param datasource 数据源
	 * @param parentId   父主键
	 * @return 字典集合
	 */
	public static <D extends DictionaryDataBase> TreeSet<D> findByParentId(String datasource, String parentId) {
		SortedSet<DictionaryDataBase> cacheData = findAll(datasource);
		List<D> list = cacheData.stream()
				.filter(data -> data.getParentId().equals(parentId))
				.map(a -> (D) a).collect(Collectors.toList());
		return SerializationUtils.clone(Sets.newTreeSet(list));
	}


	/**
	 * 查找全部
	 *
	 * @param datasource 数据源
	 * @return 字典集合
	 */
	public static SortedSet<DictionaryDataBase> findAll(String datasource) {
		try {
			SortedSet<DictionaryDataBase> treeSet = DictionaryCacheUtil.getDictionaryCache().getDataByDatasource(datasource);

			//初始化全字典值与字典码默认值
			treeSet.forEach(dic -> {
				dic.setFullCode(dic.getCode());
				dic.setFullName(dic.getName());
			});

			//构建树形结构，过程中重新计算全字典值与全字典码
			TreeUtil.createTree(treeSet,
					DictionaryEngine.getDictionaryDataManagerMap(datasource).rootParentId(),
					DEFAULT_SPLIT_CHAR,
					"fullName", "fullCode"
			);
			return treeSet;
		} catch (NotFoundCacheException e) {
			e.printStackTrace();
		}
		return Sets.newTreeSet();
	}
}
