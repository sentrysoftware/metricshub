package com.sentrysoftware.matrix.converter.state.instance;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorInstanceProperty {
    
    public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
			new InstanceProcessor(),
            new InstanceTableProcessor()
		)
		.collect(Collectors.toSet());
	}
}