package com.sentrysoftware.hardware.prometheus.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;

@SpringBootTest
class ExporterInfoServiceTest {

	@Autowired
	private ExporterInfoService exporterInfoService;

	@Test
	void testGetExporterInfoMetric() {
		final MetricFamilySamples mfs = exporterInfoService.getExporterInfoMetric();
		assertNotNull(mfs);
		assertNotNull(mfs.help);
		assertNotNull(mfs.name);
		assertEquals(Collector.Type.GAUGE, mfs.type);
		assertEquals(ExporterInfoService.LABELS, mfs.samples.get(0).labelNames);
		mfs.samples.get(0).labelValues.forEach(Assertions::assertNotNull);
		assertEquals(1.0, mfs.samples.get(0).value);
	}

}
