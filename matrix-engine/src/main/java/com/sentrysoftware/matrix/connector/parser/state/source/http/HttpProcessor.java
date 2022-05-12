package com.sentrysoftware.matrix.connector.parser.state.source.http;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HttpSource;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class HttpProcessor extends AbstractStateParser {

	public static final String HTTP_TYPE_VALUE = "HTTP";

	@Override
	public Class<HttpSource> getType() {
		return HttpSource.class;
	}

	@Override
	public String getTypeValue() {
		return HTTP_TYPE_VALUE;
	}
}
