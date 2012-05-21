package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;

import java.util.Map;

public class StatsRuntimeProperties extends StatsAbstractGatherer {

	public Map<String, Object> getStats() {
		Map<String, Object> stats = createMap();
		Runtime runtime = Runtime.getRuntime();
		stats.put(STAT_MAX_MEMORY, runtime.maxMemory());
		stats.put(STAT_FREE_MEMORY, runtime.freeMemory());
		stats.put(STAT_TOTAL_MEMORY, runtime.totalMemory());
		stats.put(STAT_PROCESSORS, runtime.availableProcessors());
		stats.put(STAT_TIME_STAMP, System.currentTimeMillis());
		return stats;
	}

	public Map<String, Object> getStatistics() {
		return getStats();
	}
	
	public static void main(String args[]){
		StatsRuntimeProperties runProps = new StatsRuntimeProperties();
		System.out.println(runProps.getStatistics());
		String[] keys = {STAT_TYPE, STAT_NAME, STAT_DATA_SIZE, STAT_TOTAL_MEMORY, STAT_FREE_MEMORY };
		System.out.println(runProps.getStatistics(keys));
	}
}