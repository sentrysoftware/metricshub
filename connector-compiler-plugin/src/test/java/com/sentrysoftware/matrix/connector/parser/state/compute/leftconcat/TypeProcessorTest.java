package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TypeProcessorTest {

    private final TypeProcessor typeProcessor = new TypeProcessor();

    private final Connector connector = new Connector();
    private static final String LEFT_CONCAT_TYPE_KEY_1 = "enclosure.discovery.source(1).compute(1).type";
    private static final String LEFT_CONCAT_TYPE_KEY_2 = "enclosure.discovery.source(1).compute(2).type";
    private static final String FOO = "FOO";
    private static final String LEFT_CONCAT_TYPE_VALUE = "LeftConcat";

    @Test
    void testParse() {

        // Key does not match
        assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, FOO, connector));

        // Key matches, value is invalid
        assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(LEFT_CONCAT_TYPE_KEY_1, FOO, connector));

        // Key matches, value is valid, no Source found
        typeProcessor.parse(LEFT_CONCAT_TYPE_KEY_1, LEFT_CONCAT_TYPE_VALUE, connector);
        assertTrue(connector.getHardwareMonitors().isEmpty());

        // Key matches, value is valid, Source found, source.getComputes() == null
        SNMPGetTableSource source = SNMPGetTableSource
                .builder()
                .index(1)
                .build();

        connector
                .getHardwareMonitors()
                .add(
                        HardwareMonitor
                                .builder()
                                .type(MonitorType.ENCLOSURE)
                                .discovery(
                                        Discovery
                                                .builder()
                                                .sources(
                                                        Collections.singletonList(source)
                                                )
                                                .build()
                                )
                                .build()
                );
        typeProcessor.parse(LEFT_CONCAT_TYPE_KEY_1, LEFT_CONCAT_TYPE_VALUE, connector);
        assertNotNull(source.getComputes());
        assertEquals(1, source.getComputes().size());
        Compute compute = source.getComputes().get(0);
        assertTrue(compute instanceof LeftConcat);
        assertEquals(1, compute.getIndex());

        // Key matches, value is valid, Source found, source.getComputes() != null
        typeProcessor.parse(LEFT_CONCAT_TYPE_KEY_2, LEFT_CONCAT_TYPE_VALUE, connector);
        assertNotNull(source.getComputes());
        assertEquals(2, source.getComputes().size());
        compute = source.getComputes().get(0);
        assertTrue(compute instanceof LeftConcat);
        assertEquals(1, compute.getIndex());
        compute = source.getComputes().get(1);
        assertTrue(compute instanceof LeftConcat);
        assertEquals(2, compute.getIndex());
    }
}