package com.sentrysoftware.matrix.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostProperties {

	private boolean isLocalhost;
	private String ipmitoolCommand;
	private Set<String> possibleWmiNamespaces;
	private Set<String> possibleWbemNamespaces;
	private String vCenterTicket;
	private boolean osCommandExecutesLocally;
	private boolean osCommandExecutesRemotely;
	private boolean mustCheckSshStatus;
	private Map<String, ConnectorNamespace> connectorNamespaces;
	private PowerMeter powerMeter;
}
