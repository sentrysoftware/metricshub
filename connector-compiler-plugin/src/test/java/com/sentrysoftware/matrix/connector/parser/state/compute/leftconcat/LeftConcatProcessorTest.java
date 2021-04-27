package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

class LeftConcatProcessorTest {

    private LeftConcatProcessor typeProcessor;

    private Connector connector;

    private static final String FOO = "FOO";
    private static final String LEFT_CONCAT_TYPE_KEY = "enclosure.discovery.source(1).compute(1).type";
    private static final String LEFT_CONCAT_TYPE_VALUE = "LeftConcat";


    @BeforeEach
    void setUp() {

        typeProcessor = new TypeProcessor();
        connector = new Connector();
    }

    @Test
    void testDetect() {

        assertFalse(typeProcessor.detect(null, null, null));
        assertFalse(typeProcessor.detect(null, FOO, null));
        assertFalse(typeProcessor.detect(FOO, FOO, null));
        assertFalse(typeProcessor.detect(LEFT_CONCAT_TYPE_KEY, FOO, null));
        assertTrue(typeProcessor.detect(LEFT_CONCAT_TYPE_KEY, LEFT_CONCAT_TYPE_VALUE, null));
    }

    @Test
    void testParse() {

        assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, null, null));
        assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, FOO, null));
        assertDoesNotThrow(() -> typeProcessor.parse(LEFT_CONCAT_TYPE_KEY, LEFT_CONCAT_TYPE_VALUE, connector));
    }

    @Test
    void testLeftConcat() {

        // No Source found
        Matcher matcher = typeProcessor.getMatcher(LEFT_CONCAT_TYPE_KEY);
        assertTrue(matcher.matches());
        assertNull(typeProcessor.getLeftConcat(matcher, connector));

        // Source found
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
                                                        Collections.singletonList(
                                                                SNMPGetTableSource
                                                                        .builder()
                                                                        .index(1)
                                                                        .build())
                                                )
                                                .build()
                                )
                                .build()
                );
        assertNull(typeProcessor.getLeftConcat(matcher, connector));
    }
}