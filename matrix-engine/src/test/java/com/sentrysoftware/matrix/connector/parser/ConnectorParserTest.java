package com.sentrysoftware.matrix.connector.parser;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorParserTest {

    private final ConnectorParser connectorParser = new ConnectorParser();

    private static final String MS_HW_DELL_OPEN_MANAGE_HDFS_PATH = "src/test/resources/hdf/MS_HW_DellOpenManage.hdfs";

    private static final String EMPTY_STRING = "";
    private static final String SPACE = " ";
    private static final String FOO = "FOO";

    private static final String OID = "1.3.6.1.4.1.674.10892.1.300.10.1";

    @Test
    void testParse() {

        // Invalid paths
        assertThrows(IllegalArgumentException.class, () -> connectorParser.parse(null));
        assertThrows(IllegalArgumentException.class, () -> connectorParser.parse(EMPTY_STRING));
        assertThrows(IllegalArgumentException.class, () -> connectorParser.parse(SPACE));

        // Valid path, but file does not exist => IllegalStateException thrown
        assertThrows(IllegalStateException.class, () -> connectorParser.parse(FOO));

        // Valid path, no Exception thrown
        Connector connector = connectorParser.parse(MS_HW_DELL_OPEN_MANAGE_HDFS_PATH);
        assertNotNull(connector);

        Detection detection = connector.getDetection();
        assertNotNull(detection);

        List<Criterion> criteria = detection.getCriteria();
        assertNotNull(criteria);
        assertEquals(1, criteria.size());

        Criterion criterion = criteria.get(0);
        assertTrue(criterion instanceof SNMPGetNext);

        SNMPGetNext snmpGetNextCriterion = (SNMPGetNext) criterion;
        assertEquals(OID, snmpGetNextCriterion.getOid());
        assertNull(snmpGetNextCriterion.getExpectedResult());
        assertFalse(snmpGetNextCriterion.isForceSerialization());
    }
}