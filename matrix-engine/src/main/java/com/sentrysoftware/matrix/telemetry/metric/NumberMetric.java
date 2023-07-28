package com.sentrysoftware.matrix.telemetry.metric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class NumberMetric extends AbstractMetric {

	private Double value;
	private Double previousValue;
}
