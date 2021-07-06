package com.sentrysoftware.hardware.prometheus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO to wrap the exporter configuration for all targets.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiHostsConfigurationDTO {

	@Builder.Default
	private List<HostConfigurationDTO> targets = new ArrayList<>();
}
