package com.sentrysoftware.matrix.converter.state.detection.snmp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OidProcessor extends AbstractStateConverter {

	private static final String SNMP_GET_OID_KEY = ".snmpget";

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildCriteriaKeyRegex("snmpget(next)?"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final ArrayNode criteria = getOrCreateCriteria(connector);

		final String type = key.trim().endsWith(SNMP_GET_OID_KEY) ? "snmpGet" : "snmpGetNext";

		final ObjectNode criterion = JsonNodeFactory.instance.objectNode();
		final Matcher matcher = getMatcher(key);
		matcher.find();
		final String index = matcher.group(1);
		final String typeKey = String.format("detection.criteria(%s).type", index);

		appendComment(typeKey, preConnector, criterion);

		createTextNode("type", type, criterion);
		createTextNode("oid", value, criterion);

		criteria.add(criterion);
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}
