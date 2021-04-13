package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnmpProcessorTest {

    private final SnmpProcessor oidProcessor = new OidProcessor();
    private final SnmpProcessor expectedResultProcessor = new ExpectedResultProcessor();

    private final Connector connector = new Connector();

    private static final String CRITERION_0_OID_KEY = "detection.criteria(0).snmpgetnext";
    private static final String CRITERION_1_OID_KEY = "detection.criteria(1).snmpgetnext";
    private static final String CRITERION_2_OID_KEY = "detection.criteria(2).snmpgetnext";
    private static final String CRITERION_2_EXPECTED_RESULT = "detection.criteria(2).expectedresult";
    private static final String CRITERION_3_OID_KEY = "detection.criteria(3).snmpgetnext";
    private static final String FOO = "FOO";
    private static final String BAR = "BAR";
    private static final String BAZ = "BAZ";
    private static final String QUX = "QUX";

    @Test
    void testDetect() {

        assertFalse(oidProcessor.detect(null, null, null));
        assertFalse(oidProcessor.detect(null, FOO, null));
        assertFalse(oidProcessor.detect(CRITERION_1_OID_KEY, null, null));
        assertTrue(oidProcessor.detect(CRITERION_1_OID_KEY, FOO, null));
    }

    @Test
    void testParseInvalidArguments() {

        assertThrows(IllegalArgumentException.class, () -> oidProcessor.parse(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> oidProcessor.parse(null, null, connector));
        assertThrows(IllegalArgumentException.class, () -> oidProcessor.parse(null, FOO, connector));
        assertThrows(IllegalArgumentException.class, () -> oidProcessor.parse(null, FOO, null));
        assertThrows(IllegalArgumentException.class, () -> oidProcessor.parse(CRITERION_1_OID_KEY, FOO, null));
        assertThrows(IllegalArgumentException.class, () -> oidProcessor.parse(CRITERION_1_OID_KEY, null, null));
        assertThrows(IllegalArgumentException.class, () -> oidProcessor.parse(CRITERION_1_OID_KEY, null, connector));
        assertThrows(IllegalArgumentException.class, () -> oidProcessor.parse(CRITERION_0_OID_KEY, FOO, connector));
    }

    @Test
    void testParse() {

        // Valid arguments, only one criterion
        oidProcessor.parse(CRITERION_1_OID_KEY, FOO, connector);
        Detection detection = connector.getDetection();
        assertNotNull(detection);
        List<Criterion> criteria = detection.getCriteria();
        assertEquals(1, criteria.size());
        Criterion criterion = criteria.get(0);
        assertTrue(criterion instanceof SNMPGetNext);
        SNMPGetNext snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertEquals(FOO, snmpGetNextCriterion.getOid());

        // Valid arguments, 2 additional criteria
        oidProcessor.parse(CRITERION_2_OID_KEY, BAR, connector);
        oidProcessor.parse(CRITERION_3_OID_KEY, BAZ, connector);

        detection = connector.getDetection();
        assertNotNull(detection);
        criteria = detection.getCriteria();
        assertEquals(3, criteria.size());

        criterion = criteria.get(0);
        assertTrue(criterion instanceof SNMPGetNext);
        snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertEquals(FOO, snmpGetNextCriterion.getOid());

        criterion = criteria.get(1);
        assertTrue(criterion instanceof SNMPGetNext);
        snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertEquals(BAR, snmpGetNextCriterion.getOid());

        criterion = criteria.get(2);
        assertTrue(criterion instanceof SNMPGetNext);
        snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertEquals(BAZ, snmpGetNextCriterion.getOid());

        // Valid arguments, adding ExpectedResult value to the second criterion
        expectedResultProcessor.parse(CRITERION_2_EXPECTED_RESULT, QUX, connector);

        detection = connector.getDetection();
        assertNotNull(detection);
        criteria = detection.getCriteria();
        assertEquals(3, criteria.size());

        criterion = criteria.get(0);
        assertTrue(criterion instanceof SNMPGetNext);
        snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertEquals(FOO, snmpGetNextCriterion.getOid());
        assertNull(snmpGetNextCriterion.getExpectedResult());

        criterion = criteria.get(1);
        assertTrue(criterion instanceof SNMPGetNext);
        snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertEquals(BAR, snmpGetNextCriterion.getOid());
        assertEquals(QUX, snmpGetNextCriterion.getExpectedResult());

        criterion = criteria.get(2);
        assertTrue(criterion instanceof SNMPGetNext);
        snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertEquals(BAZ, snmpGetNextCriterion.getOid());
        assertNull(snmpGetNextCriterion.getExpectedResult());
    }
}