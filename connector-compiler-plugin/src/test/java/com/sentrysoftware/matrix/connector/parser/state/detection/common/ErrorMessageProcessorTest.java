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

class ErrorMessageProcessorTest {

	private final ErrorMessageProcessor errorMessageProcessor = new ErrorMessageProcessor(WBEM.class, "WBEM");

	private final Connector connector = new Connector();

	private static final String ERROR_MESSAGE_KEY = "detection.criteria(1).errormessage";
	private static final String FOO = "FOO";

	@Test
	void testGetTypeValue() {

		assertNull(new ErrorMessageProcessor(WBEM.class, null).getTypeValue());
	}

	@Test
	void testParse() {

		WBEM wbem = WBEM.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(wbem)).build();
		connector.setDetection(detection);
		assertNull(wbem.getErrorMessage());
		errorMessageProcessor.parse(ERROR_MESSAGE_KEY, FOO, connector);
		assertEquals(FOO, wbem.getErrorMessage());

		// setErrorMessage() not available
		SNMP snmp = SNMPGetNext.builder().index(1).build();
		detection.setCriteria(Collections.singletonList(snmp));
		connector.setDetection(detection);
		ErrorMessageProcessor absurdProcessor = new ErrorMessageProcessor(SNMP.class, "SNMP");
		assertThrows(IllegalStateException.class, () -> absurdProcessor.parse(ERROR_MESSAGE_KEY, FOO, connector));
	}
}