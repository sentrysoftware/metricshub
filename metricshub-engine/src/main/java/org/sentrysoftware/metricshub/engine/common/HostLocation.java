package org.sentrysoftware.metricshub.engine.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration representing the location of a host, either local or remote.
 */
@Getter
@AllArgsConstructor
public enum HostLocation {
	/**
	 * Represents a local host.
	 */
	LOCAL("local"),
	/**
	 * Represents a remote host.
	 */
	REMOTE("remote");

	private String key;
}
