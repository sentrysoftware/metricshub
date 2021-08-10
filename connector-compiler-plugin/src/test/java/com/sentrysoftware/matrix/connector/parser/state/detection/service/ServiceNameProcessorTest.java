package com.sentrysoftware.matrix.connector.parser.state.detection.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;

class ServiceNameProcessorTest {

	private static final ServiceNameProcessor SERVICE_NAME_PROCESSOR = new ServiceNameProcessor();
	private static final String SERVICE_NAME = "Detection.Criteria(2).ServiceName";
	private static final String VALUE = "TWGIPC";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(Service.class, SERVICE_NAME_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals( "Service", SERVICE_NAME_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {

		assertThrows(IllegalArgumentException.class, () -> SERVICE_NAME_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = SERVICE_NAME_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = SERVICE_NAME_PROCESSOR.getMatcher("detection.criteria(1).exclude");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = SERVICE_NAME_PROCESSOR.getMatcher(SERVICE_NAME);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {
		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> SERVICE_NAME_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> SERVICE_NAME_PROCESSOR.parse(SERVICE_NAME, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> SERVICE_NAME_PROCESSOR.parse(SERVICE_NAME, VALUE, null));

		// check criterion not found
		assertThrows(IllegalArgumentException.class, () -> SERVICE_NAME_PROCESSOR.parse(SERVICE_NAME, VALUE, CONNECTOR));

		final Service service = Service.builder().index(2).build();
		final Detection detection = Detection.builder().criteria(Collections.singletonList(service)).build();
		CONNECTOR.setDetection(detection);
		SERVICE_NAME_PROCESSOR.parse(SERVICE_NAME, VALUE, CONNECTOR);
	}

}
