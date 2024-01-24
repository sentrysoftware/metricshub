package org.sentrysoftware.metricshub.engine.connector.model.monitor.task;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Map;
import java.util.Set;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;

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

	public AbstractCollect(final Map<String, Source> sources, final Mapping mapping, final Set<String> executionOrder) {
		super(sources, mapping, executionOrder);
	}
}
