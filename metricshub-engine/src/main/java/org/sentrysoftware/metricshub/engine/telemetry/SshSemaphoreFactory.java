package org.sentrysoftware.metricshub.engine.telemetry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * A factory class for managing SSH semaphores associated with hostnames.
 */
public class SshSemaphoreFactory {

	private static final int MAX_PERMITS = 8;

	private static final SshSemaphoreFactory INSTANCE = new SshSemaphoreFactory();

	private final Map<String, Semaphore> sshSemaphores = new HashMap<>();

	/**
	 * Gets the Semaphore associated with the hostname, creating it if it does not exist.
	 *
	 * @param hostname The hostname for which to retrieve the Semaphore.
	 * @return The Semaphore associated with the hostname.
	 */
	public synchronized Semaphore createOrGetSempahore(final String hostname) {
		sshSemaphores.putIfAbsent(hostname, new Semaphore(MAX_PERMITS));
		return sshSemaphores.get(hostname);
	}

	/**
	 * Gets the static instance containing all semaphores
	 *
	 * @return the static instance containing all semaphores
	 */
	public static SshSemaphoreFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Removes the host-semaphore key-value pair for the specified hostname.
	 *
	 * @param hostname The hostname for which to delete the Semaphore.
	 */
	public void deleteSemaphore(String hostname) {
		sshSemaphores.remove(hostname);
	}
}
