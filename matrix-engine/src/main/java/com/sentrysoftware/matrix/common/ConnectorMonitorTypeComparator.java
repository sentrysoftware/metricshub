package com.sentrysoftware.matrix.common;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.connector.model.Connector;

import java.util.Comparator;

public class ConnectorMonitorTypeComparator implements Comparator<Connector> {

	@Override
	public int compare(Connector firstConnector, Connector secondConnector) {
		if (firstConnector.getMonitors().get(KnownMonitorType.HOST.getKey()) != null &&
				secondConnector.getMonitors().get(KnownMonitorType.ENCLOSURE.getKey()) != null) {
			return 1;
		} else if (firstConnector.getMonitors().get(KnownMonitorType.ENCLOSURE.getKey()) != null &&
				secondConnector.getMonitors().get(KnownMonitorType.HOST.getKey()) != null) {
			return -1;
		} else {
			return firstConnector.getConnectorIdentity().getCompiledFilename().compareTo(secondConnector.getConnectorIdentity().getCompiledFilename());
		}
	}
}
