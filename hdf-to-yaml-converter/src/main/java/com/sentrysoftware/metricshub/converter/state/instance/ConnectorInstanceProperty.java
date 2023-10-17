package com.sentrysoftware.metricshub.converter.state.instance;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorInstanceProperty {

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream.of(new InstanceProcessor(), new InstanceTableProcessor()).collect(Collectors.toSet());
	}
}
