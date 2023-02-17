package com.sentrysoftware.matrix.connector.model.identity.criterion;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HttpCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = SKIP)
	private HttpMethod method = HttpMethod.GET;
	@NonNull
	@JsonSetter(nulls = FAIL)
	private String url;
	// String or EmbeddedFile reference
	private String header;
	private String body;
	private String expectedResult;
	private String errorMessage;
	@JsonSetter(nulls = SKIP)
	private ResultContent resultContent = ResultContent.BODY;
	private String authenticationToken;

	@Builder
	@JsonCreator
	public HttpCriterion( // NOSONAR
		@JsonProperty("type") String type,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty("method") HttpMethod method,
		@JsonProperty(value = "url", required =  true) @NonNull String url,
		@JsonProperty("header") String header,
		@JsonProperty("body") String body,
		@JsonProperty("expectedResult") String expectedResult,
		@JsonProperty("errorMessage") String errorMessage,
		@JsonProperty("resultContent") ResultContent resultContent,
		@JsonProperty("authenticationToken") String authenticationToken
	) {

		super(type, forceSerialization);
		this.method = method == null ? HttpMethod.GET : method;
		this.url = url;
		this.header = header;
		this.body = body;
		this.expectedResult = expectedResult;
		this.errorMessage = errorMessage;
		this.resultContent = resultContent == null ? ResultContent.BODY : resultContent;
		this.authenticationToken = authenticationToken;
	}

}
