package com.sentrysoftware.matrix.engine.strategy.discovery;

import java.util.Comparator;
import java.util.Objects;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE;

public class EnclosureFirstComparator implements Comparator<Connector> {

	@Override
	public int compare(final Connector connector1, final Connector connector2) {

		if (checkConnectorWithEnclosureComputer(connector1) && checkConnectorWithEnclosureComputer(connector2)) {
			// compiledFilename can never be null
			return compareNames(connector1, connector2);
		}

		// If the HardwareMonitor is a Computer Enclosure then it is first
		if (checkConnectorWithEnclosureComputer(connector1)) {
			return -1;
		}

		if (checkConnectorWithEnclosure(connector1) && checkConnectorWithEnclosure(connector2)) {
			// compiledFilename can never be null
			return compareNames(connector1, connector2);
		}

		// If the HardwareMonitor is Enclosure then it is first
		if (checkConnectorWithEnclosure(connector1)) {
			return -1;
		}

		return 1;
	}

	/**
	 * Compare connectors names
	 * 
	 * @param connector1
	 * @param connector2
	 * @return The value 0 if the the two connector names are equals; a value less than 0 if the connector1 name is lexicographically less than
	 *         the connector2 name; and a value greater than 0 if the connector1 name is lexicographically greater than the connector2 name.
	 */
	private static int compareNames(final Connector connector1, final Connector connector2) {
		return connector1.getCompiledFilename().compareTo(connector2.getCompiledFilename());
	}

	/**
	 * Check if the connector has a hardware monitor of type enclosure
	 * 
	 * @param connector
	 * @return <code>true</code> if the given connector contains an Enclosure HardwareMonitor otherwise <code>false</code>.
	 */
	private static boolean checkConnectorWithEnclosure(final Connector connector) {
		return connector != null  && connector.getHardwareMonitors() != null && !connector.getHardwareMonitors().isEmpty() && connector
				.getHardwareMonitors().stream()
				.anyMatch(EnclosureFirstComparator::checkEnclosureHardwareMonitor);
	}

	/**
	 * Check the hardware monitor type
	 * 
	 * @param hardwareMonitor
	 * @return <code>true</code> if the given hardwareMonitor is an Enclosure otherwise <code>false</code>.
	 */
	private static boolean checkEnclosureHardwareMonitor(final HardwareMonitor hardwareMonitor) {
		return MonitorType.ENCLOSURE.equals(hardwareMonitor.getType()) && Objects.nonNull(hardwareMonitor.getDiscovery());
	}

	/**
	 * Check if the connector has a hardware monitor of type enclosure and extended type computer
	 * 
	 * @param hardwareMonitor
	 * @return <code>true</code> if the given hardwareMonitor is a Computer Enclosure otherwise <code>false</code>.
	 */
	private boolean checkConnectorWithEnclosureComputer(final Connector connector) {
		return connector != null  && connector.getHardwareMonitors() != null && !connector.getHardwareMonitors().isEmpty() && connector
				.getHardwareMonitors().stream()
				.anyMatch(EnclosureFirstComparator::checkEnclosureComputerJob);
	}

	/**
	 * Check the hardware monitor type
	 * 
	 * @param hardwareMonitor
	 * @return <code>true</code> if the given hardwareMonitor is an Enclosure of type Computer otherwise <code>false</code>.
	 */
	private static boolean checkEnclosureComputerJob(HardwareMonitor job) {
		return checkEnclosureHardwareMonitor(job) &&
				Objects.nonNull(job.getDiscovery().getParameters())
				&& COMPUTER.equalsIgnoreCase(job.getDiscovery().getParameters().get(TYPE));
	}

}