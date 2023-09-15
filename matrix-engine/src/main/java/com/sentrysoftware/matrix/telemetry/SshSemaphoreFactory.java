package com.sentrysoftware.matrix.telemetry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class SshSemaphoreFactory {

	private static final int MAX_PERMITS = 8;

	private static final SshSemaphoreFactory INSTANCE = new SshSemaphoreFactory();

	private final Map<String, Semaphore> sshSemaphores = new HashMap<>();

	/**
	 * Gets the Semaphore associated to the hostname, or creates it if it does not exist
	 *
	 * @param hostname
	 * @return the Semaphore associated to the hostname
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
	 * Removes the host-semaphore key-value pair of the specified hostname
	 *
	 * @param hostname
	 */
	public void deleteSemaphore(String hostname) {
		sshSemaphores.remove(hostname);
	}
}
