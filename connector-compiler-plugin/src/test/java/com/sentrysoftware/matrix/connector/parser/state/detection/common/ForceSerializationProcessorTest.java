package com.sentrysoftware.matrix.connector.parser.state.detection.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForceSerializationProcessorTest {

	private final ForceSerializationProcessor forceSerializationProcessor =
		new ForceSerializationProcessor(SNMP.class, "SNMP");

	private final Connector connector = new Connector();

	private static final String FORCE_SERIALIZATION_KEY = "detection.criteria(1).forceserialization";
	private static final String FOO = "FOO";
	private static final String ZERO = "0";
	private static final String ONE = "1";

	@Test
	void testGetType() {

		assertEquals(SNMP.class, new ForceSerializationProcessor(SNMP.class, null).getType());
	}

	@Test
	void testGetTypeValue() {

		assertNull(new ForceSerializationProcessor(SNMP.class, null).getTypeValue());
	}

	@Test
	void testDetect() {

		// value null
		assertFalse(forceSerializationProcessor.detect(null, null, null));

		// value not null, key null
		assertFalse(forceSerializationProcessor.detect(null, FOO, null));

		// value not null, key not null, key does not match
		assertFalse(forceSerializationProcessor.detect(FOO, FOO, null));

		// value not null, key not null, key matches, no detection found
		assertFalse(forceSerializationProcessor.detect(FORCE_SERIALIZATION_KEY, FOO, connector));

		// value not null, key not null, key matches, detection found, detection.getCriteria() is null
		Detection detection = new Detection();
		detection.setCriteria(null);
		connector.setDetection(detection);
		assertFalse(forceSerializationProcessor.detect(FORCE_SERIALIZATION_KEY, FOO, connector));

		// value not null, key not null, key matches, detection found, detection.getCriteria() is not null,
		// no same type criterion
		detection.setCriteria(Collections.singletonList(WBEM.builder().build()));
		assertFalse(forceSerializationProcessor.detect(FORCE_SERIALIZATION_KEY, FOO, connector));

		// value not null, key not null, key matches, detection found, detection.getCriteria() is not null,
		// same type criterion, different index
		detection.setCriteria(Collections.singletonList(SNMPGetNext.builder().index(2).build()));
		assertFalse(forceSerializationProcessor.detect(FORCE_SERIALIZATION_KEY, FOO, connector));

		// value not null, key not null, key matches, detection found, detection.getCriteria() is not null,
		// same type criterion, same index
		detection.setCriteria(Collections.singletonList(SNMPGetNext.builder().index(1).build()));
		assertTrue(forceSerializationProcessor.detect(FORCE_SERIALIZATION_KEY, FOO, connector));
	}

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> forceSerializationProcessor.parse(FOO, FOO, connector));

		// Key matches, no Criterion found
		connector.setDetection(null);
		assertThrows(
			IllegalArgumentException.class,
			() -> forceSerializationProcessor.parse(FORCE_SERIALIZATION_KEY, FOO, connector));

		// Key matches, Criterion found
		SNMPGetNext snmpGetNext = SNMPGetNext.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(snmpGetNext)).build();
		connector.setDetection(detection);
		assertFalse(snmpGetNext.isForceSerialization());
		forceSerializationProcessor.parse(FORCE_SERIALIZATION_KEY, ZERO, connector);
		assertFalse(snmpGetNext.isForceSerialization());
		forceSerializationProcessor.parse(FORCE_SERIALIZATION_KEY, ONE, connector);
		assertTrue(snmpGetNext.isForceSerialization());
	}
}