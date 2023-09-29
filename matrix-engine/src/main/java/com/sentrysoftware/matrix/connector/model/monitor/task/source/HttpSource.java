package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;
import com.sentrysoftware.matrix.strategy.source.ISourceProcessor;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HttpSource extends Source {

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = SKIP)
	private HttpMethod method = HttpMethod.GET;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private String url;

	// String or EmbeddedFile reference
	private String header;
	private String body;
	private String authenticationToken;

	@JsonSetter(nulls = SKIP)
	private ResultContent resultContent = ResultContent.BODY;

	@Builder
	public HttpSource(
		@JsonProperty("type") String type,
		@JsonProperty("computes") List<Compute> computes,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty("method") HttpMethod method,
		@JsonProperty(value = "url", required = true) @NonNull String url,
		@JsonProperty("header") String header,
		@JsonProperty("body") String body,
		@JsonProperty("authenticationToken") String authenticationToken,
		@JsonProperty("resultContent") ResultContent resultContent,
		@JsonProperty("key") String key,
		@JsonProperty("executeForEachEntryOf") ExecuteForEachEntryOf executeForEachEntryOf
	) {
		super(type, computes, forceSerialization, key, executeForEachEntryOf);
		this.method = method;
		this.url = url;
		this.header = header;
		this.body = body;
		this.authenticationToken = authenticationToken;
		this.resultContent = resultContent == null ? ResultContent.BODY : resultContent;
	}

	public HttpSource copy() {
		return HttpSource
			.builder()
			.type(type)
			.key(key)
			.forceSerialization(forceSerialization)
			.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
			.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
			.method(method)
			.url(url)
			.header(header)
			.body(body)
			.authenticationToken(authenticationToken)
			.resultContent(resultContent)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		url = updater.apply(url);
		header = updater.apply(header);
		body = updater.apply(body);
		authenticationToken = updater.apply(authenticationToken);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- method=", method);
		addNonNull(stringJoiner, "- url=", url);
		addNonNull(stringJoiner, "- header=", header);
		addNonNull(stringJoiner, "- body=", body);
		addNonNull(stringJoiner, "- authenticationToken=", authenticationToken);
		addNonNull(stringJoiner, "- resultContent=", resultContent != null ? resultContent.getName() : null);

		return stringJoiner.toString();
	}

	@Override
	public SourceTable accept(final ISourceProcessor sourceProcessor) {
		return sourceProcessor.process(this);
	}
}
