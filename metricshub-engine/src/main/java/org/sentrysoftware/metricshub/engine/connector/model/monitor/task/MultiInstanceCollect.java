package org.sentrysoftware.metricshub.engine.connector.model.monitor.task;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;

/**
 * The {@code MultiInstanceCollect} class represents a task for collecting metrics from multiple instances.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MultiInstanceCollect extends AbstractCollect {

	private static final Set<String> DEFAULT_KEYS = Set.of(MetricsHubConstants.MONITOR_ATTRIBUTE_ID);

	private static final long serialVersionUID = 1L;

	/**
	 * The set of keys used for collecting metrics from multiple instances.
	 */
	@JsonSetter(nulls = SKIP)
	private Set<String> keys = DEFAULT_KEYS;

	/**
	 * Constructs a new {@code MultiInstanceCollect} instance with the provided sources, mapping, execution order, and keys.
	 *
	 * @param sources        a map of source names to {@code Source} instances providing the metric data
	 * @param mapping        the mapping information for transforming collected data
	 * @param executionOrder a set defining the order in which the collect tasks should be executed
	 * @param keys           a set defining the keys used for collecting metrics from multiple instances
	 */
	@Builder
	public MultiInstanceCollect(
		final Map<String, Source> sources,
		final Mapping mapping,
		final Set<String> executionOrder,
		final Set<String> keys
	) {
		super(sources, mapping, executionOrder);
		this.keys = keys != null ? keys : DEFAULT_KEYS;
	}
}
