package org.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.sentrysoftware.metricshub.engine.connector.model.common.HttpMethod;
import org.sentrysoftware.metricshub.engine.connector.model.common.ResultContent;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;

/**
 * Connector detection criterion using HTTP protocol.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HttpCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	/**
	 * HTTP method for the criterion.
	 */
	@JsonSetter(nulls = SKIP)
	private HttpMethod method = HttpMethod.GET;

	/**
	 * URL for the HTTP criterion (required).
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	private String url;

	/**
	 * String or EmbeddedFile reference for the HTTP criterion.
	 */
	private String header;

	/**
	 * Body for the HTTP criterion.
	 */
	private String body;

	/**
	 * Expected result for the HTTP criterion.
	 */
	private String expectedResult;

	/**
	 * Error message for the HTTP criterion.
	 */
	private String errorMessage;

	/**
	 * Result content for the HTTP criterion.
	 */
	@JsonSetter(nulls = SKIP)
	private ResultContent resultContent = ResultContent.BODY;

	/**
	 * Authentication token for the HTTP criterion.
	 */
	private String authenticationToken;

	/**
	 * Constructor with builder for creating an instance of HttpCriterion.
	 *
	 * @param type                Type of the criterion.
	 * @param forceSerialization Flag indicating whether serialization should be forced.
	 * @param method              HTTP method for the test.
	 * @param url                 URL for the HTTP test.
	 * @param header              Header for the HTTP test.
	 * @param body                Body for the HTTP test.
	 * @param expectedResult      Expected result for the HTTP test.
	 * @param errorMessage        Error message for the HTTP test.
	 * @param resultContent       Result content for the HTTP test.
	 * @param authenticationToken Authentication token for the HTTP test.
	 */
	@Builder
	@JsonCreator
	public HttpCriterion(
		@JsonProperty("type") String type,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty("method") HttpMethod method,
		@JsonProperty(value = "url", required = true) @NonNull String url,
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

	/**
	 * Accepts the given criterion processor for evaluation.
	 *
	 * @param criterionProcessor The criterion processor to accept.
	 * @return The result of the criterion detection.
	 */
	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}
}
