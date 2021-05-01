package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.DOUBLE_QUOTE;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.EMPTY_STRING;
import static org.springframework.util.Assert.notNull;

import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;

public class ExpectedResultProcessor extends SnmpProcessor {

	protected static final Pattern EXPECTED_RESULT_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\((\\d+)\\)\\.expectedresult\\s*$", 
			Pattern.CASE_INSENSITIVE);
	
	@Override
	protected Pattern getKeyRegex() {
		return EXPECTED_RESULT_KEY_PATTERN;
	}
	
	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		// Setting the expected result
		notNull(knownCriterion, "knownCriterion should not be null.");
		((SNMP) knownCriterion).setExpectedResult(value.trim().replace(DOUBLE_QUOTE, EMPTY_STRING));
	}
}
