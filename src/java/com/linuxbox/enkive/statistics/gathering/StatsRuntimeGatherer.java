package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;
import static com.linuxbox.enkive.statistics.VarsMaker.createMap;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class StatsRuntimeGatherer extends AbstractGatherer {
	public StatsRuntimeGatherer(String serviceName, String humanName,
			List<String> keys) throws GathererException {
		super(serviceName, humanName, keys);
	}

	@Override
	protected Map<String, Object> getPointStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		Map<String, Object> pointStats = createMap();
		Runtime runtime = Runtime.getRuntime();
		pointStats.put(STAT_MAX_MEMORY, runtime.maxMemory());
		pointStats.put(STAT_FREE_MEMORY, runtime.freeMemory());
		pointStats.put(STAT_TOTAL_MEMORY, runtime.totalMemory());
		pointStats.put(STAT_PROCESSORS, runtime.availableProcessors());
		return pointStats;
	}

	@Override
	protected Map<String, Object> getIntervalStatistics(Date lowerTimestamp,
			Date upperTimestamp) throws GathererException {
		return null;
	}
}
