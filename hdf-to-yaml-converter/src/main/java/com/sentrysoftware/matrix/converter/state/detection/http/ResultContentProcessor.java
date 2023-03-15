package com.sentrysoftware.matrix.converter.state.detection.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class ResultContentProcessor extends AbstractStateConverter {

	private static final String HDF_HTTP_STATUS = "http_status";

	private static final String HDF_HTTPSTATUS = "httpstatus";

	private static final String YAML_HTTP_STATUS = "httpStatus";

	private static final Pattern RESULT_CONTENT_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.resultcontent\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {

		final ObjectNode criterion = ((ObjectNode) getLastCriterion(key, connector));

		final String resultContent;
		if (HDF_HTTP_STATUS.equalsIgnoreCase(value) || HDF_HTTPSTATUS.equalsIgnoreCase(value)) {
			resultContent = YAML_HTTP_STATUS;
		} else {
			resultContent = value.toLowerCase(); // body, all, header
		}

		criterion.set("resultContent", JsonNodeFactory.instance.textNode(resultContent));
	}

	@Override
	protected Matcher getMatcher(String key) {
		return RESULT_CONTENT_KEY_PATTERN.matcher(key);
	}
}
