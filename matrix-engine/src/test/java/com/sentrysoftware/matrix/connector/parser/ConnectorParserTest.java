package com.sentrysoftware.matrix.connector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.AllAtOnceMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WbemSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WmiSource;

class ConnectorParserTest {

	@Test
	void testExtendsManagementArrayObjectsDepthExtends() throws IOException {
		new ConnectorParserExtendsManagement("arrayObjectsDepthExtends").test();
	}

	@Test
	void testExtendsManagementArrayObjectsMerge() throws IOException {
		new ConnectorParserExtendsManagement("arrayObjectsMerge").test();
	}

	@Test
	void testExtendsManagementArrayObjectsMergeOneExtends() throws IOException {
		new ConnectorParserExtendsManagement("arrayObjectsMergeOneExtends").test();
	}

	@Test
	void testExtendsManagementMergeObjects() throws IOException {
		new ConnectorParserExtendsManagement("mergeObjects").test();
	}

	@Test
	void testExtendsManagementMergeObjectsOneExtends() throws IOException {
		new ConnectorParserExtendsManagement("mergeObjectsOneExtends").test();
	}

	@Test
	void testExtendsManagementOverwriteArraysSimpleValues() throws IOException {
		new ConnectorParserExtendsManagement("overwriteArraysSimpleValues").test();
	}

	@Test
	void testExtendsManagementOverwriteArraysSimpleValuesOneExtends() throws IOException {
		new ConnectorParserExtendsManagement("overwriteArraysSimpleValuesOneExtends").test();
	}

	@Test
	void testConstantsManagement() throws IOException {
		new ConnectorParserConstantsManagement("management").test();
	}

