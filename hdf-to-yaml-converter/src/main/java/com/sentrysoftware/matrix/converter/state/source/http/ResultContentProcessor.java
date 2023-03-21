package com.sentrysoftware.matrix.converter.state.source.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class ResultContentProcessor extends AbstractStateConverter {

	private static final String HDF_HTTP_STATUS = "http_status";

	private static final String HDF_HTTPSTATUS = "httpstatus";

	private static final String YAML_HTTP_STATUS = "httpStatus";

	private static final Pattern PATTERN = Pattern.compile(
		SOURCE_REGEX_START + "resultcontent" + REGEX_END,
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {

		final String resultContent;
		if (HDF_HTTP_STATUS.equalsIgnoreCase(value) || HDF_HTTPSTATUS.equalsIgnoreCase(value)) {
			resultContent = YAML_HTTP_STATUS;
		} else {
			resultContent = value.toLowerCase(); // body, all, header
		}

		createSourceTextNode(key, resultContent, connector, "resultContent");
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}
