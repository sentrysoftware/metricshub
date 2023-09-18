package com.sentrysoftware.matrix.agent.context;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.Getter;

@Data
public class AgentContext {

	public static final ObjectMapper OBJECT_MAPPER = JsonMapper
		.builder(new YAMLFactory())
		.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
		.enable(SerializationFeature.INDENT_OUTPUT)
		.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
		.build();

	@Getter
	private static AgentContext instance = new AgentContext();

	private AgentInfo agentInfo;

	public AgentContext() {
		agentInfo = new AgentInfo();
	}
}
