package com.sentrysoftware.matrix.connector.parser.state.instance;

import java.util.Optional;
import java.util.regex.Matcher;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public abstract class AbstractInstanceProcessor implements IConnectorStateParser {

	protected abstract Matcher getMatcher(final String key);

	@Override
	public boolean detect(String key, String value, Connector connector) {
		return key != null && value != null && getMatcher(key).matches();
	}

	/**
	 * Get the {@link HardwareMonitor} instance for the given hdf key, the
	 * HardwareMonitor is mandatory as it is the object that we are going to update
	 * in this processor. It wraps the discovery, collect, sources / computes and the instanceTable
	 * 
	 * @param key
	 * @param connector
	 * @return {@link HardwareMonitor}
	 */
	protected HardwareMonitor getHardwareMonitor(final String key, final Connector connector) {

		final Matcher matcher = getMatcher(key);

		matcher.find();

		final String monitorName = matcher.group(1);

		final Optional<HardwareMonitor> hardwareMonitorOpt = connector
				.getHardwareMonitors()
				.stream()
				.filter(hm -> hm
						.getType()
						.getName()
						.equalsIgnoreCase(monitorName))
				.findFirst();

		// If the hardwareMonitor exists then that's good! we just return the instance directly
		// otherwise it means it is not created yet, in that case we create the HardwareMonitor
		// instance in the connector the we return the object
		return hardwareMonitorOpt.isPresent() ? hardwareMonitorOpt.get()
				: createHardwareMonitor(monitorName, connector);
	}

	/**
	 * Create a {@link HardwareMonitor} for the given monitor name
	 * @param monitorName
	 * @param connector
	 * @return {@link HardwareMonitor} instance
	 */
	protected HardwareMonitor createHardwareMonitor(final String monitorName, final Connector connector) {

		final MonitorType monitorType = MonitorType.getByName(monitorName);

		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.discovery(Discovery.builder().build())
				.type(monitorType)
				.build();

		// Set the hardware monitor in the connector
		connector.getHardwareMonitors().add(hardwareMonitor);

		return hardwareMonitor;
	}
}
