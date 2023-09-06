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
public class Substring extends Compute {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private Integer column;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private String start;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private String length;

	@Builder
	@JsonCreator
	public Substring(
		@JsonProperty("type") String type, 
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty(value = "start", required = true) @NonNull String start,
		@JsonProperty(value = "length", required = true) @NonNull String length
	) {

		super(type);
		this.column = column;
		this.start = start;
		this.length = length;
	}

	/**
	 * Copy the current instance
	 * 
	 * @return new {@link Substring} instance
	 */
	public Substring copy() {
		return Substring.builder()
				.type(type)
				.column(column)
				.start(start)
				.length(length)
				.build();
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- start=", start);
		addNonNull(stringJoiner, "- length=", length);

		return stringJoiner.toString();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		start = updater.apply(start);
		length = updater.apply(length);
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
