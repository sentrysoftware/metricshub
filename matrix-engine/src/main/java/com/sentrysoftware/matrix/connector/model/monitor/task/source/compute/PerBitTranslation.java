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
public class PerBitTranslation extends Compute {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private Integer column;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private String bitList;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private String translationTable;

	@Builder
	@JsonCreator
	public PerBitTranslation(
		@JsonProperty("type") String type, 
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty(value = "bitList", required = true) @NonNull String bitList,
		@JsonProperty(value = "translationTable", required = true) @NonNull String translationTable
	) {

		super(type);
		this.column = column;
		this.bitList = bitList;
		this.translationTable = translationTable;
	}


	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- bitList=", bitList);
		addNonNull(stringJoiner, "- translationTable=", translationTable);

		return stringJoiner.toString();

	}

	@Override
	public PerBitTranslation copy() {
		return PerBitTranslation
			.builder()
			.type(type)
			.column(column)
			.bitList(bitList)
			.translationTable(translationTable)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		translationTable = updater.apply(translationTable);
	}
}
