package com.sentrysoftware.matrix.converter.state.source.reference;

import static com.sentrysoftware.matrix.converter.state.ConversionHelper.SOURCE_REF_PATTERN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public class ReferenceProcessor extends AbstractStateConverter {

	private static final Pattern PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}

	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		return getMatcher(key).matches();
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {

		final ObjectNode source = createSource(key, connector);

		appendComment(key, preConnector, source);

		// If value is like "%...%", then it's a reference source, else it's a static source.
		final Matcher matcher = SOURCE_REF_PATTERN.matcher(value);
		if (matcher.find()) {
			// No need to call createTextNode for the copy text node
			source.set("type", new TextNode("copy"));
			// Call createTextNode so that all the references
			// will be correctly converted to the matrix reloaded format
			createTextNode("from", value, source);
		} else {
			source.set("type", new TextNode("static"));
			createTextNode("value", value, source);
		}
	}
}