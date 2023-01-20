package com.sentrysoftware.matrix.connector.model.identity.criterion;

import com.sentrysoftware.matrix.connector.model.common.ResultContent;

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

	private static final long serialVersionUID = 1L;

	private String method;
	private String url;
	// String or EmbeddedFile reference
	private String header;
	private String body;
	private String expectedResult;
	private String errorMessage;
	private ResultContent resultContent = ResultContent.BODY;
	private String authenticationToken;

	@Builder
	public Http( // NOSONAR
		String type,
		boolean forceSerialization,
		String method,
		String url,
		String header,
		String body,
		String expectedResult,
		String errorMessage,
		ResultContent resultContent,
		String authenticationToken
	) {

		super(type, forceSerialization);
		this.method = method;
		this.url = url;
		this.header = header;
		this.body = body;
		this.expectedResult = expectedResult;
		this.errorMessage = errorMessage;
		this.resultContent = resultContent == null ? ResultContent.BODY : resultContent;
		this.authenticationToken = authenticationToken;
	}

}
