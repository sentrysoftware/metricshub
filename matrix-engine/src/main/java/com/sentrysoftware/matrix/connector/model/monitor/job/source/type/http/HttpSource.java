package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.source.ISourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HttpSource extends Source {

	private static final long serialVersionUID = -6658120832080657988L;

	private String method;
	private String url;
	// String or EmbeddedFile reference
	private Header header;
	private Body body;
	private String authenticationToken;
	private ResultContent resultContent = ResultContent.BODY;

	@Builder
	public HttpSource(List<Compute> computes, boolean forceSerialization, String method, String url, Header header,
			Body body, String authenticationToken, ResultContent resultContent,
			int index, String key, ExecuteForEachEntry executeForEachEntry) {

		super(computes, forceSerialization, index, key, executeForEachEntry);

		this.method = method;
		this.url = url;
		this.header = header;
		this.body = body;
		this.authenticationToken = authenticationToken;
		this.resultContent = resultContent == null ? ResultContent.BODY : resultContent;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	public HttpSource copy() {
		return HttpSource.builder()
				.index(index)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.method(method)
				.url(url)
				.header(header != null ? header.copy() : null)
				.body(body != null ? body.copy() : null)
				.authenticationToken(authenticationToken)
				.resultContent(resultContent)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		method = updater.apply(method);
		url = updater.apply(url);
		if (header != null) {
			header.update(updater);
		}
		if (body != null) {
			body.update(updater);
		}
		authenticationToken = updater.apply(authenticationToken);
	}

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- method=", method);
		addNonNull(stringJoiner, "- url=", url);
		addNonNull(stringJoiner, "- header=", header != null ? header.description() : null);
		addNonNull(stringJoiner, "- body=", body != null ? body.description() : null);
		addNonNull(stringJoiner, "- authenticationToken=", authenticationToken);
		addNonNull(stringJoiner, "- resultContent=", resultContent != null ? resultContent.getName() : null);

		return stringJoiner.toString();
	}

}
