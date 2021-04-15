package com.sentrysoftware.matrix.connector.model.detection.criteria.wbem;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WBEM extends Criterion {

	private static final long serialVersionUID = -7417503756436261103L;

	private String wbemQuery;
	private String wbemNamespace;
	private String expectedResult;

	@Builder
	public WBEM(boolean forceSerialization, String wbemQuery, String wbemNamespace, String expectedResult) {

		super(forceSerialization, null);
		this.wbemQuery = wbemQuery;
		this.wbemNamespace = wbemNamespace;
		this.expectedResult = expectedResult;
	}

}
