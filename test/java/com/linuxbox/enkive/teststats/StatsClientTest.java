package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererInterface;
import com.linuxbox.enkive.statistics.gathering.StatsMsgSearchGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoGathererMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoMsgGatherer;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsRetrievalService;
import com.linuxbox.enkive.statistics.services.storage.mongodb.MongoStatsStorageService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsClientTest {

	private static MongoStatsRetrievalService retrievalTester;
	private static MongoStatsStorageService storageTester;
	private static StatsGathererService gatherTester;
	private static StatsClient client;
	private static Mongo m;
	private static DB db;
	private static DBCollection coll;
	private static final String dbPropName = "dbGatherer";
	private static final String collPropName = "collGatherer";
	private static final String runPropName = "rtGatherer";
	private static final String msgSearchPropName = "msgSearchGatherer";
	private static final String attPropName = "attGatherer";
	private static final String msgStatPropName = "msgGatherer";

	@BeforeClass
	public static void setUp() throws ParseException, GathererException {
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		db = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
		coll.drop();

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
				TestingConstants.MONGODB_TEST_DATABASE, dbPropName,
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
				TestingConstants.MONGODB_TEST_DATABASE, collPropName,
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("freeM:avg,max,min");
		keys.add("maxM:avg,max,min");
		keys.add("totM:avg,max,min");
		keys.add("cores:avg,max,min");
		GathererInterface runProp = new StatsRuntimeGatherer(runPropName,
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("numMsg:avg,max,min");
		StatsMsgSearchGatherer msgProp = new StatsMsgSearchGatherer(
				msgSearchPropName, "* * * * * ?", keys);
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
				attPropName, "* * * * * ?", false, keys);
		attProp.setLowerDate(new Date(0L));
		attProp.setUpperDate(new Date());
		
		keys = new LinkedList<String>();
		keys.add("msgArchive:avg,max,min");
		GathererInterface msgStatProp = new StatsMongoMsgGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION,
				msgStatPropName, "* * * * * ?", keys);

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
	}

	@Test
	public void correctNumGatherers() {
		int size = client.gathererNames().size();
		assertTrue("the number of gatherers is incorrect: " + size, size == 6);
	}

	@Test
	public void correctNamedGatherers() {
		for (String name : client.gathererNames()) {
			assertTrue("name is not valid: " + name, name == dbPropName
					|| name == collPropName || name == runPropName
					|| name == msgSearchPropName || name == attPropName
					|| name == msgStatPropName);
		}
	}
}
