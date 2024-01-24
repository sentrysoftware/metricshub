package org.sentrysoftware.metricshub.engine.connector.model.monitor.task;

import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;

/**
 * The MonoInstanceCollect class represents a task for collecting metrics from a single instance.
 * It extends the AbstractCollect class and includes additional features specific to collecting
 * metrics from a mono instance.
 *
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MonoInstanceCollect extends AbstractCollect {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new MonoInstanceCollect instance with the provided sources, mapping, and execution order.
	 *
	 * @param sources        a map of source names to Source instances providing the metric data
	 * @param mapping        the mapping information for transforming collected data
	 * @param executionOrder a set defining the order in which the collect tasks should be executed
	 */
	@Builder
	public MonoInstanceCollect(
		final Map<String, Source> sources,
		final Mapping mapping,
		final Set<String> executionOrder
	) {
		super(sources, mapping, executionOrder);
	}
}
