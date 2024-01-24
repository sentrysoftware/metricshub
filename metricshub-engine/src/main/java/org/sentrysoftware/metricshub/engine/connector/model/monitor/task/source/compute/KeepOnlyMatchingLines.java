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
 * Represents a KeepOnlyMatchingLines computation task for monitoring.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KeepOnlyMatchingLines extends AbstractMatchingLines {

	private static final long serialVersionUID = 1L;

	/**
	 * KeepOnlyMatchingLines constructor using the Builder pattern.
	 *
	 * @param type      The type of the computation task.
	 * @param column    The column index used in the computation.
	 * @param regExp    The regular expression used for matching lines.
	 * @param valueList The list of values used for matching lines.
	 */
	@Builder
	@JsonCreator
	public KeepOnlyMatchingLines(
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
	public KeepOnlyMatchingLines copy() {
		return KeepOnlyMatchingLines.builder().type(type).column(column).regExp(regExp).valueList(valueList).build();
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
