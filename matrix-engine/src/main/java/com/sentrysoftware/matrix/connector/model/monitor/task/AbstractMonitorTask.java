package com.sentrysoftware.matrix.connector.model.monitor.task;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class AbstractMonitorTask implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Map<String, Source> sources = new LinkedHashMap<>(); // NOSONAR LinkHashMap is Serializable
	protected Mapping mapping;
	protected Set<String> executionOrder = new HashSet<>(); // NOSONAR HashSet is Serializable

	protected AbstractMonitorTask(final Map<String, Source> sources, final Mapping mapping, final Set<String> executionOrder)  {
		this.sources = sources == null ? new LinkedHashMap<>() : sources;
		this.mapping = mapping;
		this.executionOrder = executionOrder != null ? executionOrder : new HashSet<>();
	}
}
