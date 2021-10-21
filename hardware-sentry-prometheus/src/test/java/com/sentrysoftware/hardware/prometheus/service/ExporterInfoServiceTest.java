package com.sentrysoftware.hardware.prometheus.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.Map;

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

	@Autowired
	private Map<String, String> exporterInfo;

	@Test
	void testBuildExporterInfoMetric() {
		final MetricFamilySamples mfs = exporterInfoService.buildExporterInfoMetric();
		assertNotNull(mfs);
		assertNotNull(mfs.help);
		assertNotNull(mfs.name);
		assertEquals(Collector.Type.GAUGE, mfs.type);
		assertEquals(exporterInfo.keySet(), new HashSet<>(mfs.samples.get(0).labelNames));
		mfs.samples.get(0).labelValues.forEach(Assertions::assertNotNull);
		assertEquals(1.0, mfs.samples.get(0).value);
	}

}
