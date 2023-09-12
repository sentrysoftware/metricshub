package com.sentrysoftware.matrix.agent.config;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.agent.config.protocols.ProtocolsConfig;
import com.sentrysoftware.matrix.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.connector.model.Connector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceConfig {

	private String loggerLevel;
	private String outputDirectory;
	@JsonDeserialize(using = TimeDeserializer.class)
	private long collectPeriod;
	private int discoveryCycle;
	private AlertingSystemConfig alertingSystemConfig;
	private Boolean sequential;
	private Boolean resolveHostnameToFqdn;
	private Long jobTimeout;

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, String> attributes = new HashMap<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, Double> metrics = new HashMap<>();

	private ProtocolsConfig protocols;

	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> selectConnectors = new HashSet<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> excludeConnectors = new HashSet<>();

	private Connector connector;
}