	@Test
	void testCompiledFilenameUpdate() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector").parse("connector");
		assertEquals("connector", connector.getConnectorIdentity().getCompiledFilename());
	}

	@Test
	@Disabled("Until AvailableSourceUpdate is up!")
	void testAvailableSourceUpdate() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/availableUpdate").parse("availableSources");
		assertEquals(
			Set.of(
				WmiSource.class,
				WbemSource.class,
				TableJoinSource.class,
				TableUnionSource.class
			),
			connector.getSourceTypes()
		);
	}

	@Test
	@Disabled("Until MonitorTaskSourceDepUpdate is up!")
	void testMonitorTaskSourceDepUpdateUseCase1() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/monitorTaskSourceDep/useCase1").parse("sourceDep");

		final StandardMonitorJob monitorJob = (StandardMonitorJob) connector
			.getMonitors()
			.get("enclosure");

		final List<Set<String>> expected = buildUseCase1Dependency();

		assertEquals(expected,  monitorJob.getDiscovery().getSourceDep());

		assertEquals(List.of(Set.of("source(1)")), monitorJob.getCollect().getSourceDep());
	}

	@Test
	@Disabled("Until MonitorTaskSourceDepUpdate is up!")
	void testMonitorTaskSourceDepUpdateUseCase2() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/monitorTaskSourceDep/useCase2").parse("sourceDep");

		final StandardMonitorJob monitorJob = (StandardMonitorJob) connector
			.getMonitors()
			.get("enclosure");

		final List<Set<String>> expected = buildUseCase2Dependency();

		assertEquals(expected,  monitorJob.getDiscovery().getSourceDep());

	}

	@Test
	@Disabled("Until MonitorTaskSourceDepUpdate is up!")
	void testMonitorTaskSourceDepUpdateUseCase3() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/monitorTaskSourceDep/useCase3").parse("sourceDep");

		final StandardMonitorJob monitorJob = (StandardMonitorJob) connector
			.getMonitors()
			.get("enclosure");

		final List<Set<String>> expected = buildUseCase3Dependency();

		assertEquals(expected,  monitorJob.getDiscovery().getSourceDep());
	}

	@Test
	@Disabled("Until MonitorTaskSourceDepUpdate is up!")
	void testMonitorTaskSourceDepUpdateUseCase4() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/monitorTaskSourceDep/useCase4").parse("sourceDep");

		final StandardMonitorJob monitorJob = (StandardMonitorJob) connector
			.getMonitors()
			.get("enclosure");

		final List<Set<String>> expected = buildUseCase4Dependency();

		assertEquals(expected,  monitorJob.getDiscovery().getSourceDep());
	}

	@Test
	@Disabled("Until MonitorTaskSourceDepUpdate is up!")
	void testMonitorTaskSourceDepUpdateUseCase5() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/monitorTaskSourceDep/useCase5").parse("sourceDep");

		final StandardMonitorJob monitorJob = (StandardMonitorJob) connector
			.getMonitors()
			.get("enclosure");

		final List<Set<String>> expected = buildUseCase5MultiCollectDependency();

		assertEquals(expected,  monitorJob.getCollect().getSourceDep());
	}

	@Test
	@Disabled("Until MonitorTaskSourceDepUpdate is up!")
	void testMonitorTaskSourceDepUpdateUseCase6() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/monitorTaskSourceDep/useCase6").parse("sourceDep");

		final AllAtOnceMonitorJob monitorJob = (AllAtOnceMonitorJob) connector
			.getMonitors()
			.get("enclosure");

		final List<Set<String>> expected = buildUseCase6Dependency();

		assertEquals(expected,  monitorJob.getAllAtOnce().getSourceDep());

	}

	@Test
	@Disabled("Until MonitorTaskSourceDepUpdate is up!")
	void testMonitorTaskSourceDepUpdateUseCase7() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/monitorTaskSourceDep/useCase7").parse("sourceDep");

		final StandardMonitorJob monitorJob = (StandardMonitorJob) connector
			.getMonitors()
			.get("enclosure");

		final List<Set<String>> expected = buildUseCase7MultiCollectDependency();

		assertEquals(expected,  monitorJob.getCollect().getSourceDep());
	}

	@Test
	@Disabled("Until MonitorTaskSourceDepUpdate is up!")
	void testMonitorTaskSourceDepUpdateUseCase8() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/monitorTaskSourceDep/useCase8").parse("sourceDep");

		final StandardMonitorJob monitorJob = (StandardMonitorJob) connector
			.getMonitors()
			.get("enclosure");

		final List<Set<String>> expected = buildUseCase8MultiCollectDependency();

		assertEquals(expected,  monitorJob.getCollect().getSourceDep());
	}

	@Test
	@Disabled("Until MonitorTaskSourceDepUpdate is up!")
	void testMonitorTaskSourceDepUpdateUseCase9() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/monitorTaskSourceDep/useCase9").parse("sourceDep");

		final StandardMonitorJob monitorJob = (StandardMonitorJob) connector
			.getMonitors()
			.get("enclosure");

		final List<Set<String>> expected = buildUseCase9MultiCollectDependency();

		assertEquals(expected,  monitorJob.getCollect().getSourceDep());
	}

	@Test
	@Disabled("Until MonitorTaskSourceDepUpdate is up!")
	void testMonitorTaskSourceDepUpdateUseCase10() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/monitorTaskSourceDep/useCase10").parse("sourceDep");

		final StandardMonitorJob monitorJob = (StandardMonitorJob) connector
			.getMonitors()
			.get("enclosure");

		final List<Set<String>> expected = buildUseCase10MultiCollectDependency();

		assertEquals(expected,  monitorJob.getCollect().getSourceDep());
	}

	@Test
	@Disabled("Until PreSourceDepUpdate is up!")
	void testPreSourceDepUpdateUseCase1() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/preSourceDep/useCase1").parse("sourceDep");

		final List<Set<String>> expected = buildUseCase1Dependency();

		assertEquals(expected, connector.getPreSourceDep());

	}

	@Test
	@Disabled("Until PreSourceDepUpdate is up!")
	void testPreSourceDepUpdateUseCase2() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/preSourceDep/useCase2").parse("sourceDep");

		final List<Set<String>> expected = buildUseCase2Dependency();

		assertEquals(expected, connector.getPreSourceDep());

	}

	@Test
	@Disabled("Until PreSourceDepUpdate is up!")
	void testPreSourceDepUpdateUseCase3() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/preSourceDep/useCase3").parse("sourceDep");

		final List<Set<String>> expected = buildUseCase3Dependency();
		assertEquals(expected, connector.getPreSourceDep());
	}

	@Test
	@Disabled("Until PreSourceDepUpdate is up!")
	void testPreSourceDepUpdateUseCase4() throws IOException {
		final Connector connector = new ConnectorParserUpdateManagement("connector/management/preSourceDep/useCase4").parse("sourceDep");

		final List<Set<String>> expected = new ArrayList<>();
		final Set<String> level1 = new HashSet<>();
		// WMI query
		level1.add("source(1)");

		final Set<String> level2 = new HashSet<>();
		// WMI query with executeForEachEntryOf source(1)
		level2.add("source(2)");

		expected.add(level1);
		expected.add(level2);

		assertEquals(expected, connector.getPreSourceDep());
	}

	private List<Set<String>> buildUseCase1Dependency() {
		final List<Set<String>> expected = new ArrayList<>();
		final Set<String> level1 = new HashSet<>();
		// WBEM queries
		level1.add("source(1)");
		level1.add("source(2)");
		level1.add("source(3)");
		level1.add("source(5)");

		final Set<String> level2 = new HashSet<>();
		// TableUnion of source(2) and source(3)
		level2.add("source(4)");

		final Set<String> level3 = new HashSet<>();
		// tableJoin of source(1) and source(4)
		level3.add("source(6)");

		final Set<String> level4 = new HashSet<>();
		// tableJoin of source(6) and source(7)
		level4.add("source(7)");

		expected.add(level1);
		expected.add(level2);
		expected.add(level3);
		expected.add(level4);

		return expected;
	}

	private List<Set<String>> buildUseCase2Dependency() {
		final List<Set<String>> expected = new ArrayList<>();
		final Set<String> level1 = new HashSet<>();
		// WBEM queries
		level1.add("source(1)");
		level1.add("source(2)");
		level1.add("source(3)");
		level1.add("source(6)");

		final Set<String> level2 = new HashSet<>();
		// Copy of  source(3)
		level2.add("source(4)");

		final Set<String> level3 = new HashSet<>();
		// tableUnion of source(2) and source(4)
		level3.add("source(5)");

		final Set<String> level4 = new HashSet<>();
		// tableJoin of source(1) and source(5)
		level4.add("source(7)");

		final Set<String> level5 = new HashSet<>();
		// tableJoin of source(6) and source(7) 
		level5.add("source(8)");

		expected.add(level1);
		expected.add(level2);
		expected.add(level3);
		expected.add(level4);
		expected.add(level5);

		return expected;
	}

	private List<Set<String>> buildUseCase3Dependency() {
		final List<Set<String>> expected = new ArrayList<>();
		final Set<String> level1 = new HashSet<>();
		// WBEM queries
		level1.add("source(1)");
		level1.add("source(2)");
		level1.add("source(3)");
		level1.add("source(6)");
		level1.add("source(9)");

		final Set<String> level2 = new HashSet<>();
		// Copy of source(3)
		level2.add("source(4)");
		// TableUnion of source(2) and source(3)
		level2.add("source(5)");

		final Set<String> level3 = new HashSet<>();
		// TableJoin of source(1) and source(5)
		level3.add("source(7)");
		// TableJoin of source(1) and source(4)
		level3.add("source(10)");

		final Set<String> level4 = new HashSet<>();
		// TableJoin of source(7) and source(6)
		level4.add("source(8)");
		// TableJoin of source(10) and source(9)
		level4.add("source(11)");

		final Set<String> level5 =  new HashSet<>();
		// TableUnion of source(11) and source(8)
		level5.add("source(12)");

		expected.add(level1);
		expected.add(level2);
		expected.add(level3);
		expected.add(level4);
		expected.add(level5);

		return expected;
	}

	private List<Set<String>> buildUseCase4Dependency() {
		final List<Set<String>> expected = new ArrayList<>();
		final Set<String> level1 = new HashSet<>();
		// WMI query
		level1.add("source(1)");

		final Set<String> level2 = new HashSet<>();
		// WMI query with executeForEachEntryOf source(1)
		level2.add("source(2)");

		expected.add(level1);
		expected.add(level2);

		return expected;
	}

	private List<Set<String>> buildUseCase5MultiCollectDependency() {
		final List<Set<String>> expected = new ArrayList<>();
		final Set<String> level1 = new HashSet<>();
		// WBEM query
		level1.add("source(1)");
		// Copy of discovery source(6)
		level1.add("source(2)");

		final Set<String> level2 = new HashSet<>();
		// TableJoin of source(1) and source(2)
		level2.add("source(3)");

		expected.add(level1);
		expected.add(level2);

		return expected;
	}

	private List<Set<String>> buildUseCase6Dependency() {
		return buildUseCase1Dependency();
	}

	private List<Set<String>> buildUseCase7MultiCollectDependency() {
		return buildUseCase5MultiCollectDependency();
	}

	private List<Set<String>> buildUseCase8MultiCollectDependency() {
		final List<Set<String>> expected = new ArrayList<>();
		final Set<String> level1 = new HashSet<>();
		// Copy from a host job source(6) 
		level1.add("myExternalSource");
		// Copy of discovery source(6)
		level1.add("source(2)");
		// WBEM query
		level1.add("source(1)");

		final Set<String> level2 = new HashSet<>();
		// TableJoin of source(1) and source(2)
		level2.add("source(3)");

		final Set<String> level3 = new HashSet<>();
		// TableUnion of myExternalSource and source(2) and source(3)
		level3.add("myUnionSource");

		expected.add(level1);
		expected.add(level2);
		expected.add(level3);

		return expected;
	}

	private List<Set<String>> buildUseCase9MultiCollectDependency() {
		final List<Set<String>> expected = new ArrayList<>();
		final Set<String> level1 = new HashSet<>();
		// TableUnion from foreign discovery sources
		level1.add("myUnionSource2");
		// Copy from a host job source(6) 
		level1.add("myExternalSource");
		// Copy of discovery source(6)
		level1.add("source(2)");
		// WBEM query
		level1.add("source(1)");

		final Set<String> level2 = new HashSet<>();
		// TableJoin of source(1) and source(2)
		level2.add("source(3)");

		final Set<String> level3 = new HashSet<>();
		// TableUnion of myExternalSource and source(2) and source(3)
		level3.add("myUnionSource1");

		expected.add(level1);
		expected.add(level2);
		expected.add(level3);

		return expected;
	}

	private List<Set<String>> buildUseCase10MultiCollectDependency() {
		final List<Set<String>> expected = new ArrayList<>();
		final Set<String> level1 = new HashSet<>();
		// TableJoin from foreign discovery sources
		level1.add("tableJoin");
		// Copy from a host job source(6) 
		level1.add("myExternalSource");
		// Copy of discovery source(6)
		level1.add("source(2)");
		// WBEM query
		level1.add("source(1)");

		final Set<String> level2 = new HashSet<>();
		// TableJoin of source(1) and source(2)
		level2.add("source(3)");

		final Set<String> level3 = new HashSet<>();
		// TableUnion of myExternalSource and source(2) and source(3)
		level3.add("myUnionSource1");

		expected.add(level1);
		expected.add(level2);
		expected.add(level3);

		return expected;
	}

}
