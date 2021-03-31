package com.sentrysoftware.matrix.connector.model.detection.criteria.ucs;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criteria;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UCS extends Criteria {

	private static final long serialVersionUID = 3035383624414379693L;

	private String query;
	private String errorMessage;
	private String expectedResult;

	@Builder
	public UCS(boolean forceSerialization, String query, String errorMessage, String expectedResult) {

		super(forceSerialization);
		this.query = query;
		this.errorMessage = errorMessage;
		this.expectedResult = expectedResult;
	}

}
