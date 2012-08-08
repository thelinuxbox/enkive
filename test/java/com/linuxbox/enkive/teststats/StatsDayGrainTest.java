package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererInterface;
import com.linuxbox.enkive.statistics.gathering.StatsMsgSearchGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoGathererMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoMsgGatherer;
import com.linuxbox.enkive.statistics.granularity.DayGrain;
import com.linuxbox.enkive.statistics.granularity.HourGrain;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsRetrievalService;
import com.linuxbox.enkive.statistics.services.storage.mongodb.MongoStatsStorageService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsDayGrainTest {

	private static MongoStatsRetrievalService retrievalTester;
	private static MongoStatsStorageService storageTester;
	private static StatsGathererService gatherTester;
	private static StatsClient client;
	private static DayGrain grain;
	private static Mongo m;
	private static DB db;
	private static DBCollection coll;
	private static long dataCount;

	@BeforeClass
	public static void setUp() throws ParseException, GathererException {
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (MongoException e) {
			e.printStackTrace();
			System.exit(0);
		}
		db = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
		coll = db.getCollection(STAT_STORAGE_COLLECTION);

		List<String> keys = new LinkedList<String>();
		keys.add("db:");
		keys.add("numObj:avg,max,min");
		keys.add("nColls:avg,max,min");
		keys.add("avgOSz:avg,max,min");
		keys.add("dataSz:avg,max,min");
		keys.add("totSz:avg,max,min");
		keys.add("numInd:avg,max,min");
		keys.add("indSz:avg,max,min");
		keys.add("numExt:avg,max,min");
		keys.add("fileSz:avg,max,min");
		GathererInterface dbProp = new StatsMongoDBGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE, "DBGatherer",
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("*.ns:");
		keys.add("*.numObj:avg,max,min");
		keys.add("*.avgOSz:avg,max,min");
		keys.add("*.dataSz:avg,max,min");
		keys.add("*.totSz:avg,max,min");
		keys.add("*.numExt:avg,max,min");
		keys.add("*.lExSz:avg,max,min");
		keys.add("*.numInd:avg,max,min");
		keys.add("*.indSz:avg,max,min");
		keys.add("*.indSzs.*:avg,max,min");
		GathererInterface collProp = new StatsMongoCollectionGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE, "CollGatherer",
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("freeM:avg,max,min");
		keys.add("maxM:avg,max,min");
		keys.add("totM:avg,max,min");
		keys.add("cores:avg,max,min");
		GathererInterface runProp = new StatsRuntimeGatherer("RuntimeGatherer",
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("numMsg:avg,max,min");
		StatsMsgSearchGatherer msgProp = new StatsMsgSearchGatherer(
				"MsgPropGatherer", "* * * * * ?", keys);
		MongoGathererMessageSearchService searchService = null;
		try {
			searchService = new MongoGathererMessageSearchService(new Mongo(),
					TestingConstants.MONGODB_TEST_DATABASE,
					TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		searchService.setDocSearchService(new IndriDocSearchQueryService());
		msgProp.setSearchService(searchService);
		
		keys = new LinkedList<String>();
		keys.add("avgAtt:avg");
		keys.add("maxAtt:max");
		StatsMongoAttachmentsGatherer attProp = new StatsMongoAttachmentsGatherer(
				m, TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_DOCUMENTS_COLLECTION,
				"AttachmentGatherer", "* * * * * ?", false, keys);
		attProp.setLowerDate(new Date(0L));
		attProp.setUpperDate(new Date());
		
		keys = new LinkedList<String>();
		keys.add("msgArchive:avg,max,min");
		GathererInterface msgStatProp = new StatsMongoMsgGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION,
				"MsgStatGatherer", "* * * * * ?", keys);

		HashMap<String, GathererInterface> gatherers = new HashMap<String, GathererInterface>();
		gatherers.put("DatabaseStatsService", dbProp);
		gatherers.put("CollStatsService", collProp);
		gatherers.put("RuntimeStatsService", runProp);
		gatherers.put("MsgEntriesStatsService", msgProp);
		gatherers.put("AttachstatsService", attProp);
		gatherers.put("msgStatStatsService", msgStatProp);
		retrievalTester = new MongoStatsRetrievalService(m,
				TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_COLL);
		storageTester = new MongoStatsStorageService(m,
				TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_COLL);
		gatherTester = new StatsGathererService(gatherers);
		client = new StatsClient(gatherTester, storageTester, retrievalTester);
		grain = new DayGrain(client);

		// TODO
		Set<Map<String, Object>> stats = (new HourGrain(client))
				.consolidateData();
		Map<String, Object> timeMap = new HashMap<String, Object>();
		for (int i = 0; i < 10; i++) {
			Calendar cal = Calendar.getInstance();
			if (i < 5) {
				cal.add(Calendar.DATE, -1);
			}
			timeMap.put(GRAIN_MAX, cal.getTime());
			timeMap.put(GRAIN_MIN, cal.getTime());
			for (Map<String, Object> data : stats) {
				data.put(STAT_TIME_STAMP, timeMap);
			}
			client.storeData(stats);
		}
		dataCount = coll.count();
	}

	@Test
	public void correctQueryTest() {
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			int size = grain.gathererFilter(name).size();
			assertTrue(
					"the query did not return the correct number of objects: 5 vs. "
							+ size, size == 5);
		}
	}

	@Test
	public void noDeletedDataTest() {
		long count = coll.count();
		assertTrue("data was deleted by consolidatation: dataCount before: "
				+ dataCount + "dataCount now: " + count, count >= dataCount);
	}

	@Test
	public void consolidationMethods() {
		Set<Map<String, Object>> consolidatedData = grain.consolidateData();
		assertTrue("the consolidated data is null", consolidatedData != null);
		String methods[] = { GRAIN_AVG, GRAIN_MAX, GRAIN_MIN };
		Object exampleData = new Integer(10);
		DescriptiveStatistics statsMaker = new DescriptiveStatistics();
		statsMaker.addValue(111);
		statsMaker.addValue(11);
		statsMaker.addValue(1);
		Map<String, Object> statData = new HashMap<String, Object>();
		for (String method : methods) {
			grain.methodMapBuilder(method, exampleData, statsMaker, statData);
		}
		assertTrue("methodMapBuilder returned null", statData != null);
	}
}
