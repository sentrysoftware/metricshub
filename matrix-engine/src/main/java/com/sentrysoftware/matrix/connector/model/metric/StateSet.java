package com.sentrysoftware.matrix.connector.model.metric;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StateSet implements IMetricType {

	private static final long serialVersionUID = 1L;

	@Default
	private MetricType output = MetricType.UP_DOWN_COUNTER;
	@Default
	private Set<String> stateSet = new HashSet<>(); // NOSONAR

	@Override
	public MetricType get() {
		return output;
	}
}
