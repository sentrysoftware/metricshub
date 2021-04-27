package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ColumnProcessorTest {

    private final ColumnProcessor columnProcessor = new ColumnProcessor();

    private final Connector connector = new Connector();

    private static final String LEFT_CONCAT_COLUMN_KEY = "enclosure.discovery.source(1).compute(1).column";
    private static final String FOO = "FOO";
    private static final String NINE = "9";

    @Test
    void testParse() {

        // Key does not match
        assertThrows(IllegalArgumentException.class, () -> columnProcessor.parse(FOO, FOO, connector));

        // Key matches, no LeftConcat found
        assertThrows(
                IllegalArgumentException.class,
                () -> columnProcessor.parse(LEFT_CONCAT_COLUMN_KEY, FOO, connector)
        );

        // Key matches, LeftConcat found, invalid value
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

        assertThrows(
                IllegalArgumentException.class,
                () -> columnProcessor.parse(LEFT_CONCAT_COLUMN_KEY, FOO, connector)
        );

        // Key matches, LeftConcat found, value is valid
        columnProcessor.parse(LEFT_CONCAT_COLUMN_KEY, NINE, connector);
        assertEquals(9, leftConcat.getColumn());
    }
}