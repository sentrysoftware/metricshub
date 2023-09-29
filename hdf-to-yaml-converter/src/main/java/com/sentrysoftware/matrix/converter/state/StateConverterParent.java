package com.sentrysoftware.matrix.converter.state;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import java.util.Set;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StateConverterParent implements IConnectorStateConverter {

	private Set<IConnectorStateConverter> stateConverters;

	@Override
	public boolean detect(final String key, final String value, final JsonNode connector) {
		// Return true if one of the stateConverters hooked
		return stateConverters.stream().anyMatch(stateParser -> stateParser.detect(key, value, connector));
	}

	@Override
	public void convert(final String key, final String value, final JsonNode connector, final PreConnector preConnector) {
		// Filter detected then call its parser
		stateConverters
			.stream()
			.filter(stateParser -> stateParser.detect(key, value, connector))
			.forEach(stateParser -> stateParser.convert(key, value, connector, preConnector));
	}
}
