package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.DOUBLE_QUOTE;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.EMPTY_STRING;
import static org.springframework.util.Assert.notNull;

public class OidProcessor extends SnmpProcessor {

    private static final String OID_KEY_REGEX = "^\\s*(detection\\.criteria\\((\\d+)\\)\\.snmpget)(next)?\\s*$";
    private static final String SNMP_GET_OID_KEY = ".snmpget";

    @Override
    protected String getKeyRegex() {
        return OID_KEY_REGEX;
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
