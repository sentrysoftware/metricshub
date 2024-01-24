package org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.strategy.source.compute.IComputeProcessor;

/**
 * Represents a Replace computation task for monitoring.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Replace extends Compute {

	private static final long serialVersionUID = 1L;

	/**
	 * The column index used in the Replace computation.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	private Integer column;

	/**
	 * The existing value to be replaced.
	 */
	private String existingValue;

	/**
	 * The new value to replace the existing value.
	 */
	private String newValue;

	/**
	 * Replace constructor using the Builder pattern.
	 *
	 * @param type          The type of the computation task.
	 * @param column        The column index used in the computation.
	 * @param existingValue The existing value to be replaced.
	 * @param newValue      The new value to replace the existing value.
	 */
	@Builder
	@JsonCreator
	public Replace(
		@JsonProperty("type") String type,
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty(value = "existingValue") String existingValue,
		@JsonProperty(value = "newValue") String newValue
	) {
		super(type);
		this.column = column;
		this.existingValue = existingValue;
		this.newValue = newValue;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- existingValue=", existingValue);
		addNonNull(stringJoiner, "- newValue=", newValue);

		return stringJoiner.toString();
	}

	@Override
	public Replace copy() {
		return Replace.builder().type(type).column(column).existingValue(existingValue).newValue(newValue).build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		existingValue = updater.apply(existingValue);
		newValue = updater.apply(newValue);
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
