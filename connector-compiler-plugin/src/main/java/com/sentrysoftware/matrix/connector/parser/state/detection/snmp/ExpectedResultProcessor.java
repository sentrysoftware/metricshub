package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.DOUBLE_QUOTE;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.EMPTY_STRING;
import static org.springframework.util.Assert.notNull;

public class ExpectedResultProcessor extends SnmpProcessor {

    private static final String EXPECTED_RESULT_KEY_REGEX = "^\\s*detection\\.criteria\\((\\d+)\\)\\.expectedresult\\s*$";

    @Override
    protected String getKeyRegex() {
        return EXPECTED_RESULT_KEY_REGEX;
    }

    @Override
    public void parse(final String key, final String value, final Connector connector) {

        super.parse(key, value, connector);

        // Setting the expected result
        notNull(knownCriterion, "knownCriterion should not be null.");
        ((SNMP) knownCriterion).setExpectedResult(value.trim().replace(DOUBLE_QUOTE, EMPTY_STRING));
    }
}
