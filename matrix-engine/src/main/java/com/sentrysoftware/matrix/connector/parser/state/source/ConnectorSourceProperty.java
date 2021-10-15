package com.sentrysoftware.matrix.connector.parser.state.source;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.http.ConnectorHttpProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.ipmi.ConnectorIpmiProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.oscommand.ConnectorOsCommandProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.reference.ConnectorReferenceProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.snmpget.ConnectorSnmpGetProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.snmptable.ConnectorSnmpTableProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.ConnectorSshInteractiveProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.tablejoin.ConnectorTableJoinProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.tableunion.ConnectorTableUnionProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.wbem.ConnectorWbemProperty;
import com.sentrysoftware.matrix.connector.parser.state.source.wmi.ConnectorWmiProperty;

public class ConnectorSourceProperty {

	private ConnectorSourceProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(ConnectorSnmpTableProperty.getConnectorProperties(),
				ConnectorTableJoinProperty.getConnectorProperties(),
				ConnectorTableUnionProperty.getConnectorProperties(),
				ConnectorSnmpGetProperty.getConnectorProperties(),
				ConnectorWbemProperty.getConnectorProperties(),
				ConnectorWmiProperty.getConnectorProperties(),
				ConnectorHttpProperty.getConnectorProperties(),
				ConnectorReferenceProperty.getConnectorProperties(),
				ConnectorOsCommandProperty.getConnectorProperties(),
				ConnectorIpmiProperty.getConnectorProperties(),
				ConnectorSshInteractiveProperty.getConnectorProperties())
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
	}
}