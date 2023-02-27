package com.sentrysoftware.matrix.connector.model.monitor.task;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.NonBlankInLinkedHashSetDeserializer;
import com.sentrysoftware.matrix.connector.deserializer.custom.SourcesDeserializer;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class AbstractMonitorTask implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = SourcesDeserializer.class)
	protected Map<String, Source> sources = new LinkedHashMap<>(); // NOSONAR LinkHashMap is Serializable
	protected Mapping mapping;
	@JsonDeserialize(using = NonBlankInLinkedHashSetDeserializer.class)
	protected Set<String> executionOrder = new LinkedHashSet<>(); // NOSONAR HashSet is Serializable

	protected AbstractMonitorTask(final Map<String, Source> sources, final Mapping mapping, final Set<String> executionOrder)  {
		this.sources = sources == null ? new LinkedHashMap<>() : sources;
		this.mapping = mapping;
		this.executionOrder = executionOrder != null ? executionOrder : new LinkedHashSet<>();
	}
}
