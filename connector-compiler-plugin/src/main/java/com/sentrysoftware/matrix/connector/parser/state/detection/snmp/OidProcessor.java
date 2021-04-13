package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;

import java.util.List;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.DOUBLE_QUOTE;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.EMPTY_STRING;

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

        // connector and connector.getDetection() are never null here
        List<Criterion> criteria = connector.getDetection().getCriteria();

        // criteria is never null here
        // and criterionIndex is always in [0; criteria.size()[
        Criterion criterion = criteria.get(criterionIndex - 1);

        isSnmp(criterion);

        // Changing the criterion to SNMPGet if necessary
        // Note: key is never null here
        if (key.trim().endsWith(SNMP_GET_OID_KEY)) {

            SNMPGet snmpGetCriterion = new SNMPGet();
            snmpGetCriterion.setForceSerialization(criterion.isForceSerialization());
            snmpGetCriterion.setExpectedResult(((SNMPGetNext) criterion).getExpectedResult());

            criterion = snmpGetCriterion;
            criteria.set(criterionIndex - 1, criterion);
        }

        // Setting the OID
        ((SNMP) criterion).setOid(value.trim().replace(DOUBLE_QUOTE, EMPTY_STRING));
    }
}
