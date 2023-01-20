package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HttpSource extends Source {

	private static final long serialVersionUID = 1L;

	private String method;
	private String url;
	// String or EmbeddedFile reference
	private String header;
	private String body;
	private String authenticationToken;
	private ResultContent resultContent = ResultContent.BODY;

	@Builder
	public HttpSource( // NOSONAR on contructor
		String type,
		List<Compute> computes,
		boolean forceSerialization,
		String method,
		String url,
		String header,
		String body,
		String authenticationToken,
		ResultContent resultContent,
		String key,
		ExecuteForEachEntry executeForEachEntry
	) {

		super(type, computes, forceSerialization, key, executeForEachEntry);

		this.method = method;
		this.url = url;
		this.header = header;
		this.body = body;
		this.authenticationToken = authenticationToken;
		this.resultContent = resultContent == null ? ResultContent.BODY : resultContent;
	}

	public HttpSource copy() {
		return HttpSource.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
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
		method = updater.apply(method);
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

}
