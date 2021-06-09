package com.sentrysoftware.matrix.connector.parser.state.detection.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;
import lombok.AllArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.ONE;

@AllArgsConstructor
public class ForceSerializationProcessor extends AbstractStateParser {

	private final Class<? extends Criterion> type;
	private final String typeValue;

	private static final Pattern FORCE_SERIALIZATION_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.forceserialization\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	protected Class<?> getType() {
		return type;
	}

	@Override
	protected String getTypeValue() {
		return typeValue;
	}

	@Override
	public Matcher getMatcher(String key) {
		return FORCE_SERIALIZATION_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		getCriterion(key, connector).setForceSerialization(ONE.equals(value));
	}
}
