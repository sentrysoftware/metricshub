package com.sentrysoftware.matrix.connector.parser.state.detection.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WbemNameSpaceProcessorTest {

	private final WbemNameSpaceProcessor wbemNameSpaceProcessor = new WbemNameSpaceProcessor(WBEM.class, "WBEM");

	private final Connector connector = new Connector();

	private static final String WBEM_NAMESPACE_KEY = "detection.criteria(1).wbemnamespace";
	private static final String FOO = "FOO";

	@Test
	void testGetTypeValue() {

		assertNull(new WbemNameSpaceProcessor(WBEM.class, null).getTypeValue());
	}

	@Test
	void testParse() {

		WBEM wbem = WBEM.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(wbem)).build();
		connector.setDetection(detection);
		assertNull(wbem.getWbemNamespace());
		wbemNameSpaceProcessor.parse(WBEM_NAMESPACE_KEY, FOO, connector);
		assertEquals(FOO, wbem.getWbemNamespace());

		// setWBemNameSpace() not available
		SNMP snmp = SNMPGetNext.builder().index(1).build();
		detection.setCriteria(Collections.singletonList(snmp));
		connector.setDetection(detection);
		WbemNameSpaceProcessor absurdProcessor = new WbemNameSpaceProcessor(SNMP.class, "SNMP");
		assertThrows(IllegalStateException.class, () -> absurdProcessor.parse(WBEM_NAMESPACE_KEY, FOO, connector));
	}
}