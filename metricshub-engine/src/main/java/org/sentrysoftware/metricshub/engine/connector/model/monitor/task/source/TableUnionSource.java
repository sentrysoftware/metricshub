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
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.TablesDeserializer;
import org.sentrysoftware.metricshub.engine.connector.model.common.ExecuteForEachEntryOf;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import org.sentrysoftware.metricshub.engine.strategy.source.ISourceProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;

/**
 * Represents a source that performs a union operation on multiple tables.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TableUnionSource extends Source {

	private static final long serialVersionUID = 1L;

	/**
	 * The list of tables to be included in the union operation.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = TablesDeserializer.class)
	private List<String> tables = new ArrayList<>();

	/**
	 * Builder for creating instances of {@code TableUnionSource}.
	 *
	 * @param type                 The type of the source.
	 * @param computes             List of computations to be applied to the source.
	 * @param forceSerialization   Flag indicating whether to force serialization.
	 * @param tables               List of tables to be included in the union operation.
	 * @param key                  The key associated with the source.
	 * @param executeForEachEntryOf The execution context for each entry of the source.
	 */
	@Builder
	@JsonCreator
	public TableUnionSource(
		@JsonProperty("type") String type,
		@JsonProperty("computes") List<Compute> computes,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "tables", required = true) @NonNull List<String> tables,
		@JsonProperty("key") String key,
		@JsonProperty("executeForEachEntryOf") ExecuteForEachEntryOf executeForEachEntryOf
	) {
		super(type, computes, forceSerialization, key, executeForEachEntryOf);
		this.tables = tables;
	}

	@Override
	public TableUnionSource copy() {
		return TableUnionSource
			.builder()
			.type(type)
			.key(key)
			.forceSerialization(forceSerialization)
			.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
			.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
			.tables(tables != null ? new ArrayList<>(tables) : null)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// For now, there is nothing to update
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- tables=", tables);

		return stringJoiner.toString();
	}

	@Override
	public SourceTable accept(final ISourceProcessor sourceProcessor) {
		return sourceProcessor.process(this);
	}
}
