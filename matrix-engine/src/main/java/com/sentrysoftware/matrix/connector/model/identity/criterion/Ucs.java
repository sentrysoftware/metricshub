package com.sentrysoftware.matrix.connector.model.identity.criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ucs extends Criterion {

	private static final long serialVersionUID = 1L;

	private String query;
	private String errorMessage;
	private String expectedResult;

	@Builder
	public Ucs(String type, boolean forceSerialization, String query, String errorMessage, String expectedResult) {

		super(type, forceSerialization);
		this.query = query;
		this.errorMessage = errorMessage;
		this.expectedResult = expectedResult;
	}

}
