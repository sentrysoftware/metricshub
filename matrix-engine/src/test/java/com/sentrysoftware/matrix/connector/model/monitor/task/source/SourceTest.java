package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.common.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;

class SourceTest {

	private static final String SOURCE_REF1 = "$monitors.cpu.discovery.sources.source1$";
	private static final String SOURCE_REF2 = "$monitors.cpu.discovery.sources.source2$";

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesCopy() {
		final Source copy = CopySource.builder().from(SOURCE_REF1).build();
		assertArrayEquals(new String[] { SOURCE_REF1 }, copy.getPossibleReferences());
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesHttp() {
		final Source http = HttpSource
			.builder()
			.authenticationToken("authToken")
			.url("url/$entry.column(1)$")
			.body("body")
			.header("header")
			.executeForEachEntryOf(
				ExecuteForEachEntryOf
					.builder()
					.source(SOURCE_REF1)
					.concatMethod(EntryConcatMethod.LIST)
					.build()
			)
			.build();

		assertArrayEquals(
			new String[] {
				"url/$entry.column(1)$",
				"header",
				"body",
				"authToken",
				SOURCE_REF1 
			},
			http.getPossibleReferences()
		);
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesIpmi() {
		final Source ipmi = IpmiSource.builder().build();
		assertArrayEquals(new String[] {}, ipmi.getPossibleReferences());
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesOsCommand() {
		final Source osCommand = OsCommandSource
			.builder()
			.commandLine("cmd " + SOURCE_REF1)
			.exclude("exclude")
			.keep("keep")
			.selectColumns("1,2,3")
			.separators("\t")
			.build();

		assertArrayEquals(
			new String[] {
				"cmd " + SOURCE_REF1,
				"exclude",
				"keep",
				"\t",
				"1,2,3" 
			},
			osCommand.getPossibleReferences()
		);
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesSnmpGet() {
		final Source snmpGet = SnmpGetSource
			.builder()
			.oid(SOURCE_REF1)
			.build();
		assertArrayEquals(new String[] {SOURCE_REF1}, snmpGet.getPossibleReferences());
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesSnmpTable() {
		final Source snmpTable = SnmpTableSource
			.builder()
			.oid(SOURCE_REF1)
			.selectColumns("ID,1,2")
			.build();
		assertArrayEquals(new String[] {SOURCE_REF1, "ID,1,2"}, snmpTable.getPossibleReferences());
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesSshInteractive() {

		final Source sshInteractive = SshInteractiveSource
			.builder()
			.exclude("exclude")
			.keep("keep")
			.selectColumns("1,2,3")
			.separators("\t")
			.build();

		assertArrayEquals(
			new String[] {
				"exclude",
				"keep",
				"\t",
				"1,2,3"
			},
			sshInteractive.getPossibleReferences()
		);
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesStatic() {
		final Source staticSource = StaticSource.builder().value(SOURCE_REF1).build();
		assertArrayEquals(new String[] { SOURCE_REF1 }, staticSource.getPossibleReferences());
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesTableJoin() {
		final Source tableJoin = TableJoinSource
			.builder()
			.leftTable(SOURCE_REF1)
			.rightTable(SOURCE_REF2)
			.defaultRightLine(";;;;;;")
			.keyType("WBEM")
			.build();

		assertArrayEquals(
			new String[] {
				SOURCE_REF1,
				SOURCE_REF2,
				";;;;;;",
				"WBEM"
			},
			tableJoin.getPossibleReferences()
		);
	}
	
	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesTableUnion() {
		final Source tableUnion = TableUnionSource
			.builder()
			.tables(List.of(SOURCE_REF1, SOURCE_REF2))
			.build();

		assertArrayEquals(
			new String[] {
				SOURCE_REF1,
				SOURCE_REF2,
			},
			tableUnion.getPossibleReferences()
		);
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesUcs() {
		final Source ucs = UcsSource
			.builder()
			.queries(new LinkedHashSet<>(List.of(SOURCE_REF1, SOURCE_REF2)))
			.exclude("exclude")
			.keep("keep")
			.selectColumns("1,2")
			.build();

		assertArrayEquals(
			new String[] {
				SOURCE_REF1,
				SOURCE_REF2,
				"exclude",
				"keep",
				"1,2"
			},
			ucs.getPossibleReferences()
		);
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesWbem() {
		final Source wbem = WbemSource
			.builder()
			.query(SOURCE_REF1)
			.namespace(SOURCE_REF2)
			.build();

		assertArrayEquals(
			new String[] {
				SOURCE_REF1,
				SOURCE_REF2
			},
			wbem.getPossibleReferences()
		);
	}

	@Test
	@Disabled("Until getPossibleReferences is up!")
	void testGetPossibleReferencesWmi() {
		final Source wmi = WmiSource
			.builder()
			.query(SOURCE_REF1)
			.namespace(SOURCE_REF2)
			.build();

		assertArrayEquals(
			new String[] {
				SOURCE_REF1,
				SOURCE_REF2
			},
			wmi.getPossibleReferences()
		);
	}
}
