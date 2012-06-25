package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_HOUR;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_STD_DEV;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_SUM;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_WEIGHT;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;

public abstract class AbstractGrain implements Grain {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.granularity.AbstractGrain");
	protected StatsClient client;
	protected Integer filterObj;
	protected int grainType;
	protected Date startDate;
	protected Date endDate;

	public AbstractGrain(StatsClient client) {
		this.client = client;
		System.out.println("starting abstract client");
		setFilterString();
		setDates();
		consolidateData();
		System.out.println("finishing abstract client");
	}

	protected abstract void setFilterString();

	protected abstract void setDates();
/*
	private double getValue(String key, Map<String, Object> map) {
		double result = -1;
		// TODO System.out.println("map: " + map);
		if (map.get(key) instanceof Integer) {
			result = (double) ((Integer) map.get(key)).intValue();
		} else if (map.get(key) instanceof Long) {
			result = (double) ((Long) map.get(key)).longValue();
		} else if (map.get(key) instanceof Double) {
			result = ((Double) map.get(key)).doubleValue();
		}
		// TODO System.out.println("result: " + result);
		return result;
	}
*/
	private Object injectType(Object example, double value) {
		Object result = null;
		if (example instanceof Integer) {
			result = (int) value;
		} else if (example instanceof Long) {
			result = (long) value;
		} else if (example instanceof Double) {
			result = value;
		}

		return result;
	}

	private Set<Map<String, Object>> serviceFilter(String name) {
		Map<String, Map<String, Object>> query = new HashMap<String, Map<String, Object>>();
		Map<String, Object> keyVals = new HashMap<String, Object>();
		keyVals.put(GRAIN_TYPE, filterObj);
		query.put(name, keyVals);
		// TODO FOR TESTING ONLY
		startDate = new Date(0L);
		endDate = new Date();
		Set<Map<String, Object>> result = client.queryStatistics(query,
				startDate, endDate);
		return result;
	}

	// TODO: store the data at a level path
	private Map<String, Object> storeOnPath(List<String> path,
			Map<String, Object> statsData, Map<String, Object> statToAdd) {
		Map<String, Object> cursor = statsData;
		int index = 0;
		for (String key : path) {
			if (index == path.size() - 1) {
				cursor.put(key, statToAdd);
			} else if (cursor.containsKey(key)) {
				if (cursor.get(key) instanceof Map) {
					cursor = (Map<String, Object>) cursor.get(key);
				}
				else{
					//TODO better exception saying we hit raw data
//					throw exception;
				}
			}
			index++;
		}

		return statsData;
	}

	private Object getDataVal(Map<String, Object> dataMap, List<String> path) {
		Object map = dataMap;
		System.out.println("getDV-path: " + path);
		for (String key : path) {
			// System.out.println("getDV-key: " + key);
			if (((Map<String, Object>) map).containsKey(key)) {
				if (((Map<String, Object>) map).get(key) instanceof Map) {
					// System.out.println("Dataval is a map: " + map);
					map = ((Map<String, Object>) map).get(key);
				} else {
					// System.out.println("returning dataVal: " + ((Map<String,
					// Object>)map).get(key));
					return ((Map<String, Object>) map).get(key);
				}
			}
		}

		System.out.println("getDataVal-null-dataMap: " + dataMap);
		return null;
	}

	private boolean pathMatches(List<String> path, List<KeyDef> keys) {
		for (KeyDef def : keys) {// get one key definition
			// TODO make sure this works
			if (def.getMethods() == null) {
				continue;
			}
			boolean isMatch = true;
			int pathIndex = 0;
			int defIndex = 0;
			List<String> keyString = def.getKey();
			while (pathIndex < path.size()) {// run through it to compare to
												// path
				if (defIndex >= keyString.size()) {
					// System.out.println("defIndex >= keys.size()");
					// System.out.println("paths don't match: " + path + " vs "
					// + keyString);
					 isMatch = false;
					 break;
				}
				String str = keyString.get(defIndex);
				if (str.equals("*")) {
					if(defIndex == keyString.size()-1){
						break;
					}
					// System.out.println("str*: " + str);
					// System.out.println("path*: " + path.get(pathIndex));
					defIndex++;
					str = keyString.get(defIndex);

					if (path.contains(str)) {
						for (; pathIndex < path.size(); pathIndex++) {// jumps
																		// to
																		// matching
																		// index
							if (path.get(pathIndex).equals(str)) {
								break;
							}
						}
					} else {
						// System.out.println("does not contain");
						// System.out.println("paths don't match: " + path +
						// " vs " + keyString);
						isMatch = false;
						break;
					}
				}
				// System.out.println("str: " + str);
				// System.out.println("path: " + path.get(pathIndex));
				if (path.get(pathIndex).equals(str)) {
					// System.out.println("if");
					pathIndex++;
					defIndex++;
				} else {
					// System.out.println("paths don't match: " + path + " vs "
					// + keyString);
					isMatch = false;
					break;
				}
			}
			if (isMatch) {
				System.out.println("paths match: " + path + " vs " + keyString);
				return true;
			}
		}
		return false;// no matches found
	}

