package cloud.agileframework.dictionary.cache;

import cloud.agileframework.dictionary.DictionaryDataBase;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class MemoryCacheImpl implements DictionaryCache {
	public static final MemoryCacheImpl INSTANT = new MemoryCacheImpl();

	/**
	 * 缓存数据存储介质
	 * key：字典类型
	 * value：经过计算后的字典缓存数据
	 */
	private static final Map<String, Map<RegionEnum, Map<String, DictionaryDataBase>>> REGION_DATA = Maps.newConcurrentMap();

	/**
	 * 初始化缓存数据
	 *
	 * @param regionEnum 缓存区域
	 * @param cacheData  经过计算后的字典缓存数据，这部分的key类型需要与regionEnum对应
	 */
	public void initData(String datasource, RegionEnum regionEnum, Map<String, DictionaryDataBase> cacheData) {
		Map<RegionEnum, Map<String, DictionaryDataBase>> data = REGION_DATA.get(datasource);
		if (data == null) {
			data = Maps.newHashMap();
		}
		data.put(regionEnum, cacheData);
		REGION_DATA.put(datasource, data);
	}

	/**
	 * 根据缓存区域，获取经过计算的字典缓存数据
	 *
	 * @param regionEnum 缓存区域
	 * @return 经过计算的字典缓存数据
	 */
	public Map<String, DictionaryDataBase> getDataByRegion(String datasource, RegionEnum regionEnum) {
		Map<RegionEnum, Map<String, DictionaryDataBase>> data = REGION_DATA.get(datasource);
		if (data == null) {
			return Maps.newHashMap();
		}
		Map<String, DictionaryDataBase> result = data.get(regionEnum);
		if (result == null) {
			return Maps.newHashMap();
		}
		return result;
	}

	@Override
	public synchronized void add(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException {
		Map<RegionEnum, Map<String, DictionaryDataBase>> regionData = REGION_DATA.get(datasource);
		if (regionData == null) {
			SortedSet<DictionaryDataBase> sortedSet = new ConcurrentSkipListSet<>();
			sortedSet.add(dictionaryData);
			initData(datasource, sortedSet);
			return;
		}

		Map<String, DictionaryDataBase> codeData = regionData.computeIfAbsent(RegionEnum.CODE_MEMORY, k -> Maps.newHashMap());
		codeData.put(dictionaryData.getFullCode(), dictionaryData);

		Map<String, DictionaryDataBase> nameData = regionData.computeIfAbsent(RegionEnum.NAME_MEMORY, k -> Maps.newHashMap());
		nameData.put(dictionaryData.getFullName(), dictionaryData);

		Map<String, DictionaryDataBase> fullIdData = regionData.computeIfAbsent(RegionEnum.FULL_ID_MEMORY, k -> Maps.newHashMap());
		fullIdData.put(dictionaryData.getFullId(), dictionaryData);

		Map<String, DictionaryDataBase> idData = regionData.computeIfAbsent(RegionEnum.ID_MEMORY, k -> Maps.newHashMap());
		idData.put(dictionaryData.getId(), dictionaryData);
	}

	@Override
	public synchronized void delete(String datasource, DictionaryDataBase dictionaryData) throws NotFoundCacheException {
		Map<RegionEnum, Map<String, DictionaryDataBase>> regionData = REGION_DATA.get(datasource);
		if (regionData == null) {
			return;
		}
		regionData.computeIfPresent(RegionEnum.CODE_MEMORY, (regionEnum, stringDictionaryDataBaseMap) -> {
			stringDictionaryDataBaseMap.remove(dictionaryData.getFullCode());
			return stringDictionaryDataBaseMap;
		});

		regionData.computeIfPresent(RegionEnum.NAME_MEMORY, (regionEnum, stringDictionaryDataBaseMap) -> {
			stringDictionaryDataBaseMap.remove(dictionaryData.getFullName());
			return stringDictionaryDataBaseMap;
		});

		regionData.computeIfPresent(RegionEnum.FULL_ID_MEMORY, (regionEnum, stringDictionaryDataBaseMap) -> {
			stringDictionaryDataBaseMap.remove(dictionaryData.getFullId());
			return stringDictionaryDataBaseMap;
		});

		regionData.computeIfPresent(RegionEnum.ID_MEMORY, (regionEnum, stringDictionaryDataBaseMap) -> {
			stringDictionaryDataBaseMap.remove(dictionaryData.getId());
			return stringDictionaryDataBaseMap;
		});
	}
}
