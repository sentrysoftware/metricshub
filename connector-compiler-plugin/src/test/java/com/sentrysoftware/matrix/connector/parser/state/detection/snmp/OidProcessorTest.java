package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OidProcessorTest {

	private final OidProcessor oidProcessor = new OidProcessor();

	private final Connector connector = new Connector();

	private static final String CRITERION_SNMPGETNEXT_OID_KEY = "detection.criteria(1).snmpgetnext";
	private static final String CRITERION_SNMPGET_OID_KEY = "detection.criteria(2).snmpget";
	private static final String VALUE = "1.3.6.1.4.1.674.10892.1.300.10.1";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> oidProcessor.parse(FOO, FOO, connector));

		// Key matches, type is SNMPGetNext, detection is initially null
		oidProcessor.parse(CRITERION_SNMPGETNEXT_OID_KEY, VALUE, connector);
		Detection detection = connector.getDetection();
		assertNotNull(detection);
		List<Criterion> criteria = detection.getCriteria();
		assertNotNull(criteria);
		assertEquals(1, criteria.size());
		Criterion criterion = criteria.get(0);
		assertTrue(criterion instanceof SNMPGetNext);
		assertEquals(1, criterion.getIndex());
		assertEquals(VALUE, ((SNMPGetNext) criterion).getOid());

		// Key matches, type is SNMPGet, detection is not initially null
		oidProcessor.parse(CRITERION_SNMPGET_OID_KEY, VALUE, connector);
		detection = connector.getDetection();
		assertNotNull(detection);
		criteria = detection.getCriteria();
		assertNotNull(criteria);
		assertEquals(2, criteria.size());
		criterion = criteria.get(0);
		assertTrue(criterion instanceof SNMPGetNext);
		assertEquals(1, criterion.getIndex());
		assertEquals(VALUE, ((SNMPGetNext) criterion).getOid());
		criterion = criteria.get(1);
		assertTrue(criterion instanceof SNMPGet);
		assertEquals(2, criterion.getIndex());
		assertEquals(VALUE, ((SNMPGet) criterion).getOid());
	}
}