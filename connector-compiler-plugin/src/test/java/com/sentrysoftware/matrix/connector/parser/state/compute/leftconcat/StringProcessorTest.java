package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringProcessorTest {

    private final StringProcessor stringProcessor = new StringProcessor();

    private final Connector connector = new Connector();
    private static final String LEFT_CONCAT_STRING_KEY = "enclosure.discovery.source(1).compute(1).string";
    private static final String FOO = "FOO";

    @Test
    void testParse() {

        // Key does not match
        assertThrows(IllegalArgumentException.class, () -> stringProcessor.parse(FOO, FOO, connector));

        // Key matches, no LeftConcat found
        assertThrows(
                IllegalArgumentException.class,
                () -> stringProcessor.parse(LEFT_CONCAT_STRING_KEY, FOO, connector)
        );

        // Key matches, LeftConcat found
        LeftConcat leftConcat = new LeftConcat();
        leftConcat.setIndex(1);

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
                                                                        .computes(
                                                                                Collections.singletonList(
                                                                                        leftConcat
                                                                                )
                                                                        )
                                                                        .build()
                                                        )
                                                )
                                                .build()
                                )
                                .build()
                );

        stringProcessor.parse(LEFT_CONCAT_STRING_KEY, FOO, connector);
        assertEquals(FOO, leftConcat.getString());
    }
}