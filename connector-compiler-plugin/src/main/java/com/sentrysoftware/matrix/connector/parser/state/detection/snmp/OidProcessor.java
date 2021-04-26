package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.DOUBLE_QUOTE;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.EMPTY_STRING;
import static org.springframework.util.Assert.notNull;

import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;

public class OidProcessor extends SnmpProcessor {

	private static final String SNMP_GET_OID_KEY = ".snmpget";

	protected static final Pattern OID_KEY_PATTERN = Pattern.compile(
			"^\\s*(detection\\.criteria\\((\\d+)\\)\\.snmpget)(next)?\\s*$", 
			Pattern.CASE_INSENSITIVE);
	
	@Override
	protected Pattern getKeyRegex() {
		return OID_KEY_PATTERN;
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		// Changing the criterion to SNMPGet if necessary
		// Note: key is never null here
		if (key.trim().endsWith(SNMP_GET_OID_KEY)) {

			notNull(knownCriterion, "knownCriterion should not be null.");

			knownCriterion = SNMPGet
					.builder()
					.index(knownCriterion.getIndex())
					.forceSerialization(knownCriterion.isForceSerialization())
					.expectedResult(((SNMPGetNext) knownCriterion).getExpectedResult())
					.build();

			connector
					.getDetection()
					.getCriteria()
					.set(criterionIndexInDetection, knownCriterion);
		}

		// Setting the OID
		((SNMP) knownCriterion).setOid(value.trim().replace(DOUBLE_QUOTE, EMPTY_STRING));
	}
}
