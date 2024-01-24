package org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankDeserializer;
import org.sentrysoftware.metricshub.engine.connector.model.common.ExecuteForEachEntryOf;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import org.sentrysoftware.metricshub.engine.strategy.source.ISourceProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;

/**
 * Represents a source that retrieves data using Windows Management Instrumentation (WMI).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WmiSource extends Source {

	private static final long serialVersionUID = 1L;

	/**
	 * The WMI query used to retrieve data.
	 */
	@NonNull
	@JsonDeserialize(using = NonBlankDeserializer.class)
	@JsonSetter(nulls = FAIL)
	private String query;

	/**
	 * The WMI namespace for the query.
	 */
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String namespace;

	/**
	 * Builder for creating instances of {@code WmiSource}.
	 *
	 * @param type                 The type of the source.
	 * @param computes             List of computations to be applied to the source.
	 * @param forceSerialization   Flag indicating whether to force serialization.
	 * @param query                The WMI query used to retrieve data.
	 * @param namespace            The WMI namespace for the query.
	 * @param key                  The key associated with the source.
	 * @param executeForEachEntryOf The execution context for each entry of the source.
	 */
	@Builder
	@JsonCreator
	public WmiSource(
		@JsonProperty("type") String type,
		@JsonProperty("computes") List<Compute> computes,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "query", required = true) @NonNull String query,
		@JsonProperty("namespace") String namespace,
		@JsonProperty("key") String key,
		@JsonProperty("executeForEachEntryOf") ExecuteForEachEntryOf executeForEachEntryOf
	) {
		super(type, computes, forceSerialization, key, executeForEachEntryOf);
		this.query = query;
		this.namespace = namespace;
	}

	@Override
	public WmiSource copy() {
		return WmiSource
			.builder()
			.type(type)
			.key(key)
			.forceSerialization(forceSerialization)
			.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
			.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
			.query(query)
			.namespace(namespace)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		query = updater.apply(query);
		namespace = updater.apply(namespace);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- query=", query);
		addNonNull(stringJoiner, "- namespace=", namespace);

		return stringJoiner.toString();
	}

	@Override
	public SourceTable accept(final ISourceProcessor sourceProcessor) {
		return sourceProcessor.process(this);
	}
}
