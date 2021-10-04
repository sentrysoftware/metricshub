package com.sentrysoftware.hardware.prometheus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.HashSet;

/**
 * DTO to wrap the exporter configuration for all targets.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiHostsConfigurationDTO {

	public static final int DEFAULT_JOB_POOL_SIZE = 20;
	public static final int DEFAULT_COLLECT_PERIOD = 120;
	public static final int DEFAULT_DISCOVERY_CYCLE = 30;

	@Default
	private Set<HostConfigurationDTO> targets = new HashSet<>();

	@Default
	private int jobPoolSize = DEFAULT_JOB_POOL_SIZE;

	@Default
	private int collectPeriod = DEFAULT_COLLECT_PERIOD;

	@Default
	private int discoveryCycle = DEFAULT_DISCOVERY_CYCLE;

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
