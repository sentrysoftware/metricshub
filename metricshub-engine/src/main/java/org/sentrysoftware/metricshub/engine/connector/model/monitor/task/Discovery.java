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
 * Represents a discovery task used in monitor configuration.
 *
 * <p>
 * A {@code Discovery} instance holds information about the sources, mapping, and execution order of a discovery task.
 * </p>
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Discovery extends AbstractMonitorTask {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@code Discovery} instance with the specified sources, mapping, and execution order.
	 *
	 * @param sources        The sources associated with the discovery task.
	 * @param mapping        The mapping used in the discovery task.
	 * @param executionOrder The set specifying the execution order of the task.
	 */
	@Builder
	public Discovery(final Map<String, Source> sources, final Mapping mapping, final Set<String> executionOrder) {
		super(sources, mapping, executionOrder);
	}
}
