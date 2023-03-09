package com.sentrysoftware.matrix.converter.state;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StateConverterParent implements IConnectorStateConverter {

	private Set<IConnectorStateConverter> stateConverters;

	@Override
	public boolean detect(String key, String value, JsonNode connector) {

		// Return true if one of the stateConverters hooked
		return stateConverters
			.stream()
			.anyMatch(stateParser -> stateParser.detect(key, value, connector));
	}

	@Override
	public void convert(String key, String value, JsonNode connector) {

		// Filter detected then call its parser
		stateConverters
			.stream()
			.filter(stateParser -> stateParser.detect(key, value, connector))
			.forEach(stateParser -> stateParser.convert(key, value, connector));
	}

}