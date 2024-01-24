package org.sentrysoftware.metricshub.engine.strategy.utils;

import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;

/**
 * The {@code DetectionHelper} class provides utility methods for detecting whether a given connector has at least one tag
 * that matches a set of included tags. It is used to check if a connector should be included based on user-defined tags.
 * The class is designed to have a private no-argument constructor to prevent instantiation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DetectionHelper {

	/**
	 * Checks whether the given includeConnectorTags set defined in HostConfiguration or defined using the CLI contains at least one of a given connector's tags
	 * @param includeConnectorTags tags defined by the user and stored in HostConfiguration
	 * @param connector a given connector
	 * @return boolean
	 */
	public static boolean hasAtLeastOneTagOf(final Set<String> includeConnectorTags, final Connector connector) {
		if (includeConnectorTags == null || includeConnectorTags.isEmpty()) {
			return true;
		}
		final Set<String> connectorTags = connector.getConnectorIdentity().getDetection().getTags();
		if (connectorTags == null) {
			return false;
		}
		return connectorTags.stream().anyMatch(tag -> includeConnectorTags.contains(tag));
	}
}
