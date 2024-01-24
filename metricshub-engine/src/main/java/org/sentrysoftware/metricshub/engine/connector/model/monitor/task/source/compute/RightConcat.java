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
 * Represents a RightConcat computation task for monitoring.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RightConcat extends AbstractConcat {

	private static final long serialVersionUID = 1L;

	/**
	 * RightConcat constructor using the Builder pattern.
	 *
	 * @param type   The type of the computation task.
	 * @param column The column index used in the computation.
	 * @param value  The value to be concatenated.
	 */
	@Builder
	@JsonCreator
	public RightConcat(
		@JsonProperty("type") String type,
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty(value = "value", required = true) @NonNull String value
	) {
		super(type, column, value);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public RightConcat copy() {
		return RightConcat.builder().type(type).column(column).value(value).build();
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
