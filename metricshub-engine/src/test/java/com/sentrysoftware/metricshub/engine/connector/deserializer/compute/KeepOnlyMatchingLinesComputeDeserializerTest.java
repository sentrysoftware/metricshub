package com.sentrysoftware.metricshub.engine.connector.deserializer.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.engine.connector.deserializer.DeserializerTest;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.KeepOnlyMatchingLines;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KeepOnlyMatchingLinesComputeDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/compute/keepOnlyMatchingLines/";
	}

	@Test
	void testDeserializeCompute() throws IOException {
		final Connector connector = getConnector("keepOnlyMatchingLines");

		final List<Compute> computes = new ArrayList<>();
		computes.add(
			KeepOnlyMatchingLines
				.builder()
				.type("keepOnlyMatchingLines")
				.column(1)
				.regExp("regExpTest")
				.valueList("1,2")
				.build()
		);

		final Map<String, Source> expected = new LinkedHashMap<>(
			Map.of(
				"testCompute",
				HttpSource.builder().key("${source::pre.testCompute}").type("http").url("/testUrl/").computes(computes).build()
			)
		);

		assertEquals(expected, connector.getPre());
	}
}