	@SuppressWarnings("unchecked")
	private double statToDouble(Object stat) {
		double input = -1;

		if (stat instanceof Integer) {
			input = (double) ((Integer) stat).intValue();
		} else if (stat instanceof Long) {
			input = (double) ((Long) stat).longValue();
		} else if (stat instanceof Double) {
			input = ((Double) stat).doubleValue();
		} else {
			System.out
					.println("statToDouble()-unexpected data object: " + stat);
		}

		return input;
	}

	private Set<List<String>> findPathSet(Map<String, Object> data,
			LinkedList<String> path, List<KeyDef> statKeys,
			Set<List<String>> result) {
		for (String key : data.keySet()) {
			path.addLast(key);
			if (pathMatches(path, statKeys)) {
				// / System.out.println("key: " + key);
				// System.out.println("findPath-adding path: " + path);
				result.add(new ArrayList<String>(path));
			} else {// recurse again
				if (data.get(key) instanceof Map) {
					findPathSet((Map<String, Object>) data.get(key), path,
							statKeys, result);
				}
			}
			path.removeLast();
		}
		// System.out.println("findPath-result: " + result);
		return result;
	}
/*
	private Map<String, Object> consolidateMapHelper(Map<String, Object> map,
			List<String> path, List<KeyDef> keys, Map<String, Object> result) {
		System.out.println("run...");
		for (String key : map.keySet()) {
			path.add(key);
			if (pathMatches(path, keys)) {
				result.put(key, map.get(key));
			} else {// recurse again
				if (map.get(key) instanceof Map) {
					result.put(
							key,
							consolidateMapHelper(
									(Map<String, Object>) map.get(key), path,
									keys, new HashMap<String, Object>()));
				}
			}
			path.remove(path.size() - 1);
		}
		return result;
	}
*/
	//TODO: the caller should be able to know if this is raw data or not raw data
	protected Map<String, Object> consolidateMaps(
			Set<Map<String, Object>> serviceData, List<KeyDef> keys) {
		// Map<String, Object> result = consolidateMapHelper(map, path, keys,
		// new HashMap<String, Object>());
		// System.out.println("consolidateMap()-result: " + result);
		Map<String, Object> exampleData = (Map<String, Object>) serviceData
				.toArray()[0];

		// 1. recurse to find set of paths
		Set<List<String>> dataPaths = findPathSet(exampleData,
				new LinkedList<String>(), keys, new HashSet<List<String>>());
		Map<String, Object> consolidatedData = new HashMap<String, Object>(
				exampleData);
		// 2. TODO: check for pre-consolidated data
		// know from that fact if it is hourly, daily, etc.
		
		// 3. loop over paths
		for (List<String> dataPath : dataPaths) {
			int totalWeight = 0;
			DescriptiveStatistics statsMaker = new DescriptiveStatistics();
			DescriptiveStatistics avgStatsMaker = new DescriptiveStatistics();
			Object dataVal = null;

			// 4. get data from maps and add to statMakers
			for (Map<String, Object> dataMap : serviceData) {
				dataVal = getDataVal(dataMap, dataPath);
				System.out.println("maps()-dataVal: " + dataVal);
				double input = -1;

				if (dataVal != null) {
					input = statToDouble(dataVal);
					if (input > -1) {
						statsMaker.addValue(input);
					}

					if (input >= 0) {
						Integer statWeight = (Integer) dataMap
								.get(GRAIN_WEIGHT);
						if (statWeight == null) {
							statWeight = 1;
						}

						totalWeight += statWeight;
						avgStatsMaker.addValue(input * statWeight);
						statsMaker.addValue(input);
					}
				}
			}

			// 5. loop over methods to populate map with max, min, etc.
			Map<String, Object> methodData = new HashMap<String, Object>();
			for (KeyDef keyDef : keys) {
				if (keyDef.getMethods() != null) {
					for (String method : keyDef.getMethods()) {
						if (method.equals(GRAIN_SUM)) {
							methodData.put(method,
									injectType(dataVal, statsMaker.getSum()));
						}
						if (method.equals(GRAIN_MAX)) {
							methodData.put(method,
									injectType(dataVal, statsMaker.getMax()));
						}
						if (method.equals(GRAIN_MIN)) {
							methodData.put(method,
									injectType(dataVal, statsMaker.getMin()));
						}
						if (method.equals(GRAIN_AVG)) {
							methodData.put(
									method,
									injectType(dataVal, avgStatsMaker.getSum()
											/ totalWeight));
						}
						if (method.equals(GRAIN_STD_DEV)) {
							methodData.put(
									method,
									injectType(dataVal,
											statsMaker.getStandardDeviation()));
						}
					}
				} else {
					System.out.println("keyDef returned null");
				}
			}
			// 6. store in new map on path
			System.out.println("consolData-beforeStoreOnPath: "
					+ consolidatedData);
			storeOnPath(dataPath, consolidatedData, methodData);
			System.out.println("consolData-afterStoreOnPath: "
					+ consolidatedData);
		}
		return consolidatedData;
	}

