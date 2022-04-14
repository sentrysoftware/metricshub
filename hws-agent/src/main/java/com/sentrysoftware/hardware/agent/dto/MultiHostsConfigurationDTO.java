package com.sentrysoftware.hardware.agent.dto;

import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.DEFAULT_OUTPUT_DIRECTORY;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.hardware.agent.dto.exporter.ExporterConfigDTO;
import com.sentrysoftware.hardware.agent.dto.exporter.OtlpConfigDTO;

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
	@JsonSetter(nulls = SKIP)
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
	private String outputDirectory = DEFAULT_OUTPUT_DIRECTORY.toString();

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, String> extraLabels = new HashMap<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, Double> extraMetrics = new HashMap<>();

	@Default
	private boolean resolveHostnameToFqdn = true;

	private boolean sequential;

	// Exporter settings
	@Default
	@JsonSetter(nulls = SKIP)
	private ExporterConfigDTO exporter = ExporterConfigDTO.builder().build();

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

	/**
	 * Whether the {@link OtlpConfigDTO} is present or not
	 * 
	 * @return boolean value
	 */
	public boolean hasExporterOtlpConfig() {
		return hasExporterConfig() && exporter.getOtlp() != null;
	}

	/**
	 * Whether the {@link ExporterConfigDTO} is present or not
	 * 
	 * @return boolean value
	 */
	public boolean hasExporterConfig() {
		return exporter != null;
	}
}
