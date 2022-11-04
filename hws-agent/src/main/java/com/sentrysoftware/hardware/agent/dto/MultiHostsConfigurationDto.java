package com.sentrysoftware.hardware.agent.dto;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.DEFAULT_OUTPUT_DIRECTORY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.HostsDeserializer;
import com.sentrysoftware.hardware.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.hardware.agent.dto.exporter.ExporterConfigDto;
import com.sentrysoftware.hardware.agent.dto.exporter.OtlpConfigDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to wrap the agent configuration for all hosts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiHostsConfigurationDto {

	public static final String HW_PROBLEM_DEFAULT_TEMPLATE = "Hardware problem on ${FQDN} with ${MONITOR_NAME}.${NEWLINE}${NEWLINE}${ALERT_DETAILS}${NEWLINE}${NEWLINE}${FULLREPORT}";
	public static final int DEFAULT_JOB_POOL_SIZE = 20;
	public static final long DEFAULT_COLLECT_PERIOD = 120;
	public static final int DEFAULT_DISCOVERY_CYCLE = 30;

	@Default
	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = HostsDeserializer.class)
	private Set<HostConfigurationDto> hosts = new HashSet<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Set<HostGroupConfigurationDto> hostGroups = new HashSet<>();
	
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
	private ExporterConfigDto exporter = ExporterConfigDto.builder().build();

	@Default
	private String hardwareProblemTemplate = HW_PROBLEM_DEFAULT_TEMPLATE;

	@Default
	private boolean disableAlerts = false;

	/**
	 * Build a new empty instance
	 * 
	 * @return {@link MultiHostsConfigurationDto} object
	 */
	public static MultiHostsConfigurationDto empty() {
		return MultiHostsConfigurationDto.builder().build();
	}

	/**
	 * Whether the configuration is empty or not
	 * 
	 * @return boolean value
	 */
	public boolean isEmpty() {
		return hosts.isEmpty();
	}

	/**
	 * Whether the {@link OtlpConfigDto} is present or not
	 * 
	 * @return boolean value
	 */
	public boolean hasExporterOtlpConfig() {
		return hasExporterConfig() && exporter.getOtlp() != null;
	}

	/**
	 * Whether the {@link ExporterConfigDto} is present or not
	 * 
	 * @return boolean value
	 */
	public boolean hasExporterConfig() {
		return exporter != null;
	}
}
