package com.sentrysoftware.matrix.connector.model.detection.criteria.http;

import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.ICriterionVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Http extends Criterion {

	private static final long serialVersionUID = -2862351783201204668L;

	private String method;
	private String url;
	// String or EmbeddedFile reference
	private Header header;
	private Body body;
	private String expectedResult;
	private String errorMessage;
	private ResultContent resultContent = ResultContent.BODY;

	@Builder
	public Http(boolean forceSerialization, String method, String url, Header header, Body body,
			String expectedResult, String errorMessage, ResultContent resultContent, int index) {

		super(forceSerialization, index);
		this.method = method;
		this.url = url;
		this.header = header;
		this.body = body;
		this.expectedResult = expectedResult;
		this.errorMessage = errorMessage;
		this.resultContent = resultContent == null ? ResultContent.BODY : resultContent;
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}

}
