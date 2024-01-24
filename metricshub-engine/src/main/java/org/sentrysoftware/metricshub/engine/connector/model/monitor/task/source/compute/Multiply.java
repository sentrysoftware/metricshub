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
 * Represents a Multiply computation task for monitoring.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Multiply extends Compute {

	private static final long serialVersionUID = 1L;

	/**
	 * The column index used in the Multiply computation.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	private Integer column;

	/**
	 * The value to be multiplied (either a number or a reference to another column).
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	private String value;

	/**
	 * Multiply constructor using the Builder pattern.
	 *
	 * @param type   The type of the computation task.
	 * @param column The column index used in the computation.
	 * @param value  The value to be multiplied.
	 */
	@Builder
	@JsonCreator
	public Multiply(
		@JsonProperty("type") String type,
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty(value = "value", required = true) @NonNull String value
	) {
		super(type);
		this.column = column;
		this.value = value;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- value=", value);

		return stringJoiner.toString();
	}

	@Override
	public Multiply copy() {
		return Multiply.builder().type(type).column(column).value(value).build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		value = updater.apply(value);
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
