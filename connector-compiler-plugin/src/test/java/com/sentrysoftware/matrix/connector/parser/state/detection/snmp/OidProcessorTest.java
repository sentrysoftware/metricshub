package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OidProcessorTest {

    private final OidProcessor oidProcessor = new OidProcessor();

    private final Connector connector = new Connector();

    private static final String CRITERION_1_OID_KEY = "detection.criteria(1).snmpgetnext";
    private static final String CRITERION_2_OID_KEY = "detection.criteria(2).snmpget";
    private static final String FOO = "FOO";
    private static final String BAR = "BAR";

    @Test
    void testParse() {

        // Valid arguments, 2 criteria (1 SNMPGetNext and 1 SNMPGet)
        oidProcessor.parse(CRITERION_1_OID_KEY, FOO, connector);
        oidProcessor.parse(CRITERION_2_OID_KEY, BAR, connector);

        Detection detection = connector.getDetection();
        assertNotNull(detection);
        List<Criterion> criteria = detection.getCriteria();
        assertEquals(2, criteria.size());

        Criterion criterion = criteria.get(0);
        assertTrue(criterion instanceof SNMPGetNext);
        SNMPGetNext snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertEquals(FOO, snmpGetNextCriterion.getOid());

        criterion = criteria.get(1);
        assertTrue(criterion instanceof SNMPGet);
        SNMPGet snmpGetCriterion = (SNMPGet) criterion;
        assertEquals(BAR, snmpGetCriterion.getOid());
    }
}