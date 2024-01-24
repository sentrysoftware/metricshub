package org.sentrysoftware.metricshub.engine.connector.model.monitor.task;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankInLinkedHashSetDeserializer;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.SourcesDeserializer;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;

/**
 * An abstract class representing a monitor task, providing fields for sources, mapping, execution order, and source dependencies.
 * It includes custom deserialization for sources and non-blank linked hash sets.
 */
@Data
@NoArgsConstructor
public abstract class AbstractMonitorTask implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * A map of sources associated with their names.
	 */
	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = SourcesDeserializer.class)
	protected Map<String, Source> sources = new LinkedHashMap<>(); // NOSONAR LinkHashMap is Serializable

	/**
	 * The mapping associated with the monitor task.
	 */
	protected Mapping mapping;

	/**
	 * The execution order of the monitor task.
	 */
	@JsonDeserialize(using = NonBlankInLinkedHashSetDeserializer.class)
	protected Set<String> executionOrder = new LinkedHashSet<>(); // NOSONAR LinkedHashSet is Serializable

	/**
	 * The list of source dependencies.
	 */
	protected List<Set<String>> sourceDep = new ArrayList<>(); // NOSONAR ArrayList is Serializable

	/**
	 * Initializes the AbstractMonitorTask instance with the specified sources, mapping, and execution order.
	 *
	 * @param sources       A map of sources associated with their names.
	 * @param mapping       The mapping associated with the monitor task.
	 * @param executionOrder The execution order of the monitor task.
	 */
	protected AbstractMonitorTask(
		final Map<String, Source> sources,
		final Mapping mapping,
		final Set<String> executionOrder
	) {
		this.sources = sources == null ? new LinkedHashMap<>() : sources;
		this.mapping = mapping;
		this.executionOrder = executionOrder != null ? executionOrder : new LinkedHashSet<>();
		this.sourceDep = new ArrayList<>();
	}
}
