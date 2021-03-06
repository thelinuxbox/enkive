/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.linuxbox.util.queueservice.mongodb;

import static com.linuxbox.util.mongodb.MongoDbConstants.CALL_ENSURE_INDEX_ON_INIT;
import static com.linuxbox.util.mongodb.MongoDbConstants.OBJECT_ID_KEY;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.BSONTimestamp;
import org.bson.types.ObjectId;

import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.linuxbox.util.mongodb.MongoIndexable;
import com.linuxbox.util.mongodb.UpdateFieldBuilder;
import com.linuxbox.util.queueservice.AbstractQueueEntry;
import com.linuxbox.util.queueservice.QueueEntry;
import com.linuxbox.util.queueservice.QueueService;
import com.linuxbox.util.queueservice.QueueServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class MongoQueueService implements QueueService, MongoIndexable {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.util.queueservice.mongodb");

	private static final String STATUS_FIELD = "status";
	private static final String CREATED_AT_FIELD = "createdAt";
	private static final String DEQUEUED_AT_FIELD = "dequeuedAt";
	private static final String ERRORED_AT_FIELD = "erroredAt";
	private static final String IDENTIFIER_FIELD = "identifier";
	private static final String SHARD_KEY_FIELD = "shard";
	private static final String NOTE_FIELD = "note";

	private static final int STATUS_ENQUEUED = 1;
	private static final int STATUS_DEQUEUED = 2;
	@SuppressWarnings("unused")
	private static final int STATUS_COMPLETE = 3;
	private static final int STATUS_ERROR = 4;

	// Some standard DBObjects used for queries; only create them once and
	// re-use.

	private static final DBObject QUERY_ENQUEUED_STATUS = new BasicDBObject(
			STATUS_FIELD, STATUS_ENQUEUED);
	private static final DBObject DEQUEUE_FIELDS = new BasicDBObject(
			OBJECT_ID_KEY, 1).append(IDENTIFIER_FIELD, 1).append(NOTE_FIELD, 1)
			.append(CREATED_AT_FIELD, 1).append(SHARD_KEY_FIELD, 1);
	private static final DBObject SORT_BY_CREATED_AT = new BasicDBObject(
			CREATED_AT_FIELD, 1);

	private Mongo mongo;
	private DBCollection queueCollection;

	/**
	 * Keep track of whether we created the instance of Mongo, as if so, we'll
	 * have to close it.
	 */
	private boolean createdMongo;

	public MongoQueueService(String server, int port, String database,
			String collection) throws UnknownHostException, MongoException {
		this(new Mongo(server, port), database, collection);
		createdMongo = true;
	}

	public MongoQueueService(String database, String collection)
			throws UnknownHostException, MongoException {
		this(new Mongo(), database, collection);
		createdMongo = true;
	}

	public MongoQueueService(Mongo mongo, String database, String collection) {
		this(mongo, mongo.getDB(database).getCollection(collection));
	}

	public MongoQueueService(MongoDbInfo dbInfo) {
		this(dbInfo.getMongo(), dbInfo.getCollection());
	}

	public MongoQueueService(Mongo mongo, DBCollection collection) {
		this.mongo = mongo;
		this.queueCollection = collection;

		// see comments on def'n of CALL_ENSURE_INDEX_ON_INIT to see why it's
		// done conditionally
		if (CALL_ENSURE_INDEX_ON_INIT) {
			// see class com.linuxbox.enkive.MongoDBIndexManager
		}

		// Make sure data is written out to disk before operation is complete.
		queueCollection.setWriteConcern(WriteConcern.FSYNC_SAFE);
	}

	@Override
	public void startup() throws QueueServiceException {
		// empty
	}

	@Override
	public void shutdown() throws QueueServiceException {
		if (createdMongo) {
			mongo.close();
		}
	}

	@Override
	public void enqueue(String identifier) throws QueueServiceException {
		enqueue(identifier, -1, null);
	}

	@Override
	public void enqueue(String identifier, int shardKey, Object note)
			throws QueueServiceException {
		final DBObject entry = new BasicDBObject(CREATED_AT_FIELD,
				new BSONTimestamp()).append(STATUS_FIELD, STATUS_ENQUEUED)
				.append(IDENTIFIER_FIELD, identifier)
				.append(SHARD_KEY_FIELD, shardKey).append(NOTE_FIELD, note);
		WriteResult result = queueCollection.insert(entry);
		if (!result.getLastError().ok()) {
			throw new QueueServiceException("could not enqueue \"" + identifier
					+ "\" (shard: \"" + shardKey + "\"; note: \""
					+ note.toString() + "\")", result.getLastError()
					.getException());
		}
	}

	@Override
	public QueueEntry dequeue() throws QueueServiceException {
		return dequeueHelper(QUERY_ENQUEUED_STATUS);
	}

	@Override
	public QueueEntry dequeue(String identifer) throws QueueServiceException {
		final BasicDBObject query = new BasicDBObject();
		query.putAll(QUERY_ENQUEUED_STATUS);
		query.append(IDENTIFIER_FIELD, identifer);
		return dequeueHelper(query);
	}

	@Override
	public QueueEntry dequeueByShardKey(int rangeLow, int rangeHigh)
			throws QueueServiceException {
		final QueryBuilder query = new QueryBuilder().and(STATUS_FIELD)
				.is(STATUS_ENQUEUED).and(SHARD_KEY_FIELD)
				.greaterThanEquals(rangeLow).and(SHARD_KEY_FIELD)
				.lessThan(rangeHigh);
		return dequeueHelper(query.get());
	}

	private QueueEntry dequeueHelper(DBObject query) {
		final DBObject result = queueCollection.findAndModify(query,
				DEQUEUE_FIELDS, SORT_BY_CREATED_AT, false, updateToDequeued(),
				false, false);

		if (result == null) {
			return null;
		}

		final BSONTimestamp createdAtBSON = (BSONTimestamp) result
				.get(CREATED_AT_FIELD);
		final Date createdAt = new Date(createdAtBSON.getTime() * 1000L);

		return new MongoQueueEntry((ObjectId) result.get(OBJECT_ID_KEY),
				createdAt, (String) result.get(IDENTIFIER_FIELD),
				result.get(NOTE_FIELD), (Integer) result.get(SHARD_KEY_FIELD));
	}

	@Override
	public void finishEntry(QueueEntry entry) throws QueueServiceException {
		if (entry instanceof MongoQueueEntry) {
			final MongoQueueEntry mongoEntry = (MongoQueueEntry) entry;
			final DBObject query = new BasicDBObject(OBJECT_ID_KEY,
					mongoEntry.mongoID);
			final DBObject result = queueCollection.findAndRemove(query);
			if (result == null) {
				throw new QueueServiceException(
						"could not finish queue entry \""
								+ entry.getIdentifier() + "\" "
								+ entry.getEnqueuedAt());
			}
		} else {
			throw new QueueServiceException(
					"tried to finish a queue entry that was not generated by this queue");
		}
	}

	@Override
	public void markEntryAsError(QueueEntry entry) throws QueueServiceException {
		if (entry instanceof MongoQueueEntry) {
			final MongoQueueEntry mongoEntry = (MongoQueueEntry) entry;
			final DBObject query = new BasicDBObject(OBJECT_ID_KEY,
					mongoEntry.mongoID);
			final DBObject result = queueCollection.findAndModify(query,
					updateToError());
			if (result == null) {
				throw new QueueServiceException(
						"could not finish queue entry \""
								+ entry.getIdentifier() + "\" "
								+ entry.getEnqueuedAt());
			}
		} else {
			throw new QueueServiceException(
					"tried to mark as error a queue entry that was not generated by this queue");
		}
	}

	static class MongoQueueEntry extends AbstractQueueEntry {
		private ObjectId mongoID;

		public MongoQueueEntry(ObjectId mongoID, Date enqueuedAt,
				String identifier, Object note, Integer shardKey) {
			super(enqueuedAt, identifier, note, shardKey);
			this.mongoID = mongoID;
		}
	}

	@Override
	public List<DBObject> getIndexInfo() {
		return queueCollection.getIndexInfo();
	}

	@Override
	public List<IndexDescription> getPreferredIndexes() {
		LinkedList<IndexDescription> result = new LinkedList<IndexDescription>();

		// For MongoDB multi-key indexes to work the most efficiently, the
		// queried fields should appear before the sorting fields in the
		// indexes.

		// This index is used for finding the earliest entry with a given
		// status.
		final DBObject statusTimestampIndex = new BasicDBObject(STATUS_FIELD, 1)
				.append(CREATED_AT_FIELD, 1);
		IndexDescription id1 = new IndexDescription("statusTimestampIndex",
				statusTimestampIndex, false);
		result.add(id1);

		// This index is used for finding the earliest entry with a given
		// status
		// and identifier.
		final DBObject statusIdentifierTimestampIndex = new BasicDBObject(
				STATUS_FIELD, 1).append(IDENTIFIER_FIELD, 1).append(
				CREATED_AT_FIELD, 1);
		IndexDescription id2 = new IndexDescription(
				"statusIdentifierTimestampIndex",
				statusIdentifierTimestampIndex, false);
		result.add(id2);

		return result;
	}

	@Override
	public void ensureIndex(DBObject index, DBObject options)
			throws MongoException {
		queueCollection.ensureIndex(index, options);
	}

	@Override
	public long getDocumentCount() throws MongoException {
		return queueCollection.count();
	}

	private static DBObject updateToDequeued() {
		return new UpdateFieldBuilder().set(DEQUEUED_AT_FIELD, new Date())
				.set(STATUS_FIELD, STATUS_DEQUEUED).get();
	}

	private static DBObject updateToError() {
		return new UpdateFieldBuilder().set(ERRORED_AT_FIELD, new Date())
				.set(STATUS_FIELD, STATUS_ERROR).unset(DEQUEUED_AT_FIELD).get();
	}
}
