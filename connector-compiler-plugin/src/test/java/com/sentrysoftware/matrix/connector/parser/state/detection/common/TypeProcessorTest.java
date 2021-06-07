package com.sentrysoftware.matrix.connector.parser.state.detection.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeProcessorTest {

	private final TypeProcessor typeProcessor = new TypeProcessor(WBEM.class, "WBEM");

	private final Connector connector = new Connector();
	private static final String TYPE_KEY = "detection.criteria(1).type";
	private static final String TYPE_VALUE = "WBEM";
	private static final String FOO = "FOO";

	@Test
	void testGetType() {

		assertEquals(WBEM.class, new TypeProcessor(WBEM.class, null).getType());
	}

	@Test
	void testParse() {

		// Value is invalid
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, FOO, connector));

		// Value is valid, key does not match
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, TYPE_VALUE, connector));

		// Value is valid, key matches, detection is initially null
		typeProcessor.parse(TYPE_KEY, TYPE_VALUE, connector);
		Detection detection = connector.getDetection();
		assertNotNull(detection);
		List<Criterion> criteria = detection.getCriteria();
		assertNotNull(criteria);
		assertEquals(1, criteria.size());
		Criterion criterion = criteria.get(0);
		assertTrue(criterion instanceof WBEM);
		assertEquals(1, criterion.getIndex());

		// Value is valid, key matches, detection is not initially null
		assertNotNull(connector.getDetection());
		typeProcessor.parse(TYPE_KEY, TYPE_VALUE, connector);
		detection = connector.getDetection();
		assertNotNull(detection);
		criteria = detection.getCriteria();
		assertNotNull(criteria);
		assertEquals(2, criteria.size());
		criterion = criteria.get(0);
		assertTrue(criterion instanceof WBEM);
		assertEquals(1, criterion.getIndex());
		criterion = criteria.get(1);
		assertTrue(criterion instanceof WBEM);
		assertEquals(1, criterion.getIndex());

		// Could not instantiate Criterion
		String SNMP = "SNMP";
		TypeProcessor snmpTypeProcessor = new TypeProcessor(SNMP.class, SNMP);
		assertThrows(IllegalStateException.class, () -> snmpTypeProcessor.parse(TYPE_KEY, SNMP, connector));
	}
}