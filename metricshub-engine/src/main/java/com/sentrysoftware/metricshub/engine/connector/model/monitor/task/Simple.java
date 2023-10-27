package com.sentrysoftware.metricshub.engine.connector.model.monitor.task;

import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Simple extends AbstractMonitorTask {

	private static final long serialVersionUID = 1L;

	@Builder
	public Simple(final Map<String, Source> sources, final Mapping mapping, final Set<String> executionOrder) {
		super(sources, mapping, executionOrder);
	}
}