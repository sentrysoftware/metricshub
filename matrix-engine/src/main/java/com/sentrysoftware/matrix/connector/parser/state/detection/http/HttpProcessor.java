package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.model.detection.criteria.http.Http;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class HttpProcessor extends AbstractStateParser {

	public static final String HTTP_TYPE_VALUE = "HTTP";

	@Override
	public Class<Http> getType() {
		return Http.class;
	}

	@Override
	public String getTypeValue() {
		return HTTP_TYPE_VALUE;
	}
}
