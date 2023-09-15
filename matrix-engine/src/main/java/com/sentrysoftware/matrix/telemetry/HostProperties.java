package com.sentrysoftware.matrix.telemetry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
public class HostProperties {

	private boolean isLocalhost;
	private String ipmitoolCommand;
	private int ipmiExecutionCount;
	private Set<String> possibleWmiNamespaces;
	private Set<String> possibleWbemNamespaces;
	private String vCenterTicket;
	private boolean osCommandExecutesLocally;
	private boolean osCommandExecutesRemotely;
	private boolean mustCheckSshStatus;

	@Default
	private Map<String, ConnectorNamespace> connectorNamespaces = new HashMap<>();

	private PowerMeter powerMeter;

	/**
	 *
	 * @param connectorName the name of a given connector
	 * @return ConnectorNamespace instance
	 */
	public ConnectorNamespace getConnectorNamespace(@NonNull final String connectorName) {
		synchronized (connectorNamespaces) {
			return connectorNamespaces.computeIfAbsent(connectorName, cn -> ConnectorNamespace.builder().build());
		}
	}
}
