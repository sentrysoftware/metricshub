package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeepOnlyMatchingLinesProcessorTest {

    private KeepOnlyMatchingLinesProcessor typeProcessor;

    private Connector connector;

    private static final String FOO = "FOO";
    private static final String KEEP_ONLY_MATCHING_LINES_TYPE_KEY = "enclosure.discovery.source(1).compute(1).type";
    private static final String KEEP_ONLY_MATCHING_LINES_TYPE_VALUE = "KeepOnlyMatchingLines";


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
        assertFalse(typeProcessor.detect(KEEP_ONLY_MATCHING_LINES_TYPE_KEY, FOO, null));
        assertTrue(typeProcessor.detect(KEEP_ONLY_MATCHING_LINES_TYPE_KEY, KEEP_ONLY_MATCHING_LINES_TYPE_VALUE, null));
    }

    @Test
    void testParse() {

        assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, null, null));
        assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, FOO, null));
        assertDoesNotThrow(() -> typeProcessor.parse(KEEP_ONLY_MATCHING_LINES_TYPE_KEY, KEEP_ONLY_MATCHING_LINES_TYPE_VALUE, connector));
    }

    @Test
    void testGetKeepOnlyMatchingLines() {

        // No Source found
        Matcher matcher = typeProcessor.getMatcher(KEEP_ONLY_MATCHING_LINES_TYPE_KEY);
        assertTrue(matcher.matches());
        assertNull(typeProcessor.getKeepOnlyMatchingLines(matcher, connector));

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
        assertNull(typeProcessor.getKeepOnlyMatchingLines(matcher, connector));
    }
}