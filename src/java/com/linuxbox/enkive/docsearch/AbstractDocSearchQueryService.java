package com.linuxbox.enkive.docsearch;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.exception.DocSearchException;

public abstract class AbstractDocSearchQueryService implements
		DocSearchQueryService {
	@SuppressWarnings("unused")
	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.docsearch");

	/**
	 * The maximum search results to return by default.
	 */
	private static final int DEFAULT_MAX_SEARCH_RESULTS = 100;

	protected int maxSearchResults;

	public AbstractDocSearchQueryService() {
		maxSearchResults = DEFAULT_MAX_SEARCH_RESULTS;
	}

	@Override
	public List<String> search(String query) throws DocSearchException {
		return search(query, maxSearchResults);
	}

	public int getMaxSearchResults() {
		return maxSearchResults;
	}

	public void setMaxSearchResults(int maxSearchResults) {
		this.maxSearchResults = maxSearchResults;
	}
}
