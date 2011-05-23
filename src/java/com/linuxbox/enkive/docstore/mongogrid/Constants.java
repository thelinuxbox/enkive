package com.linuxbox.enkive.docstore.mongogrid;

import java.nio.charset.Charset;

import com.linuxbox.util.mongodb.MongoDBConstants;

public interface Constants extends MongoDBConstants {
	Charset PREFERRED_CHARSET = Charset.forName("UTF-8");

	// file control
	String CONT_FILE_COLLECTION = "fileControl";
	String CONT_FILE_IDENTIFIER_KEY = "identifier";
	String CONT_FILE_TIMESTAMP_KEY = "timestamp";

	// our metadata fields
	String BINARY_ENCODING_KEY = "binaryEncoding";
	String FILE_EXTENSION_KEY = "fileExtension";
	String INDEX_STATUS_KEY = "indexStatus";
	String INDEX_TIMESTAMP_KEY = "indexTimestamp";
	String INDEX_SHARD_KEY = "indexShardKey";

	// our metadata fields with dot notation for queries
	String BINARY_ENCODING_QUERY = METADATA_KEY + "." + BINARY_ENCODING_KEY;
	String FILE_EXTENSION_QUERY = METADATA_KEY + "." + FILE_EXTENSION_KEY;
	String INDEX_STATUS_QUERY = METADATA_KEY + "." + INDEX_STATUS_KEY;
	String INDEX_TIMESTAMP_QUERY = METADATA_KEY + "." + INDEX_TIMESTAMP_KEY;
	String INDEX_SHARD_QUERY = METADATA_KEY + "." + INDEX_SHARD_KEY;
}
