package com.sentrysoftware.matrix.engine.strategy.source.compute;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComputeVisitorTest {

    ComputeVisitor computeVisitor;

    private static final String FOO = "FOO";
    private static final String BAR = "BAR";
    private static final String BAZ = "BAZ";

    @BeforeEach
    void setUp() {

        computeVisitor = new ComputeVisitor();
    }

    @Test
    void visitKeepOnlyMatchingLinesNoOperation() {

        // KeepOnlyMatchingLines is null
        computeVisitor.setSourceTable(SourceTable.empty());
        computeVisitor.visit((KeepOnlyMatchingLines) null);
        assertNotNull(computeVisitor.getSourceTable().getTable());
        assertTrue(computeVisitor.getSourceTable().getTable().isEmpty());

        // KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is null
        KeepOnlyMatchingLines keepOnlyMatchingLines = KeepOnlyMatchingLines.builder().build();
        computeVisitor.visit(keepOnlyMatchingLines);
        assertTrue(computeVisitor.getSourceTable().getTable().isEmpty());

        // KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is not null,
        // keepOnlyMatchingLines.getColumn() <= 0
        keepOnlyMatchingLines.setColumn(0);
        computeVisitor.visit(keepOnlyMatchingLines);
        assertTrue(computeVisitor.getSourceTable().getTable().isEmpty());

        // KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is not null,
        // keepOnlyMatchingLines.getColumn() > 0,
        // computeVisitor.getSourceTable() is null
        keepOnlyMatchingLines.setColumn(1);
        computeVisitor.setSourceTable(null);
        computeVisitor.visit(keepOnlyMatchingLines);
        assertNull(computeVisitor.getSourceTable());

        // KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is not null,
        // keepOnlyMatchingLines.getColumn() > 0,
        // computeVisitor.getSourceTable() is not null, computeVisitor.getSourceTable().getTable() is null
        computeVisitor.setSourceTable(SourceTable.builder().table(null).build());
        computeVisitor.visit(keepOnlyMatchingLines);
        assertNull(computeVisitor.getSourceTable().getTable());

        // KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is not null,
        // keepOnlyMatchingLines.getColumn() > 0,
        // computeVisitor.getSourceTable() is not null, computeVisitor.getSourceTable().getTable() is not null,
        // computeVisitor.getSourceTable().getTable().isEmpty()
        computeVisitor.setSourceTable(SourceTable.empty());
        computeVisitor.visit(keepOnlyMatchingLines);
        assertTrue(computeVisitor.getSourceTable().getTable().isEmpty());

        // KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is not null,
        // keepOnlyMatchingLines.getColumn() > 0,
        // computeVisitor.getSourceTable() is not null, computeVisitor.getSourceTable().getTable() is not null,
        // computeVisitor.getSourceTable().getTable() is not empty,
        // keepOnlyMatchingLines.getColumn() > sourceTable.getTable().get(0).size()
        computeVisitor.setSourceTable(
                SourceTable
                        .builder()
                        .table(
                                Collections.singletonList(
                                        Collections.singletonList(FOO)
                                )
                        )
                        .build());
        keepOnlyMatchingLines.setColumn(2);
        computeVisitor.visit(keepOnlyMatchingLines);
        assertEquals(1, computeVisitor.getSourceTable().getTable().size());
    }

    @Test
    void visitKeepOnlyMatchingLines() {

        List<String> line1 = Arrays.asList(FOO, "1", "2", "3");
        List<String> line2 = Arrays.asList(BAR, "10", "20", "30");
        List<String> line3 = Arrays.asList(BAZ, "100", "200", "300");
        List<List<String>> table = Arrays.asList(line1, line2, line3);

        computeVisitor.setSourceTable(
                SourceTable
                        .builder()
                        .table(table)
                        .build());

        // regexp is null, valueList is null
        KeepOnlyMatchingLines keepOnlyMatchingLines = KeepOnlyMatchingLines
                .builder()
                .column(1)
                .regExp(null)
                .valueList(null)
                .build();
        computeVisitor.visit(keepOnlyMatchingLines);
        assertEquals(table, computeVisitor.getSourceTable().getTable());

        // regexp is empty, valueList is null
        keepOnlyMatchingLines.setRegExp("");
        computeVisitor.visit(keepOnlyMatchingLines);
        assertEquals(table, computeVisitor.getSourceTable().getTable());

        // regexp is empty, valueList is empty
        keepOnlyMatchingLines.setValueList(Collections.emptyList());
        computeVisitor.visit(keepOnlyMatchingLines);
        assertEquals(table, computeVisitor.getSourceTable().getTable());

        // regex is not null, not empty
        keepOnlyMatchingLines.setRegExp("^B.*");
        computeVisitor.visit(keepOnlyMatchingLines);
        assertNotEquals(table, computeVisitor.getSourceTable().getTable());
        List<List<String>> resultTable = computeVisitor.getSourceTable().getTable();
        assertNotNull(resultTable);
        assertEquals(2, resultTable.size());
        assertEquals(line2, resultTable.get(0));
        assertEquals(line3, resultTable.get(1));

        // regex is null,
        // valueList is not null, not empty
        computeVisitor.getSourceTable().setTable(table);
        keepOnlyMatchingLines.setRegExp(null);
        keepOnlyMatchingLines.setValueList(Arrays.asList("3", "300"));
        keepOnlyMatchingLines.setColumn(4);
        computeVisitor.visit(keepOnlyMatchingLines);
        assertNotEquals(table, computeVisitor.getSourceTable().getTable());
        resultTable = computeVisitor.getSourceTable().getTable();
        assertNotNull(resultTable);
        assertEquals(2, resultTable.size());
        assertEquals(line1, resultTable.get(0));
        assertEquals(line3, resultTable.get(1));
    }
}