package com.sentrysoftware.matrix.telemetry;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.IWinConfiguration;
import com.sentrysoftware.matrix.configuration.WinRmConfiguration;
import com.sentrysoftware.matrix.configuration.WmiConfiguration;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelemetryManager {

	@Default
	private Map<String, Map<String, Monitor>> monitors = new HashMap<>();
	private HostProperties hostProperties;
	private HostConfiguration hostConfiguration;
	private ConnectorStore connectorStore;

	public synchronized void run(){
		// Implement the code here 
	}

	/**
	 * Get the protocol configuration used to execute requests on Windows machines.
	 *  (WinRM or WMI)<br> WinRM is prioritized.
	 *
	 * @return {@link com.sentrysoftware.matrix.configuration.IWinConfiguration} instance.
	 */
	public IWinConfiguration getWinConfiguration() {
		// We prioritize WinRM over WMI as it's more efficient.
		final IWinConfiguration winConfiguration = (WinRmConfiguration) this.getHostConfiguration().getConfigurations().get(WinRmConfiguration.class);

		// Let's try WMI if the WinRM is not available
		if (winConfiguration == null) {
			return (WmiConfiguration) this.getHostConfiguration().getConfigurations().get(WmiConfiguration.class);
		}

		return winConfiguration;
	}

}
