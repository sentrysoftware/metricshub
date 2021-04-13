package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.ONE;
import static org.junit.jupiter.api.Assertions.*;

class ForceSerializationProcessorTest {

    private final ForceSerializationProcessor forceSerializationProcessor = new ForceSerializationProcessor();

    private final Connector connector = new Connector();

    private static final String CRITERION_1_FORCE_SERIALIZATION_KEY = "detection.criteria(1).forceserialization";
    private static final String CRITERION_2_FORCE_SERIALIZATION_KEY = "detection.criteria(2).forceserialization";
    private static final String FOO = "FOO";

    @Test
    void testParse() {

        // Valid arguments
        forceSerializationProcessor.parse(CRITERION_1_FORCE_SERIALIZATION_KEY, FOO, connector);
        forceSerializationProcessor.parse(CRITERION_2_FORCE_SERIALIZATION_KEY, ONE, connector);

        Detection detection = connector.getDetection();
        assertNotNull(detection);
        List<Criterion> criteria = detection.getCriteria();
        assertEquals(2, criteria.size());

        Criterion criterion = criteria.get(0);
        assertTrue(criterion instanceof SNMPGetNext);
        SNMPGetNext snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertFalse(snmpGetNextCriterion.isForceSerialization());

        criterion = criteria.get(1);
        assertTrue(criterion instanceof SNMPGetNext);
        snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertTrue(snmpGetNextCriterion.isForceSerialization());
    }
}