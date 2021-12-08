package com.sentrysoftware.hardware.agent.dto;

import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.DEFAULT_OUTPUT_DIRECTORY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TimeDeserializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to wrap the agent configuration for all targets.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiHostsConfigurationDTO {

	public static final int DEFAULT_JOB_POOL_SIZE = 20;
	public static final long DEFAULT_COLLECT_PERIOD = 120;
	public static final int DEFAULT_DISCOVERY_CYCLE = 30;

	@Default
	private Set<HostConfigurationDTO> targets = new HashSet<>();

	@Default
	private int jobPoolSize = DEFAULT_JOB_POOL_SIZE;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private long collectPeriod = DEFAULT_COLLECT_PERIOD;

	@Default
	private int discoveryCycle = DEFAULT_DISCOVERY_CYCLE;

	private boolean exportTimestamps;

	@Default
	private String loggerLevel = "OFF";

	@Default
	private String outputDirectory = DEFAULT_OUTPUT_DIRECTORY;

	@Default
	private Map<String, String> extraLabels = new HashMap<>();

	@Default
	private Map<String, Double> extraMetrics = new HashMap<>();

	@Default
	private String otlpGrpcEndpoint = "http://localhost:4317";

	/**
	 * Build a new empty instance
	 * 
	 * @return {@link MultiHostsConfigurationDTO} object
	 */
	public static MultiHostsConfigurationDTO empty() {
		return MultiHostsConfigurationDTO.builder().build();
	}

	/**
	 * Whether the configuration is empty or not
	 * 
	 * @return boolean value
	 */
	public boolean isEmpty() {
		return targets.isEmpty();
	}
}