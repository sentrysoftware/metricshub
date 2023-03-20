package com.sentrysoftware.matrix.connector.model.monitor.task;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

import lombok.NoArgsConstructor;

@NoArgsConstructor

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = MonoInstanceCollect.class)
@JsonSubTypes(
	{
		@JsonSubTypes.Type(value = MonoInstanceCollect.class, name = "monoInstance"),
		@JsonSubTypes.Type(value = MultiInstanceCollect.class, name = "multiInstance"),
	}
)
public class AbstractCollect extends AbstractMonitorTask {

	private static final long serialVersionUID = 1L;

	public AbstractCollect(final Map<String, Source> sources, final Mapping mapping, final Set<String> executionOrder)  {
		super(sources, mapping, executionOrder);
	}
}
