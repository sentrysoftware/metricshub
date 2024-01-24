package org.sentrysoftware.metricshub.engine.connector.model.metric;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a set of states as a metric type.
 *
 * <p>
 * A StateSet instance holds a set of string states, and it is considered a metric type with the default type
 * {@link MetricType#UP_DOWN_COUNTER}.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StateSet implements IMetricType {

	private static final long serialVersionUID = 1L;

	/**
	 * The metric type of the StateSet. Default is {@link MetricType#UP_DOWN_COUNTER}.
	 */
	@Default
	private MetricType output = MetricType.UP_DOWN_COUNTER;

	/**
	 * The set of states associated with the metric.
	 */
	@Default
	@JsonProperty("stateSet")
	private Set<String> set = new HashSet<>(); // NOSONAR

	@Override
	public MetricType get() {
		return output;
	}
}
