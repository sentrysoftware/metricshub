package com.sentrysoftware.matrix.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StateSetMetric extends AbstractMetric {

	private String value;
	private String previousValue;
	private String[] stateSet;
}
