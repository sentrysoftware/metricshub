<<<<<<< Updated upstream:metricshub-engine/src/main/java/org/sentrysoftware/metricshub/engine/common/HostLocation.java
package org.sentrysoftware.metricshub.engine.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HostLocation {
	LOCAL("local"),
	REMOTE("remote");

	private String key;
}
=======
package com.sentrysoftware.metricshub.engine.common;

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
>>>>>>> Stashed changes:metricshub-engine/src/main/java/com/sentrysoftware/metricshub/engine/common/HostLocation.java
