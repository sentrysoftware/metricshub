package com.sentrysoftware.matrix.connector.model.detection.criteria.wmi;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WMI extends Criterion {

	private static final long serialVersionUID = -2278078347788456921L;
	private String wbemQuery;
	private String wbemNamespace;
	private String expectedResult;

	@Builder
	public WMI(boolean forceSerialization, String wbemQuery, String wbemNamespace, String expectedResult) {

		super(forceSerialization);
		this.wbemQuery = wbemQuery;
		this.wbemNamespace = wbemNamespace;
		this.expectedResult = expectedResult;
	}

}
