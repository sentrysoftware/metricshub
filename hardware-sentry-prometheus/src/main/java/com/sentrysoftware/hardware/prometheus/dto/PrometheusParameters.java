package com.sentrysoftware.hardware.prometheus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class PrometheusParameters {
	private String prometheusParameterName;
	private String prometheusParameterUnit;
	private Double prometheusParameterFactor;

}