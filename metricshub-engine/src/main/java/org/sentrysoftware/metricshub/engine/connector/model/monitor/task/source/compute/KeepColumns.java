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
 * Represents a KeepColumns computation task for monitoring.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KeepColumns extends Compute {

	private static final long serialVersionUID = 1L;

	/**
	 * The column numbers to be kept in the KeepColumns computation.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	private String columnNumbers;

	/**
	 * KeepColumns constructor using the Builder pattern.
	 *
	 * @param type          The type of the computation task.
	 * @param columnNumbers The column numbers to be kept.
	 */
	@Builder
	@JsonCreator
	public KeepColumns(
		@JsonProperty("type") String type,
		@JsonProperty(value = "columnNumbers", required = true) @NonNull String columnNumbers
	) {
		super(type);
		this.columnNumbers = columnNumbers;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- columnNumbers=", columnNumbers);

		return stringJoiner.toString();
	}

	@Override
	public KeepColumns copy() {
		return KeepColumns.builder().type(type).columnNumbers(columnNumbers).build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		columnNumbers = updater.apply(columnNumbers);
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
