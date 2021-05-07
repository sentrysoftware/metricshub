package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ConnectorKeepOnlyMatchingLinesPropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(
				Stream.of(TypeProcessor.class, ColumnProcessor.class, ValueListProcessor.class, RegexpProcessor.class).collect(Collectors.toSet()),
				ConnectorKeepOnlyMatchingLinesProperty.getConnectorProperties().stream().map(obj -> obj.getClass()).collect(Collectors.toSet()));
	}
}