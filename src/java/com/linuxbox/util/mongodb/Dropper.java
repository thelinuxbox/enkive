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
package com.linuxbox.util.mongodb;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class Dropper {
	public static void dropDatabase(String dbName) throws UnknownHostException,
			MongoException {
		Mongo m = new Mongo();
		DB db = m.getDB(dbName);
		db.dropDatabase();
	}

	public static void dropCollection(String dbName, String collection)
			throws UnknownHostException, MongoException {
		Mongo m = new Mongo();
		DB db = m.getDB(dbName);
		DBCollection c = db.getCollection(collection);
		c.drop();
	}
}
