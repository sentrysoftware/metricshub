package org.sentrysoftware.metricshub.engine.connector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

class ReferenceResolverProcessorTest {

	@Test
	void testProcessNode() {
		final String json =
			"""
			pre:
			  source1:
			    type: http
			    leftTable: ${source::pre.source4} # ${source::pre.source4}
			  source2:
			    type: tableJoin
			    rightTable: ${source::source1} # ${source::pre.source1}
			monitors:
			  enclosure: # <object>
			    discovery: # <object> | <job> key possible values [ discovery, collect, simple]
			      # Sources
			      sources: # <source-object>
			        # Http Source
			        httpSource1: # <source-object>
			          type: http
			        httpSource2: # <source-object>
			          type1: ${source::httpSource1} # ${source::monitors.enclosure.discovery.sources.httpSource1}
			      mapping:
			        source1: ${source::httpSource2} # ${source::monitors.enclosure.discovery.sources.httpSource2}
			    collect: # <object> | <job> key possible values [ discovery, collect, simple]
			      # Sources
			      sources: # <source-object>
			        # Http Source
			        httpSource1: # <source-object>
			          type: http
			        httpSource2: # <source-object>
			          type1: ${source::httpSource1} # ${source::monitors.enclosure.discovery.sources.httpSource1}
			      mapping:
			        source1: ${source::httpSource2} # ${source::monitors.enclosure.discovery.sources.httpSource2}
			  disk: # <object>
			    discovery: # <object> | <job> key possible values [ discovery, collect, simple]
			      # Sources
			      sources: # <source-object>
			        # Http Source
			        httpSource1: # <source-object>
			          type2: http
			        httpSource2: # <source-object>
			          type1: ${source::monitors.disk.discovery.sources.httpSource1} # ${source::monitors.disk.discovery.sources.httpSource1}
			      mapping:
			        source1: ${source::monitors.disk.discovery.sources.httpSource2} # ${source::monitors.disk.discovery.sources.httpSource2}
			""";

		final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		try {
			final JsonNode rootNode = objectMapper.readTree(json);

			JsonNode processedNode = ReferenceResolverProcessor
				.builder()
				.next(new ConstantsProcessor())
				.build()
				.processNode(rootNode);

			// Check that the relative source references are correctly replaced under the "pre" section
			assertEquals("${source::pre.source4}", processedNode.get("pre").get("source1").get("leftTable").asText());
			assertEquals("${source::pre.source1}", processedNode.get("pre").get("source2").get("rightTable").asText());

			// Check that the relative source references are correctly replaced under the "enclosure" monitor section
			assertEquals(
				"${source::monitors.enclosure.discovery.sources.httpSource1}",
				processedNode
					.get("monitors")
					.get("enclosure")
					.get("discovery")
					.get("sources")
					.get("httpSource2")
					.get("type1")
					.asText()
			);
			assertEquals(
				"${source::monitors.enclosure.discovery.sources.httpSource2}",
				processedNode.get("monitors").get("enclosure").get("discovery").get("mapping").get("source1").asText()
			);
			assertEquals(
				"${source::monitors.enclosure.collect.sources.httpSource1}",
				processedNode
					.get("monitors")
					.get("enclosure")
					.get("collect")
					.get("sources")
					.get("httpSource2")
					.get("type1")
					.asText()
			);
			assertEquals(
				"${source::monitors.enclosure.collect.sources.httpSource2}",
				processedNode.get("monitors").get("enclosure").get("collect").get("mapping").get("source1").asText()
			);

			// Check that the relative source references are correctly replaced under the "disk" monitor section
			assertEquals(
				"${source::monitors.disk.discovery.sources.httpSource1}",
				processedNode
					.get("monitors")
					.get("disk")
					.get("discovery")
					.get("sources")
					.get("httpSource2")
					.get("type1")
					.asText()
			);
			assertEquals(
				"${source::monitors.disk.discovery.sources.httpSource2}",
				processedNode.get("monitors").get("disk").get("discovery").get("mapping").get("source1").asText()
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
