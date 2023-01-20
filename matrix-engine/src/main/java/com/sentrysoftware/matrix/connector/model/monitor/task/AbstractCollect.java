package com.sentrysoftware.matrix.connector.model.monitor.task;

import java.util.Map;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AbstractCollect extends AbstractMonitorTask {

	private static final long serialVersionUID = 1L;

	public AbstractCollect(final Map<String, Source> sources, final Mapping mapping, final Set<String> executionOrder)  {
		super(sources, mapping, executionOrder);
	}
}
