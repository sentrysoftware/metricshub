package com.sentrysoftware.metricshub.converter.state.source;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.source.http.ConnectorHttpProperty;
import com.sentrysoftware.metricshub.converter.state.source.ipmi.ConnectorIpmiProperty;
import com.sentrysoftware.metricshub.converter.state.source.oscommand.ConnectorOsCommandProperty;
import com.sentrysoftware.metricshub.converter.state.source.reference.ConnectorReferenceProperty;
import com.sentrysoftware.metricshub.converter.state.source.snmpget.ConnectorSnmpGetProperty;
import com.sentrysoftware.metricshub.converter.state.source.snmptable.ConnectorSnmpTableProperty;
import com.sentrysoftware.metricshub.converter.state.source.tablejoin.ConnectorTableJoinProperty;
import com.sentrysoftware.metricshub.converter.state.source.tableunion.ConnectorTableUnionProperty;
import com.sentrysoftware.metricshub.converter.state.source.wbem.ConnectorWbemProperty;
import com.sentrysoftware.metricshub.converter.state.source.wmi.ConnectorWmiProperty;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSourceProperty {

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(
				ConnectorWmiProperty.getConnectorProperties(),
				ConnectorWbemProperty.getConnectorProperties(),
				ConnectorSnmpTableProperty.getConnectorProperties(),
				ConnectorSnmpGetProperty.getConnectorProperties(),
				ConnectorTableJoinProperty.getConnectorProperties(),
				ConnectorTableUnionProperty.getConnectorProperties(),
				ConnectorHttpProperty.getConnectorProperties(),
				ConnectorOsCommandProperty.getConnectorProperties(),
				ConnectorReferenceProperty.getConnectorProperties(),
				ConnectorOsCommandProperty.getConnectorProperties(),
				ConnectorIpmiProperty.getConnectorProperties()
			)
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
	}
}
