package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.connector.deserializer.PostDeserializeHelper;

class SourceTest {

	private static final String SOURCE_REF0 = "$pre.source1$";
	private static final String SOURCE_REF1 = "$monitors.cpu.discovery.sources.source1$";
	private static final String SOURCE_REF2 = "$monitors.cpu.discovery.sources.source2$";

	private static final  ObjectMapper MAPPER = PostDeserializeHelper
		.addPostDeserializeSupport(JsonHelper.buildYamlMapper());

	@Test
	void testReferencesCopy() throws IOException {
		final String copyYaml = """
			type: copy
			from: $monitors.cpu.discovery.sources.source1$
			forceSerialization: false
			""";
		final Source source = MAPPER.readValue(copyYaml, Source.class);
		assertEquals(Set.of(SOURCE_REF1), source.getReferences());
	}

	@Test
	void testReferencesHttp() throws IOException {
		final String httpYaml = """
			type: http
			url: url/$entry.column(1)$
			header: $pre.source1$
			body: body1
			authenticationToken: authToken
			resultContent: body
			forceSerialization: false
			executeForEachEntryOf:
			  source: $monitors.cpu.discovery.sources.source1$
			  concatMethod: list
			""";

		final Source source = MAPPER.readValue(httpYaml, Source.class);
		assertEquals(
			Set.of(SOURCE_REF0, SOURCE_REF1),
			source.getReferences()
		);
	}

	@Test
	void testReferencesIpmi() throws IOException {
		final String ipmiYaml = """
				type: ipmi
				forceSerialization: false
				""";

		final Source source = MAPPER.readValue(ipmiYaml, Source.class);
		assertEquals(
			Collections.emptySet(),
			source.getReferences()
		);
	}

	@Test
	void testReferencesOsCommand() throws IOException {
		final String osCommandYaml = """
				type: osCommand
				forceSerialization: false
				commandLine: $monitors.cpu.discovery.sources.source1$
				exclude: exclude
				keep: keep
				beginAtLineNumber: 1
				endAtLineNumber: 10
				separators: "\t"
				selectColumns: 1,2,3
				""";

		final Source source = MAPPER.readValue(osCommandYaml, Source.class);
		assertEquals(
			Set.of(SOURCE_REF1),
			source.getReferences()
		);

	}

	@Test
	void testReferencesSnmpGet() throws IOException {
		final String snmpGetYaml = """
				type: snmpGet
				forceSerialization: false
				oid: $monitors.cpu.discovery.sources.source1$
				""";

		final Source source = MAPPER.readValue(snmpGetYaml, Source.class);
		assertEquals(
			Set.of(SOURCE_REF1),
			source.getReferences()
		);
	}

	@Test
	void testReferencesSnmpTable() throws IOException {
		final String snmpTableYaml = """
				type: snmpTable
				forceSerialization: false
				oid: $monitors.cpu.discovery.sources.source1$
				selectColumns: ID,1,2,3
				""";

		final Source source = MAPPER.readValue(snmpTableYaml, Source.class);
		assertEquals(
			Set.of(SOURCE_REF1),
			source.getReferences()
		);
	}

	@Test
	void testReferencesStatic() throws IOException {
		final String staticSourceYaml = """
			type: static
			value: $monitors.cpu.discovery.sources.source1$
			""";
		final Source source = MAPPER.readValue(staticSourceYaml, Source.class);
		assertEquals(Set.of(SOURCE_REF1), source.getReferences());
	}

	@Test
	void testReferencesTableJoin() throws IOException {
		final String tableJoinYaml = """
				type: tableJoin
				forceSerialization: false
				leftTable: $monitors.cpu.discovery.sources.source1$
				rightTable: $monitors.cpu.discovery.sources.source2$
				defaultRightLine: ;;;;;;
				keyType: WBEM
				leftKeyColumn: 1
				rightKeyColumn: 2
				""";

		final Source source = MAPPER.readValue(tableJoinYaml, Source.class);
		assertEquals(
			Set.of(SOURCE_REF1, SOURCE_REF2),
			source.getReferences()
		);

	}

	@Test
	void testReferencesTableUnion() throws IOException {
		final String tableUnionYaml = """
				type: tableUnion
				forceSerialization: false
				tables: 
				- $monitors.cpu.discovery.sources.source1$
				- $monitors.cpu.discovery.sources.source2$
				""";

		final Source source = MAPPER.readValue(tableUnionYaml, Source.class);
		assertEquals(
			Set.of(SOURCE_REF1, SOURCE_REF2),
			source.getReferences()
		);
	}

	@Test
	void testReferencesWbem() throws IOException {
		final String wbemYaml = """
				type: wbem
				forceSerialization: false
				query: $monitors.cpu.discovery.sources.source1$
				namespace: $monitors.cpu.discovery.sources.source2$
				""";

		final Source source = MAPPER.readValue(wbemYaml, Source.class);
		assertEquals(
			Set.of(SOURCE_REF1, SOURCE_REF2),
			source.getReferences()
		);
	}

	@Test
	void testReferencesWmi() throws IOException {
		final String wmiYaml = """
				type: wmi
				forceSerialization: false
				query: $monitors.cpu.discovery.sources.source1$
				namespace: $monitors.cpu.discovery.sources.source2$
				""";

		final Source source = MAPPER.readValue(wmiYaml, Source.class);
		assertEquals(
			Set.of(SOURCE_REF1, SOURCE_REF2),
			source.getReferences()
		);
	}
}
