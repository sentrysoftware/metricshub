package com.sentrysoftware.matrix.converter.state.mapping;

import com.fasterxml.jackson.databind.JsonNode;

public class NoopConverter implements IMappingConverter {

	@Override
	public void convertCollectProperty(String key, String value, JsonNode node) {
		// Not implemented
	}

	@Override
	public void postConvertDiscoveryProperties(JsonNode mapping) {
		// Not implemented
	}

}
