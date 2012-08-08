package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.VarsMaker;

public class StatsRuntimeGatherer extends AbstractGatherer {

	public StatsRuntimeGatherer(String serviceName, String schedule) {
		super(serviceName, schedule);
	}
	
	public StatsRuntimeGatherer(String serviceName, String schedule, List<String> keys) throws GathererException {
		super(serviceName, schedule, keys);		
	}

	@Override
	public Map<String, Object> getStatistics() {
		Map<String, Object> stats = VarsMaker.createMap();
		Runtime runtime = Runtime.getRuntime();
		stats.put(STAT_MAX_MEMORY, runtime.maxMemory());
		stats.put(STAT_FREE_MEMORY, runtime.freeMemory());
		stats.put(STAT_TOTAL_MEMORY, runtime.totalMemory());
		stats.put(STAT_PROCESSORS, runtime.availableProcessors());
		stats.put(STAT_TIME_STAMP, new Date(System.currentTimeMillis()));
		return stats;
	}
}
