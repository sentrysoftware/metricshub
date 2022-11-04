package com.sentrysoftware.hardware.agent.deserialization;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.hardware.agent.dto.HostnameInfoDto;
import com.sentrysoftware.hardware.agent.dto.HostnameMapDto;
import com.sentrysoftware.hardware.agent.dto.HostnameSetDto;
import com.sentrysoftware.hardware.agent.dto.IHostnames;

public class HostnamesDeserializer extends JsonDeserializer<IHostnames> {

	@Override
	public IHostnames deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
		if (parser.isExpectedStartArrayToken()) {
			return HostnameSetDto
				.builder()
				.set(parser.readValueAs(new TypeReference<Set<String>>(){}))
				.build();
		}
		
		return HostnameMapDto
			.builder()
			.map(parser.readValueAs(new TypeReference<Map<String, HostnameInfoDto>>(){}))
			.build();
	}

}
