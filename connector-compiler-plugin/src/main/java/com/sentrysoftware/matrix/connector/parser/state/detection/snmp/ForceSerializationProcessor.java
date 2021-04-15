package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.ONE;
import static com.sentrysoftware.matrix.utils.Assert.notNull;

public class ForceSerializationProcessor extends SnmpProcessor {

    private static final String FORCE_SERIALIZATION_KEY_REGEX = "^\\s*detection\\.criteria\\((\\d+)\\)\\.forceserialization\\s*$";

    @Override
    protected String getKeyRegex() {
        return FORCE_SERIALIZATION_KEY_REGEX;
    }

    @Override
    public void parse(final String key, final String value, final Connector connector) {

        super.parse(key, value, connector);

        // Setting the expected result
        notNull(knownCriterion, "knownCriterion should not be null.");
        knownCriterion.setForceSerialization(value.trim().equals(ONE));
    }
}
