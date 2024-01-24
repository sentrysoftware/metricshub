package org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.strategy.source.compute.IComputeProcessor;

/**
 * Represents an ExcludeMatchingLines computation task for monitoring.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExcludeMatchingLines extends AbstractMatchingLines {

	private static final long serialVersionUID = 1L;

	/**
	 * ExcludeMatchingLines constructor using the Builder pattern.
	 *
	 * @param type      The type of the computation task.
	 * @param column    The column from which to extract values for matching.
	 * @param regExp    The regular expression pattern to match against the column values.
	 * @param valueList The list of values to match against the column values.
	 */
	@Builder
	@JsonCreator
	public ExcludeMatchingLines(
		@JsonProperty("type") String type,
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty("regExp") String regExp,
		@JsonProperty("valueList") String valueList
	) {
		super(type, column, regExp, valueList);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public ExcludeMatchingLines copy() {
		return ExcludeMatchingLines.builder().type(type).column(column).regExp(regExp).valueList(valueList).build();
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
