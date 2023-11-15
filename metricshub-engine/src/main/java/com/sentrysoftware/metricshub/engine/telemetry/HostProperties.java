package com.sentrysoftware.metricshub.engine.telemetry;

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

	/**
	 * Get the connector namespace defined for the given connector identifier
	 *
	 * @param connectorId the identifier of the connector namespace
	 * @return ConnectorNamespace instance
	 */
	public ConnectorNamespace getConnectorNamespace(@NonNull final String connectorId) {
		synchronized (connectorNamespaces) {
			return connectorNamespaces.computeIfAbsent(connectorId, cn -> ConnectorNamespace.builder().build());
		}
	}
}
