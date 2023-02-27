package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
public class ExcludeMatchingLines extends AbstractMatchingLines {

	private static final long serialVersionUID = 1L;

	@Builder
	@JsonCreator
	public ExcludeMatchingLines(
		@JsonProperty("type") String type, 
		@JsonProperty(value = "column", required = true) @NonNull Integer column,
		@JsonProperty("regExp") String regExp,
		@JsonProperty("valueList") Set<String> valueList
	) {

		super(type, column, regExp, valueList);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public ExcludeMatchingLines copy() {
		return ExcludeMatchingLines
			.builder()
			.type(type)
			.column(column)
			.regExp(regExp)
			.valueList(valueList == null ? null :
				valueList
					.stream()
					.collect(Collectors
						.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER))
					)
			)
			.build();

	}

}
