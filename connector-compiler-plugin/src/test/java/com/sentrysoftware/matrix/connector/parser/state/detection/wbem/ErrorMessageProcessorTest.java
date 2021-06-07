package com.sentrysoftware.matrix.connector.parser.state.detection.wbem;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ErrorMessageProcessorTest {

	private final ErrorMessageProcessor errorMessageProcessor = new ErrorMessageProcessor();

	private final Connector connector = new Connector();

	private static final String ERROR_MESSAGE_KEY = "detection.criteria(1).errormessage";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		WBEM wbem = WBEM.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(wbem)).build();
		connector.setDetection(detection);
		assertNull(wbem.getErrorMessage());
		errorMessageProcessor.parse(ERROR_MESSAGE_KEY, FOO, connector);
		assertEquals(FOO, wbem.getErrorMessage());
	}
}