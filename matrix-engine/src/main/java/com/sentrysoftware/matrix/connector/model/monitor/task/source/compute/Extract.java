package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sentrysoftware.matrix.strategy.source.compute.IComputeProcessor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Extract extends Compute {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private Integer column;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private Integer subColumn;

	private String subSeparators;

	@Builder
	@JsonCreator
	public Extract(
		@JsonProperty("type") String type,
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty(value = "subColumn", required = true) @NonNull Integer subColumn,
		@JsonProperty("subSeparators") String subSeparators
	) {
		super(type);
		this.column = column;
		this.subColumn = subColumn;
		this.subSeparators = subSeparators;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- subColumn=", subColumn);
		addNonNull(stringJoiner, "- subSeparators=", subSeparators);

		return stringJoiner.toString();
	}

	@Override
	public Extract copy() {
		return Extract
			.builder()
			.type(type)
			.column(column)
			.subColumn(subColumn)
			.subSeparators(subSeparators)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		subSeparators = updater.apply(subSeparators);
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
