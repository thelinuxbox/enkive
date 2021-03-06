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
package com.linuxbox.enkive.workspace.searchResult;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;

public abstract class SearchResult {

	public enum Status {
		QUEUED,

		RUNNING,

		COMPLETE,

		CANCEL_REQUESTED,

		CANCELED,

		ERROR,

		UNKNOWN; // when the status was read from DB, did not understand
	}

	protected SearchQueryBuilder searchQueryBuilder;

	private String id;
	private Date timestamp;
	protected String executedBy;
	private Set<String> messageIds;
	private Status status;
	protected String searchQueryId;
	protected Boolean isSaved = false;
	protected SearchQueryBuilder queryBuilder;

	public static String SORTBYDATE = "sortByDate";
	public static String SORTBYNAME = "sortByName";
	public static String SORTBYSUBJECT = "sortBySubject";
	public static String SORTBYSENDER = "sortBySender";
	public static String SORTBYRECEIVER = "sortByReceiver";
	public static String SORTBYSTATUS = "sortByStatus";

	public static int SORT_ASC = 1;
	public static int SORT_DESC = -1;

	public SearchResult() {
		this.timestamp = new Date();
		this.status = Status.RUNNING;
		messageIds = new HashSet<String>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Set<String> getMessageIds() {
		return messageIds;
	}

	public void setMessageIds(Set<String> messageIds) {
		this.messageIds = messageIds;
	}

	public String getSearchQueryId() {
		return searchQueryId;
	}

	public void setSearchQueryId(String searchQueryId) {
		this.searchQueryId = searchQueryId;
	}

	public String getExecutedBy() {
		return executedBy;
	}

	public void setExecutedBy(String executedBy) {
		this.executedBy = executedBy;
	}

	public Boolean isSaved() {
		return isSaved;
	}

	public void setSaved(Boolean isSaved) {
		this.isSaved = isSaved;
	}

	public SearchQuery getSearchQuery() throws WorkspaceException {
		return searchQueryBuilder.getSearchQuery(getSearchQueryId());
	}

	public abstract void saveSearchResult() throws WorkspaceException;

	public abstract void deleteSearchResult() throws WorkspaceException;

	public SearchQueryBuilder getSearchQueryBuilder() {
		return searchQueryBuilder;
	}

	public void setSearchQueryBuilder(SearchQueryBuilder searchQueryBuilder) {
		this.searchQueryBuilder = searchQueryBuilder;
	}

	public abstract void sortSearchResultMessages(String sortBy, int sortDir)
			throws WorkspaceException;
}
