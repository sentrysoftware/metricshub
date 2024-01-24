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
 * The {@code Simple} class represents a simple monitor task.
 * It extends the {@code AbstractMonitorTask} class and includes basic features for monitor tasks.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Simple extends AbstractMonitorTask {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@code Simple} instance with the provided sources, mapping, and execution order.
	 *
	 * @param sources        a map of source names to {@code Source} instances providing the metric data
	 * @param mapping        the mapping information for transforming collected data
	 * @param executionOrder a set defining the order in which the monitor tasks should be executed
	 */
	@Builder
	public Simple(final Map<String, Source> sources, final Mapping mapping, final Set<String> executionOrder) {
		super(sources, mapping, executionOrder);
	}
}
