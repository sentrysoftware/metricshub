package com.sentrysoftware.matrix.engine.strategy.discovery;

import java.util.Comparator;
import java.util.Objects;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

public class EnclosureFirstComparator implements Comparator<Connector> {

	@Override
	public int compare(final Connector connector1, final Connector connector2) {

		if (checkConnector(connector1) && checkConnector(connector2)) {
			// compiledFilename can never be null
			return connector1.getCompiledFilename().compareTo(connector2.getCompiledFilename());
		}

		// If the HardwareMonitor is Enclosure then it is first
		if (checkConnector(connector1)) {
			return -1;
		}

		return 1;
	}

	private boolean checkConnector(final Connector connector) {
		return connector != null  && connector.getHardwareMonitors() != null && !connector.getHardwareMonitors().isEmpty() && connector
				.getHardwareMonitors().stream()
				.anyMatch(job -> MonitorType.ENCLOSURE.equals(job.getType())
						&& Objects.nonNull(job.getDiscovery()));
	}

}