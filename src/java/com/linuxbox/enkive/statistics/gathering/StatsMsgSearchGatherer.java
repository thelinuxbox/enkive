package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.statistics.StatsConstants.SIMPLE_DATE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.THIRTY_DAYS;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchResult;
import com.mongodb.MongoException;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.*;

public class StatsMsgSearchGatherer extends AbstractGatherer {
	MessageSearchService searchService;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");
	
	public StatsMsgSearchGatherer(String serviceName, String schedule) {
		super(serviceName, schedule);
	}
	
	protected Map<String, Set<String>> keyBuilder(){
		Map<String, Set<String>> keys = new HashMap<String, Set<String>>();
		keys.put(STAT_NUM_ENTRIES, setCreator(GRAIN_AVG));
		return keys;
	}
	
	protected int numEntries(String dateEarliest, String dateLatest) {
		HashMap<String, String> hmap = new HashMap<String, String>();
		hmap.put(DATE_EARLIEST_PARAMETER, dateEarliest);
		hmap.put(DATE_LATEST_PARAMETER, dateLatest);
		int result = -1;// -1 is error value
		try {
			// TODO is this efficient? should we instead add a method to our
			// search service that would do a count of messages b/w a pair of
			// dates?
			final SearchResult result2 = searchService.search(hmap);
			final Set<String> result3 = result2.getMessageIds();
			final int count = result3.size();
			if (count == 0) {
				result = 0;
				LOGGER.warn("StatisticsMsgEntries: Warning no Entries found in numEntries() between "
						+ dateEarliest + " & " + dateLatest);
			} else {
				result = count;
			}
		} catch (MessageSearchException e) {
			LOGGER.warn(
					"MessageSearchException in StatsMsgEntries.numEntries()", e);
		} catch (NullPointerException e) {
			LOGGER.warn("NullPointerException in statsMsgEntries.numEntries()",
					e);
		}

		return result;
	}

	public Map<String, Object> getStatistics() {

		long currTime = System.currentTimeMillis();
		Date currDate = new Date(currTime);
		Date prevDate = new Date(currTime - THIRTY_DAYS);
		return getStatistics(prevDate, currDate);
	}

	// testing
	public Map<String, Object> getStatistics(Date startDate, Date endDate) {
		Map<String, Object> result = createMap();
		// create value strings for current date and 30-days previous
		String lowerDate = new StringBuilder(SIMPLE_DATE.format(startDate))
				.toString();
		String upperDate = new StringBuilder(SIMPLE_DATE.format(endDate))
				.toString();

		result.put(STAT_TIME_STAMP, System.currentTimeMillis());
		result.put(STAT_NUM_ENTRIES, numEntries(lowerDate, upperDate));
		return result;
	}

	// required for spring to work
	public MessageSearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(MessageSearchService searchService) {
		this.searchService = searchService;
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsMsgSearchGatherer msgEntries = new StatsMsgSearchGatherer("name", "0/5 * * * ?");
		System.out.println(msgEntries.getStatistics());
		String[] keys = {};
		System.out.println(msgEntries.getStatistics(keys));
	}
}