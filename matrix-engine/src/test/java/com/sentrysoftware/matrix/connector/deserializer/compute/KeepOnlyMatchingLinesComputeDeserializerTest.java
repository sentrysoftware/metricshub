package com.sentrysoftware.matrix.connector.deserializer.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.KeepOnlyMatchingLines;

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
            KeepOnlyMatchingLines.builder()
					.type("keepOnlyMatchingLines")
					.column(1)
					.regExp("regExpTest")
					.valueList(Set.of("1", "2"))
					.build()
        );

		final Map<String, Source> expected = new LinkedHashMap<>(
			Map.of(
				"testCompute",
				HttpSource
					.builder()
					.key("$pre.testCompute$")
					.type("http")
					.url("/testUrl/")
                    .computes(computes)
					.build()
			)
		);

		assertEquals(expected, connector.getPre());
	}
}
