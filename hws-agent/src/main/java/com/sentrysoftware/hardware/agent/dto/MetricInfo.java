package com.sentrysoftware.hardware.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
@AllArgsConstructor
public class MetricInfo {
	@NonNull
	private String name;

	@Default
	private double factor = 1D;

	@Default
	@NonNull
	private String unit = "";

	@Default
	@NonNull
	private MetricType type = MetricType.GAUGE;

	@Default
	@NonNull
	private String description = "";

	public enum MetricType { GAUGE, COUNTER }
}