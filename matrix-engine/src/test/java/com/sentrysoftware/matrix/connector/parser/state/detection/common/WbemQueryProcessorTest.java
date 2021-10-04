package com.sentrysoftware.matrix.connector.parser.state.detection.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class WbemQueryProcessorTest {

	private final WbemQueryProcessor wbemQueryProcessor = new WbemQueryProcessor(WBEM.class, "WBEM");

	private final Connector connector = new Connector();

	private static final String WBEM_QUERY_KEY = "detection.criteria(1).wbemquery";
	private static final String FOO = "FOO";

	@Test
	void testGetTypeValue() {

		assertNull(new WbemQueryProcessor(WBEM.class, null).getTypeValue());
	}

	@Test
	void testParse() {

		WBEM wbem = (WBEM) WBEM.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(wbem)).build();
		connector.setDetection(detection);
		assertNull(wbem.getWbemQuery());
		wbemQueryProcessor.parse(WBEM_QUERY_KEY, FOO, connector);
		assertEquals(FOO, wbem.getWbemQuery());

		// setWBemQuery() not available
		SNMP snmp = SNMPGetNext.builder().index(1).build();
		detection.setCriteria(Collections.singletonList(snmp));
		connector.setDetection(detection);
		WbemQueryProcessor absurdProcessor = new WbemQueryProcessor(SNMP.class, "SNMP");
		assertThrows(IllegalStateException.class, () -> absurdProcessor.parse(WBEM_QUERY_KEY, FOO, connector));
	}
}