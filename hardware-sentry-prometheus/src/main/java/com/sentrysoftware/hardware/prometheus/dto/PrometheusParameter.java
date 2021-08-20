package com.sentrysoftware.hardware.prometheus.dto;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
@AllArgsConstructor
public class PrometheusParameter {
	@NonNull
	private String name;

	@Default
	private double factor = 1D;

	@Default
	@NonNull
	private String unit = HardwareConstants.EMPTY;

	@Default
	@NonNull
	private PrometheusMetricType type = PrometheusMetricType.GAUGE;

	public enum PrometheusMetricType { GAUGE, COUNTER }
}