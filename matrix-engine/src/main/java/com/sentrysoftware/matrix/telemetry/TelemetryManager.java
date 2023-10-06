package com.sentrysoftware.matrix.telemetry;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.IWinConfiguration;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelemetryManager {

	@Default
	private Map<String, Map<String, Monitor>> monitors = new HashMap<>();

	@Default
	private HostProperties hostProperties = new HostProperties();

	private HostConfiguration hostConfiguration;
	private ConnectorStore connectorStore;
	private Long strategyTime;

	public synchronized void run() {
		// Implement the code here
	}

	/**
	 * Get the protocol configuration used to execute requests on Windows machines.
	 * (WinRM or WMI)<br> WinRM is prioritized.
	 *
	 * @return {@link com.sentrysoftware.matrix.configuration.IWinConfiguration} instance.
	 */
	public IWinConfiguration getWinConfiguration() {
		return hostConfiguration.getWinConfiguration();
	}

	/**
	 * Finds a monitor using its type and its id attribute
	 * @param type
	 * @param id
	 * @return Monitor instance
	 */
	public Monitor findMonitorByTypeAndId(final String type, final String id) {
		final Map<String, Monitor> findMonitorByTypeResult = findMonitorByType(type);
		if (findMonitorByTypeResult != null) {
			return findMonitorById(id, findMonitorByTypeResult);
		}
		return null;
	}

	/**
	 * Finds a monitor using its id attribute
	 * @param id
	 * @param monitorsMap
	 * @return Monitor instance
	 */
	public Monitor findMonitorById(final String id, final Map<String, Monitor> monitorsMap) {
		return monitorsMap.get(id);
	}

	/**
	 * Finds a monitor using its type
	 *
	 * @param type
	 * @return Monitor instance
	 */
	public Map<String, Monitor> findMonitorByType(final String type) {
		return this.getMonitors() == null ? null : this.getMonitors().get(type);
	}

	/**
	 * Add a new monitor instance
	 *
	 * @param monitor     Monitor instance we wish to add
	 * @param monitorType The type of the monitor
	 * @param id          The monitor's identifier
	 * @return added {@link Monitor} instance
	 */
	public Monitor addNewMonitor(
		@NonNull final Monitor monitor,
		@NonNull final String monitorType,
		@NonNull final String id
	) {
		synchronized (monitors) {
			monitors.computeIfAbsent(monitorType, t -> new HashMap<>()).put(id, monitor);
			return monitor;
		}
	}
}