	protected int findWeight(Set<Map<String, Object>> serviceData) {
		int weight = 0;
		for (Map<String, Object> map : serviceData) {
			if (map.get(GRAIN_WEIGHT) == null)
				weight++;
			else {
				weight += (Integer) map.get(GRAIN_WEIGHT);
			}
		}
		return weight;
	}

	public void consolidateData() {
		System.out.println("consolidateData()");
		// build set for each service
		// Set<Map<String,Object>> storageData = new
		// HashSet<Map<String,Object>>();
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			System.out.println("name: " + name);
			Set<Map<String, Object>> serviceData = serviceFilter(name);
			System.out.println("ServiceData: " + serviceData);
			if (!serviceData.isEmpty()) {
				consolidateMaps(serviceData, attribute.getKeys());
				if (name.equals("CollectionStatsService")) {
					System.out.println("exit");
					System.exit(0);
				}
				// TODO storageData = new HashSet<Map<String, Object>>();
				// storageData.add(consolidateMaps(serviceData, ));
			}
		}
		// client.storeData(storageData);
	}
	
	//TODO: stat.stat.* working
	public static void main(String args[]){
		KeyDef def = new KeyDef("*");
		KeyDef thing = new KeyDef("hi.how.are.you.my.good.sir");
		List<String> path = thing.getKey();
		
		boolean isMatch = true;
		int pathIndex = 0;
		int defIndex = 0;
		List<String> keyString = def.getKey();
		while (pathIndex < path.size()) {// run through it to compare to path
			if (defIndex == keyString.size()) {
				 System.out.println("defIndex >= keys.size()");
				// System.out.println("paths don't match: " + path + " vs "
				// + keyString);
			//	return false;
				 System.out.println("paths don't match: " + path + " vs " + keyString);
				 isMatch = false;
				 break;
			}
			String str = keyString.get(defIndex);
			if (str.equals("*")) {
				if(defIndex == keyString.size()-1){
					break;
				}
				// System.out.println("str*: " + str);
				// System.out.println("path*: " + path.get(pathIndex));
				defIndex++;
				str = keyString.get(defIndex);

				if (path.contains(str)) {
					for (; pathIndex < path.size(); pathIndex++) {// jumps
																	// to
																	// matching
																	// index
						if (path.get(pathIndex).equals(str)) {
							break;
						}
					}
				} else {
					// System.out.println("does not contain");
					// System.out.println("paths don't match: " + path +
					// " vs " + keyString);
					isMatch = false;
					break;
				}
			}
			// System.out.println("str: " + str);
			// System.out.println("path: " + path.get(pathIndex));
			if (path.get(pathIndex).equals(str)) {
				// System.out.println("if");
				pathIndex++;
				defIndex++;
			} else {
				// System.out.println("paths don't match: " + path + " vs "
				// + keyString);
				isMatch = false;
				break;
			}
		}
		if (isMatch) {
			System.out.println("paths match: " + path + " vs " + keyString);
		}
	}
}
