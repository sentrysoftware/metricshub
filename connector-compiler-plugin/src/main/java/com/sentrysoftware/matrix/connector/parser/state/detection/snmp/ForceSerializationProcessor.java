package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.ONE;
import static org.springframework.util.Assert.notNull;

import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;

public class ForceSerializationProcessor extends SnmpProcessor {


	protected static final Pattern FORCE_SERIALIZATION_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\((\\d+)\\)\\.forceserialization\\s*$", 
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Pattern getKeyRegex() {
		return FORCE_SERIALIZATION_KEY_PATTERN;
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		// Setting the expected result
		notNull(knownCriterion, "knownCriterion should not be null.");
		knownCriterion.setForceSerialization(value.trim().equals(ONE));
	}
}
