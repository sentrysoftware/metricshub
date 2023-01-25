package com.sentrysoftware.matrix.connector.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomConcatMethod implements IEntryConcatMethod {

	private static final long serialVersionUID = 1L;

	private String concatStart;
	private String concatEnd;

	@Override
	public CustomConcatMethod copy() {
		return CustomConcatMethod
			.builder()
			.concatStart(concatStart)
			.concatEnd(concatEnd)
			.build();
	}

	@Override
	public String getDescription() {
		return String.format("custom[concatStart=%s, concatEnd=%s]", concatStart, concatEnd);
	}
}
