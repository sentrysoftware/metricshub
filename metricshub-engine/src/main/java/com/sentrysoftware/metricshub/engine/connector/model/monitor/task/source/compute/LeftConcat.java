package com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sentrysoftware.metricshub.engine.strategy.source.compute.IComputeProcessor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeftConcat extends AbstractConcat {

	private static final long serialVersionUID = 1L;

	@Builder
	@JsonCreator
	public LeftConcat(
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
	public LeftConcat copy() {
		return LeftConcat.builder().type(type).column(column).value(value).build();
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
