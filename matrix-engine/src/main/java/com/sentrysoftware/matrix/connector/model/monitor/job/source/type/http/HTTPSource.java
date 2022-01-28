package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
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

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HTTPSource extends Source {

	private static final long serialVersionUID = -6658120832080657988L;

	private String method;
	private String url;
	// String or EmbeddedFile reference
	private Header header;
	private Body body;
	private String authenticationToken;
	private String executeForEachEntryOf;
	private ResultContent resultContent = ResultContent.BODY;
	private EntryConcatMethod entryConcatMethod;
	private String entryConcatStart;
	private String entryConcatEnd;

	@Builder
	public HTTPSource(List<Compute> computes, boolean forceSerialization, String method, String url, Header header,
			Body body, String authenticationToken, String executeForEachEntryOf, ResultContent resultContent,
			EntryConcatMethod entryConcatMethod, String entryConcatStart, String entryConcatEnd, int index, String key) {

		super(computes, forceSerialization, index, key);
		this.method = method;
		this.url = url;
		this.header = header;
		this.body = body;
		this.authenticationToken = authenticationToken;
		this.executeForEachEntryOf = executeForEachEntryOf;
		this.resultContent = resultContent == null ? ResultContent.BODY : resultContent;
		this.entryConcatMethod = entryConcatMethod;
		this.entryConcatStart = entryConcatStart;
		this.entryConcatEnd = entryConcatEnd;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	public HTTPSource copy() {
		return HTTPSource.builder()
				.index(getIndex())
				.key(getKey())
				.forceSerialization(isForceSerialization())
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.method(method)
				.url(url)
				.header(header != null ? header.copy() : null)
				.body(body != null ? body.copy() : null)
				.authenticationToken(authenticationToken)
				.executeForEachEntryOf(executeForEachEntryOf)
				.resultContent(resultContent)
				.entryConcatMethod(entryConcatMethod)
				.entryConcatStart(entryConcatStart)
				.entryConcatEnd(entryConcatEnd)
				.build();
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
		addNonNull(stringJoiner, "- executeForEachEntryOf=", executeForEachEntryOf);
		addNonNull(stringJoiner, "- resultContent=", resultContent != null ? resultContent.getName() : null);
		addNonNull(stringJoiner, "- entryConcatMethod=", entryConcatMethod != null ? entryConcatMethod.getName() : null);
		addNonNull(stringJoiner, "- entryConcatStart=", entryConcatStart);
		addNonNull(stringJoiner, "- entryConcatEnd=", entryConcatEnd);

		return stringJoiner.toString();
	}

}
