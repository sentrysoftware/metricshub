package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.DOUBLE_QUOTE;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.EMPTY_STRING;

public class ExpectedResultProcessor extends SnmpProcessor {

    private static final String EXPECTED_RESULT_KEY_REGEX = "^\\s*detection\\.criteria\\((\\d+)\\)\\.expectedresult\\s*$";

    @Override
    protected String getKeyRegex() {
        return EXPECTED_RESULT_KEY_REGEX;
    }

    @Override
    public void parse(final String key, final String value, final Connector connector) {

        super.parse(key, value, connector);

        // connector, connector.getDetection() and criteria are never null here,
        // and criterionIndex is always in [0; criteria.size()[
        Criterion criterion = connector.getDetection().getCriteria().get(criterionIndex - 1);

        isSnmp(criterion);

        // Setting the expected result
        ((SNMP) criterion).setExpectedResult(value.trim().replace(DOUBLE_QUOTE, EMPTY_STRING));
    }
}
