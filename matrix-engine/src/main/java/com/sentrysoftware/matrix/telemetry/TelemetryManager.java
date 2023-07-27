package com.sentrysoftware.matrix.telemetry;

import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.monitor.Monitor;
import com.sentrysoftware.matrix.engine.configuration.HostConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelemetryManager {

	private Map<String, Map<String, Monitor>> monitors;
	private HostProperties hostProperties;
	private HostConfiguration hostConfiguration;
	private ConnectorStore connectorStore;

	public synchronized void run(){
		// TODO
	}

}
