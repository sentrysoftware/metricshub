package com.sentrysoftware.matrix.connector.deserializer.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.ExcludeMatchingLines;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExcludeMatchingLinesComputeDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/compute/excludeMatchingLines/";
	}

	@Test
	void testDeserializeCompute() throws IOException {
		final Connector connector = getConnector("excludeMatchingLines");

		final List<Compute> computes = new ArrayList<>();
		computes.add(
			ExcludeMatchingLines.builder().type("excludeMatchingLines").column(1).valueList("test,exclude,values").build()
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
