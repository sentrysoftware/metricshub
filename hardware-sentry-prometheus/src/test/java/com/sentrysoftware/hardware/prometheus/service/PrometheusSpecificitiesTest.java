package com.sentrysoftware.hardware.prometheus.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PrometheusSpecificitiesTest {

	@Test
	void testGetPrometheusParameterName() {
		assertNotNull(PrometheusSpecificities.getPrometheusParameterName("fan", "speedpercent"));
		assertEquals("speed", PrometheusSpecificities.getPrometheusParameterName("fan", "speedpercent"));
		assertNull(PrometheusSpecificities.getPrometheusParameterName(null, "speedpercent"));
		assertNull(PrometheusSpecificities.getPrometheusParameterName("fan", null));
		assertNull(PrometheusSpecificities.getPrometheusParameterName("blabla", "speedpercent"));
		assertNull(PrometheusSpecificities.getPrometheusParameterName("fan", "blabla"));
	}
	
	@Test
	void testGetPrometheusParameterUnit() {
		assertNotNull(PrometheusSpecificities.getPrometheusParameterUnit("fan", "speedpercent"));
		assertEquals("fraction", PrometheusSpecificities.getPrometheusParameterUnit("fan", "speedpercent"));
		assertNull(PrometheusSpecificities.getPrometheusParameterUnit(null, "speedpercent"));
		assertNull(PrometheusSpecificities.getPrometheusParameterUnit("fan", null));
		assertNull(PrometheusSpecificities.getPrometheusParameterUnit("blabla", "speedpercent"));
		assertNull(PrometheusSpecificities.getPrometheusParameterUnit("fan", "blabla"));
	}
	
	@Test
	void testGetPrometheusParameterFactor() {
		assertEquals(0.01, PrometheusSpecificities.getPrometheusParameterFactor("fan", "speedpercent"));
		assertEquals(1.0, PrometheusSpecificities.getPrometheusParameterFactor(null, "speedpercent"));
		assertEquals(1.0, PrometheusSpecificities.getPrometheusParameterFactor("fan", null));
		assertEquals(1.0, PrometheusSpecificities.getPrometheusParameterFactor("blabla", "speedpercent"));
		assertEquals(1.0, PrometheusSpecificities.getPrometheusParameterFactor("fan", "blabla"));
	}

}
