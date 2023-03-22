package com.sentrysoftware.matrix.converter.state.source;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.source.tablejoin.ConnectorTableJoinProperty;
import com.sentrysoftware.matrix.converter.state.source.http.ConnectorHttpProperty;
import com.sentrysoftware.matrix.converter.state.source.reference.ConnectorReferenceProperty;
import com.sentrysoftware.matrix.converter.state.source.wmi.ConnectorWmiProperty;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSourceProperty {

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
				ConnectorWmiProperty.getConnectorProperties(),
				ConnectorTableJoinProperty.getConnectorProperties(),
				ConnectorHttpProperty.getConnectorProperties(),
				ConnectorReferenceProperty.getConnectorProperties()
			)
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
	}
}