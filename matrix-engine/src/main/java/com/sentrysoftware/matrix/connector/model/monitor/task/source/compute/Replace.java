package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Replace extends Compute {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private Integer column;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private String existingValue;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private String newValue;

	@Builder
	@JsonCreator
	public Replace(
		@JsonProperty("type") String type, 
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty(value = "existingValue", required = true) @NonNull String existingValue,
		@JsonProperty(value = "newValue", required = true) @NonNull String newValue
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
		return Replace
			.builder()
			.type(type)
			.column(column)
			.existingValue(existingValue)
			.newValue(newValue)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		existingValue = updater.apply(existingValue);
		newValue = updater.apply(newValue);
	}
}
