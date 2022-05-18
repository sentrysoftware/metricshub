package com.sentrysoftware.matrix.connector.parser.state.detection.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.Snmp;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SnmpGetNext;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpectedResultProcessorTest {

	private final ExpectedResultProcessor expectedResultProcessor = new ExpectedResultProcessor(Snmp.class, "SNMP");

	private final Connector connector = new Connector();

	private static final String EXPECTED_RESULT_KEY = "detection.criteria(1).expectedresult";
	private static final String FOO = "FOO";

	@Test
	void testGetType() {

		assertEquals(Snmp.class, new ExpectedResultProcessor(Snmp.class, null).getType());
	}

	@Test
	void testGetTypeValue() {

		assertNull(new ExpectedResultProcessor(Snmp.class, null).getTypeValue());
	}

	@Test
	void testParse() {

		// Key matches, Criterion found, no expectedResult field
		Process process = Process.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(process)).build();
		connector.setDetection(detection);
		ExpectedResultProcessor processExpectedResultProcessor = new ExpectedResultProcessor(Process.class, "Process");
		assertThrows(IllegalStateException.class,
			() -> processExpectedResultProcessor.parse(EXPECTED_RESULT_KEY, FOO, connector));

		// Key matches, Criterion found, expectedResult field exists
		SnmpGetNext snmpGetNext = SnmpGetNext.builder().index(1).build();
		detection = Detection.builder().criteria(Collections.singletonList(snmpGetNext)).build();
		connector.setDetection(detection);
		assertNull(snmpGetNext.getExpectedResult());
		expectedResultProcessor.parse(EXPECTED_RESULT_KEY, FOO, connector);
		assertEquals(FOO, snmpGetNext.getExpectedResult());
	}
}