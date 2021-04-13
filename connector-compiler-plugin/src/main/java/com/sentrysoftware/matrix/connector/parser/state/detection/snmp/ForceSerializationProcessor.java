package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.ONE;

public class ForceSerializationProcessor extends SnmpProcessor {

    private static final String FORCE_SERIALIZATION_KEY_REGEX = "^\\s*detection\\.criteria\\((\\d+)\\)\\.forceserialization\\s*$";

    @Override
    protected String getKeyRegex() {
        return FORCE_SERIALIZATION_KEY_REGEX;
    }

    @Override
    public void parse(final String key, final String value, final Connector connector) {

        super.parse(key, value, connector);

        // connector, connector.getDetection() and criteria are never null here,
        // and criterionIndex is always in [0; criteria.size()[
        Criterion criterion = connector.getDetection().getCriteria().get(criterionIndex - 1);

        isSnmp(criterion);

        // Setting the expected result
        criterion.setForceSerialization(value.trim().equals(ONE));
    }
}
