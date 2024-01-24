package org.sentrysoftware.metricshub.engine.common;

import java.util.Comparator;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;

/**
 * Comparator for sorting {@link Connector} instances based on the monitor type.
 * <p>
 * This comparator prioritizes connectors with monitor types HOST and ENCLOSURE.
 * Connectors with these monitor types will be ordered first, and then the remaining connectors
 * will be sorted based on their compiled filenames.
 * </p>
 */
public class ConnectorMonitorTypeComparator implements Comparator<Connector> {

	@Override
	public int compare(Connector firstConnector, Connector secondConnector) {
		if (
			firstConnector.getMonitors().get(KnownMonitorType.HOST.getKey()) != null &&
			secondConnector.getMonitors().get(KnownMonitorType.ENCLOSURE.getKey()) != null
		) {
			return 1;
		} else if (
			firstConnector.getMonitors().get(KnownMonitorType.ENCLOSURE.getKey()) != null &&
			secondConnector.getMonitors().get(KnownMonitorType.HOST.getKey()) != null
		) {
			return -1;
		} else {
			return firstConnector
				.getConnectorIdentity()
				.getCompiledFilename()
				.compareTo(secondConnector.getConnectorIdentity().getCompiledFilename());
		}
	}
}
