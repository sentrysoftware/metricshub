package com.sentrysoftware.matrix.converter.state.detection.snmp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;

public class OidProcessor extends AbstractStateConverter {

	private static final String SNMP_GET_OID_KEY = ".snmpget";

	private static final Pattern OID_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.snmpget(next)?\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final JsonNode criteria = TypeProcessor.getOrCreateCriteria(connector);

		final String type = key.trim().endsWith(SNMP_GET_OID_KEY) ? "snmpGet" : "snmpGetNext";

		final JsonNode criterion = JsonNodeFactory.instance.objectNode();
		final Matcher matcher = getMatcher(key);
		matcher.find();
		String index = matcher.group(1);
		String typeKey = String.format("detection.criteria(%s).type", index);
		if (preConnector.getComments().containsKey(typeKey)) {
			final String comments = preConnector.getComments().get(typeKey).stream().collect(Collectors.joining("\n"));
			((ObjectNode) criterion).set("_comment", JsonNodeFactory.instance.textNode(comments));
		}

		((ObjectNode) criterion).set("type", JsonNodeFactory.instance.textNode(type));
		((ObjectNode) criterion).set("oid", JsonNodeFactory.instance.textNode(value));
		((ArrayNode) criteria).add(criterion);
	}

	@Override
	protected Matcher getMatcher(String key) {
		return OID_KEY_PATTERN.matcher(key);
	}

}
