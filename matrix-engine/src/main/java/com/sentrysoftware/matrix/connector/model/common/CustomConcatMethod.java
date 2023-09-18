package com.sentrysoftware.matrix.connector.model.common;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class CustomConcatMethod implements IEntryConcatMethod {

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = FAIL)
	@NonNull
	private String concatStart;

	@JsonSetter(nulls = FAIL)
	@NonNull
	private String concatEnd;

	@Builder
	@JsonCreator
	public CustomConcatMethod(
		@JsonProperty(value = "concatStart", required = true) @NonNull String concatStart,
		@JsonProperty(value = "concatEnd", required = true) @NonNull String concatEnd
	) {
		this.concatStart = concatStart;
		this.concatEnd = concatEnd;
	}

	@Override
	public CustomConcatMethod copy() {
		return CustomConcatMethod.builder().concatStart(concatStart).concatEnd(concatEnd).build();
	}

	@Override
	public String getDescription() {
		return String.format("custom[concatStart=%s, concatEnd=%s]", concatStart, concatEnd);
	}
}
