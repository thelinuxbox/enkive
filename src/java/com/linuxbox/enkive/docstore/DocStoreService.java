package com.linuxbox.enkive.docstore;

import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exception.StorageException;

public interface DocStoreService {
	/**
	 * Shut down the service and release any resources used by the service.
	 */
	void shutdown();

	/**
	 * Stores the given document and generates a unique identifier for the
	 * document, which is returned. If the document is already stored, it is not
	 * stored a second time, but instead the existing identifier is returned.
	 * This inherently does de-duplication.
	 * 
	 * @param document
	 * @return unique identifier for document
	 * @throws StorageException
	 */
	StoreRequestResult store(Document document) throws DocStoreException;

	/**
	 * Retrieves a document given its unique identifier.
	 * 
	 * @param identifier
	 * @return
	 * @throws DocumentNotFoundException
	 * @throws DocStoreException
	 */
	Document retrieve(String identifier) throws DocStoreException;

	/**
	 * Removes the specified document.
	 * 
	 * @param identifier
	 * @return true if the file was found and removed, false if the file was not
	 *         found, or throws an exception if there was an issue (for which a
	 *         retry might work)
	 * @throws DocStoreException
	 */
	boolean remove(String identifier) throws DocStoreException;

	/**
	 * The given document perhaps cannot be removed because another thread is
	 * controlling it (e.g., creating it). An exception is thrown, and this will
	 * retry a few times after waiting the specified time.
	 * 
	 * @param identifier
	 * @param numberOfAttempts
	 * @param millisecondsBetweenAttempts
	 * @return
	 */
	boolean removeWithRetries(String identifier, int numberOfAttempts,
			int millisecondsBetweenAttempts) throws DocStoreException;

	/**
	 * Retrieve the (earliest) un-indexed document. May mark the document as
	 * being in the process of being indexed, which is different than having
	 * been indexed.
	 * 
	 * @return The identifier of a document that's not been indexed.
	 */
	String nextUnindexed();

	/**
	 * Marks the given document as having been indexed, so it will not be
	 * retrieved as un-indexed again.
	 * 
	 * @param identifier
	 * @throws DocSearchException
	 */
	void markAsIndexed(String identifier) throws DocSearchException;
}
