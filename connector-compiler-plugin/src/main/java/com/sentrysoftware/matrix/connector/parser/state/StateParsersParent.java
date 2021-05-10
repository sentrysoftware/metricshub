package com.sentrysoftware.matrix.connector.parser.state;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.Connector;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StateParsersParent implements IConnectorStateParser {

	private Set<IConnectorStateParser> stateParsers;

	@Override
	public boolean detect(String key, String value, Connector connector) {

		// Return true if one of the stateParsers hooked
		return stateParsers
				.stream()
				.anyMatch(stateParser -> stateParser.detect(key, value, connector));
	}

	@Override
	public void parse(String key, String value, Connector connector) {

		// Filter detected then call its parser
		stateParsers.stream()
		.filter(stateParser -> stateParser.detect(key, value, connector))
		.forEach(stateParser -> stateParser.parse(key, value, connector));
	}

}
