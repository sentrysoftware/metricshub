package com.sentrysoftware.hardware.prometheus.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PrometheusSpecificitiesTest {

	private static final String SPEEDPERCENT = "speedpercent";

	@Test
	void testGetPrometheusParameterName() {
		assertNotNull(PrometheusSpecificities.getPrometheusParameter("fan", SPEEDPERCENT).getPrometheusParameterName());
		assertNotNull(PrometheusSpecificities.getPrometheusParameter("fAn", "speedPercent").getPrometheusParameterName());
		assertEquals("speed_fraction", PrometheusSpecificities.getPrometheusParameter("fan", SPEEDPERCENT).getPrometheusParameterName());
		assertNull(PrometheusSpecificities.getPrometheusParameter("blabla", SPEEDPERCENT));
		assertNull(PrometheusSpecificities.getPrometheusParameter("fan", "blabla"));
	}
	
	@Test
	void testGetPrometheusParameterUnit() {
		assertNotNull(PrometheusSpecificities.getPrometheusParameter("fan", SPEEDPERCENT).getPrometheusParameterUnit());
		assertEquals("fraction", PrometheusSpecificities.getPrometheusParameter("fan", SPEEDPERCENT).getPrometheusParameterUnit());
	}
	
	@Test
	void testGetPrometheusParameterFactor() {
		assertEquals(0.01, PrometheusSpecificities.getPrometheusParameter("fan", SPEEDPERCENT).getPrometheusParameterFactor());
	}

}
