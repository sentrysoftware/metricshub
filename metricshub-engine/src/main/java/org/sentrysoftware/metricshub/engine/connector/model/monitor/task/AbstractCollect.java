package org.sentrysoftware.metricshub.engine.connector.model.monitor.task;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Map;
import java.util.Set;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;

/**
 * An abstract class representing a collection task within the MetricsHub engine. Subclasses, such as
 * {@link MonoInstanceCollect} and {@link MultiInstanceCollect}, define specific types of collection tasks.
 * This class provides a common structure for all collection tasks, including sources, mappings, and execution order.
 */
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type",
	defaultImpl = MonoInstanceCollect.class
)
@JsonSubTypes(
	{
		@JsonSubTypes.Type(value = MonoInstanceCollect.class, name = "monoInstance"),
		@JsonSubTypes.Type(value = MultiInstanceCollect.class, name = "multiInstance")
	}
)
public class AbstractCollect extends AbstractMonitorTask {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an instance of AbstractCollect with the specified sources, mapping, and execution order.
	 *
	 * @param sources        A map of source names to source configurations.
	 * @param mapping        The mapping configuration for the collection task.
	 * @param executionOrder A set defining the order of execution for the collection task.
	 */
	public AbstractCollect(final Map<String, Source> sources, final Mapping mapping, final Set<String> executionOrder) {
		super(sources, mapping, executionOrder);
	}
}
