package org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.EMPTY;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.common.EntryConcatMethod;
import org.sentrysoftware.metricshub.engine.connector.model.common.ExecuteForEachEntryOf;
import org.sentrysoftware.metricshub.engine.connector.model.common.IEntryConcatMethod;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import org.sentrysoftware.metricshub.engine.strategy.source.ISourceProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;

/**
 * An abstract class representing a data source in the MetricsHub engine.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
	{
		@JsonSubTypes.Type(value = CopySource.class, name = "copy"),
		@JsonSubTypes.Type(value = HttpSource.class, name = "http"),
		@JsonSubTypes.Type(value = IpmiSource.class, name = "ipmi"),
		@JsonSubTypes.Type(value = OsCommandSource.class, name = "osCommand"),
		@JsonSubTypes.Type(value = SnmpGetSource.class, name = "snmpGet"),
		@JsonSubTypes.Type(value = SnmpTableSource.class, name = "snmpTable"),
		@JsonSubTypes.Type(value = StaticSource.class, name = "static"),
		@JsonSubTypes.Type(value = TableJoinSource.class, name = "tableJoin"),
		@JsonSubTypes.Type(value = TableUnionSource.class, name = "tableUnion"),
		@JsonSubTypes.Type(value = WbemSource.class, name = "wbem"),
		@JsonSubTypes.Type(value = WmiSource.class, name = "wmi")
	}
)
@Data
@NoArgsConstructor
public abstract class Source implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The type of the data source.
	 */
	protected String type;

	/**
	 * List of compute operations to be performed on the source data.
	 * It is annotated with @JsonSetter to skip null values during deserialization.
	 */
	@JsonSetter(nulls = SKIP)
	private List<Compute> computes = new ArrayList<>();

	/**
	 * Flag indicating whether the source should be force-serialized.
	 */
	protected boolean forceSerialization;

	/**
	 * A key associated with the source, excluded from JSON serialization using @JsonIgnore.
	 */
	@JsonIgnore
	protected String key;

	/**
	 * Configuration for executing an operation for each entry of the source.
	 */
	protected ExecuteForEachEntryOf executeForEachEntryOf;
	/**
	 * Set of references associated with the source.
	 */
	private Set<String> references = new HashSet<>();

	protected Source(
		String type,
		List<Compute> computes,
		boolean forceSerialization,
		String key,
		ExecuteForEachEntryOf executeForEachEntryOf
	) {
		this.type = type;
		this.computes = computes == null ? new ArrayList<>() : computes;
		this.forceSerialization = forceSerialization;
		this.key = key;
		this.executeForEachEntryOf = executeForEachEntryOf;
		this.references = new HashSet<>();
	}

	/**
	 * Creates a copy of the source.
	 *
	 * @return A new Source instance that is a copy of the original.
	 */
	public abstract Source copy();

	/**
	 * Updates the source based on the provided updater function.
	 *
	 * @param updater The updater function to modify the source.
	 */
	public abstract void update(UnaryOperator<String> updater);

	/**
	 * Accepts a source processor to perform processing on the source.
	 *
	 * @param sourceProcessor The source processor to accept.
	 * @return A SourceTable representing the processed source.
	 */
	public abstract SourceTable accept(ISourceProcessor sourceProcessor);

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(new StringBuilder("- ").append(key).append(".type=").append(this.getClass().getSimpleName()));

		addNonNull(stringJoiner, "- forceSerialization=", forceSerialization);
		// A small trick here because the executeForEachEntryOf.toString value is already
		// formatted that's why we don't need a prefix for the string value of the nested executeForEachEntryOf
		addNonNull(stringJoiner, EMPTY, executeForEachEntryOf != null ? executeForEachEntryOf.toString() : null);

		return stringJoiner.toString();
	}

	/**
	 * Whether the {@link ExecuteForEachEntryOf} is present in the {@link Source} or
	 * not
	 *
	 * @return <code>true</code> if {@link ExecuteForEachEntryOf} is present otherwise
	 *         <code>false</code>
	 */
	public boolean isExecuteForEachEntryOf() {
		// CHECKSTYLE:OFF
		return (
			executeForEachEntryOf != null &&
			executeForEachEntryOf.getSource() != null &&
			!executeForEachEntryOf.getSource().isBlank()
		);
		// CHECKSTYLE:ON
	}

	/**
	 * Get the {@link EntryConcatMethod} value
	 *
	 * @return {@link EntryConcatMethod} enum value
	 */
	public IEntryConcatMethod getEntryConcatMethod() {
		return executeForEachEntryOf != null ? executeForEachEntryOf.getConcatMethod() : null;
	}

	/**
	 * Get the executeForEachEntryOf string value
	 *
	 * @return String value
	 */
	public String getExecuteForEachEntryOf() {
		return executeForEachEntryOf != null ? executeForEachEntryOf.getSource() : null;
	}
}
