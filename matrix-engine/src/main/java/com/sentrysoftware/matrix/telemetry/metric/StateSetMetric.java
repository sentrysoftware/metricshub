package com.sentrysoftware.matrix.telemetry.metric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StateSetMetric extends AbstractMetric {

	private String value;
	private String previousValue;
	private String[] stateSet;
}
