package org.sentrysoftware.metricshub.engine.connector.model.common;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.EMPTY;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.StringJoiner;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankDeserializer;

/**
 * Represents a configuration for executing an operation for each entry of a specified source.
 */
@Data
@NoArgsConstructor
public class ExecuteForEachEntryOf implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The source for executing the operation for each entry.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String source;

	/**
	 * The method used to concatenate entries.
	 */
	@JsonSetter(nulls = SKIP)
	private IEntryConcatMethod concatMethod = EntryConcatMethod.LIST;

	/**
	 * Constructs an instance of {@link ExecuteForEachEntryOf}.
	 *
	 * @param source       The source for executing the operation for each entry.
	 * @param concatMethod The method used to concatenate entries.
	 */
	@Builder
	@JsonCreator
	public ExecuteForEachEntryOf(
		@JsonProperty(value = "source", required = true) @NonNull String source,
		@JsonProperty("concatMethod") IEntryConcatMethod concatMethod
	) {
		this.source = source;
		this.concatMethod = concatMethod == null ? EntryConcatMethod.LIST : concatMethod;
	}

	/**
	 * Creates a copy of the current {@link ExecuteForEachEntryOf} instance.
	 *
	 * @return A new instance of {@link ExecuteForEachEntryOf} with the same source and concatenation method.
	 */
	public ExecuteForEachEntryOf copy() {
		return ExecuteForEachEntryOf.builder().source(source).concatMethod(concatMethod.copy()).build();
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		addNonNull(stringJoiner, "- executeForEachEntryOf=", source);
		addNonNull(stringJoiner, "- concatMethod=", concatMethod != null ? concatMethod.getDescription() : EMPTY);

		return stringJoiner.toString();
	}
}
