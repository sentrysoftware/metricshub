package org.sentrysoftware.metricshub.engine.connector.model.metric;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StateSet implements IMetricType {

	private static final long serialVersionUID = 1L;

	@Default
	private MetricType output = MetricType.UP_DOWN_COUNTER;

	@Default
	@JsonProperty("stateSet")
	private Set<String> set = new HashSet<>(); // NOSONAR

	@Override
	public MetricType get() {
		return output;
	}
}
