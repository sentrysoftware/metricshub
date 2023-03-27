package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KeepOnlyMatchingLines extends AbstractMatchingLines {

	private static final long serialVersionUID = 1L;

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
		return KeepOnlyMatchingLines
			.builder()
			.type(type)
			.column(column)
			.regExp(regExp)
			.valueList(valueList)
			.build();

	}
}
