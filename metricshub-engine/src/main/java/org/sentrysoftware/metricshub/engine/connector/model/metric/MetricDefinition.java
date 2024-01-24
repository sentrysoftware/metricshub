package org.sentrysoftware.metricshub.engine.connector.model.metric;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.EMPTY;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the definition of a metric.
 *
 * <p>A MetricDefinition instance holds information about a metric, such as its unit, description, and type.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetricDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The unit of the metric. Default is an empty string.
	 */
	@Default
	@JsonSetter(nulls = SKIP)
	private String unit = EMPTY;

	/**
	 * The description of the metric. Default is an empty string.
	 */
	@Default
	@JsonSetter(nulls = SKIP)
	private String description = EMPTY;

	/**
	 * The type of the metric. Default is {@link MetricType#GAUGE}.
	 */
	@Default
	@JsonSetter(nulls = SKIP)
	private IMetricType type = MetricType.GAUGE;
}
