package com.sentrysoftware.matrix.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NumberMetric extends AbstractMetric {

	private Double value;
	private Double previousValue;
}
