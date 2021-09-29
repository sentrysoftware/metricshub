package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http;

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
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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

}
