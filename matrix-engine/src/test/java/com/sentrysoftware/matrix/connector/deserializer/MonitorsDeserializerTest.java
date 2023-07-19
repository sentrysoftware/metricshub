package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.AllAtOnceMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.AllAtOnce;
import com.sentrysoftware.matrix.connector.model.monitor.task.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.task.Mapping;
import com.sentrysoftware.matrix.connector.model.monitor.task.MonoInstanceCollect;
import com.sentrysoftware.matrix.connector.model.monitor.task.MultiInstanceCollect;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WbemSource;

class MonitorsDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/monitors/";
	}

	@Test
	void testMonitorsDiscovery() throws IOException {

		final Connector connector = getConnector("monitorsDiscovery");

		Map<String, MonitorJob> monitors = connector.getMonitors();

		MonitorJob job = monitors.get("enclosure");

		assertTrue(job instanceof StandardMonitorJob, () -> "MonitorJob is expected to be a StandardMonitorJob");

		final StandardMonitorJob standard = (StandardMonitorJob) job;

		final Discovery discovery = standard.getDiscovery();

		assertNotNull(discovery);

		final Map<String, Source> expectedSources = new HashMap<>(
			Map.of(
				"source(1)",
				WbemSource
					.builder()
					.type("wbem")
					.query("SELECT __PATH,Model,EMCSerialNumber FROM EMC_ArrayChassis")
					.namespace("root/emc")
					.key("${source::monitors.enclosure.discovery.sources.source(1)}")
					.build(),
				"source(2)",
				WbemSource
					.builder()
					.type("wbem")
					.query("SELECT Antecedent,Dependent FROM EMC_ComputerSystemPackage")
					.namespace("root/emc")
					.key("${source::monitors.enclosure.discovery.sources.source(2)}")
					.build(),
				"source(3)",
				WbemSource
					.builder()
					.type("wbem")
					.query("SELECT Antecedent,Dependent FROM EMC_SystemPackaging")
					.namespace("root/emc")
					.key("${source::monitors.enclosure.discovery.sources.source(3)}")
					.build(),
				"source(4)",
				TableUnionSource
					.builder()
					.type("tableUnion")
					.tables(
						new ArrayList<>(
							List.of(
								"$monitors.enclosure.discovery.source(1)$",
								"$monitors.enclosure.discovery.source(2)$"
							)
						)
					)
					.key("${source::monitors.enclosure.discovery.sources.source(4)}")
					.build(),
				"source(5)",
				WbemSource
					.builder()
					.type("wbem")
					.query("SELECT __PATH,ElementName,Description,OtherIdentifyingInfo,OperationalStatus FROM EMC_StorageSystem")
					.namespace("root/emc")
					.key("${source::monitors.enclosure.discovery.sources.source(5)}")
					.build(),
				"source(6)",
				TableJoinSource
					.builder()
					.type("tableJoin")
					.leftTable("${source::monitors.enclosure.discovery.sources.source(1)}")
					.rightTable("${source::monitors.enclosure.discovery.sources.source(4)}")
					.leftKeyColumn(1)
					.rightKeyColumn(1)
					.keyType("WBEM")
					.defaultRightLine(";;")
					.key("${source::monitors.enclosure.discovery.sources.source(6)}")
					.build(),
				"source(7)",
				TableJoinSource
					.builder()
					.type("tableJoin")
					.leftTable("${source::monitors.enclosure.discovery.sources.source(6)}")
					.rightTable("${source::monitors.enclosure.discovery.sources.source(5)}")
					.leftKeyColumn(5)
					.rightKeyColumn(1)
					.keyType("WBEM")
					.defaultRightLine(";;;;")
					.key("${source::monitors.enclosure.discovery.sources.source(7)}")
					.build()
			)
		);

		assertEquals(expectedSources, discovery.getSources());

		final Mapping mapping = discovery.getMapping();

		final Mapping expectedMapping = Mapping
			.builder()
			.source("${source::monitors.enclosure.discovery.sources.source(7)}")
			.attributes(
				Map.of(
					"id", "buildId($column(6))",
					"parent", "",
					"name", "buildName(Storage, EMC, $column(2), (, $column(7), ))",
					"model", "$column(2)",
					"vendor", "EMC",
					"serial_number", "$column(3)",
					"type", "Storage"
				)
			)
			.conditionalCollection(Map.of("hw.status", "$column(10)"))
			.build();

		assertEquals(expectedMapping, mapping);
	}

	@Test
	void testMonitorsMultiInstanceCollect() throws IOException {

		final Connector connector = getConnector("monitorsMultiInstanceCollect");

		Map<String, MonitorJob> monitors = connector.getMonitors();

		MonitorJob job = monitors.get("enclosure");

		assertTrue(job instanceof StandardMonitorJob, () -> "MonitorJob is expected to be a StandardMonitorJob");

		final StandardMonitorJob standard = (StandardMonitorJob) job;

		final MultiInstanceCollect multiInstanceCollect = (MultiInstanceCollect) standard.getCollect();

		assertNotNull(multiInstanceCollect);

		final Map<String, Source> expectedSources = new HashMap<>(
			Map.of(
				"source(1)",
				WbemSource
					.builder()
					.type("wbem")
					.query("SELECT __PATH,OperationalStatus,StatusDescription FROM EMC_StorageSystem")
					.namespace("root/emc")
					.key("${source::monitors.enclosure.collect.sources.source(1)}")
					.build()
			)
		);

		assertEquals(expectedSources, multiInstanceCollect.getSources());

		final Mapping mapping = multiInstanceCollect.getMapping();

		final Mapping expectedMapping = Mapping
			.builder()
			.deviceId("$column(1)")
			.source("${source::monitors.enclosure.collect.sources.Source(1)}")
			.metrics(Map.of("hw.status", "$column(2)"))
			.legacyTextParameters(Map.of("StatusInformation", "$column(3)"))
			.build();

		assertEquals(expectedMapping, mapping);
	}

	@Test
	void testMonitorsMonoInstanceCollect() throws IOException {

		final Connector connector = getConnector("monitorsMonoInstanceCollect");

		Map<String, MonitorJob> monitors = connector.getMonitors();

		MonitorJob job = monitors.get("enclosure");

		assertTrue(job instanceof StandardMonitorJob, () -> "MonitorJob is expected to be a StandardMonitorJob");

		final StandardMonitorJob standard = (StandardMonitorJob) job;

		final MonoInstanceCollect monoInstanceCollect = (MonoInstanceCollect) standard.getCollect();

		assertNotNull(monoInstanceCollect);

		final Map<String, Source> expectedSources = new HashMap<>(
			Map.of(
				"source(1)",
				WbemSource
					.builder()
					.type("wbem")
					.query("SELECT $enclosure.deviceId$,OperationalStatus FROM EMC_StorageSystem")
					.namespace("root/emc")
					.key("${source::monitors.enclosure.collect.sources.source(1)}")
					.build()
			)
		);

		assertEquals(expectedSources, monoInstanceCollect.getSources());

		final Mapping mapping = monoInstanceCollect.getMapping();

		final Mapping expectedMapping = Mapping
			.builder()
			.source("${source::monitors.enclosure.collect.sources.Source(1)}")
			.metrics(Map.of("hw.status", "$column(2)"))
			.build();

		assertEquals(expectedMapping, mapping);
	}

	@Test
	void testMonitorsAllAtOnce() throws IOException {

		final Connector connector = getConnector("monitorsAllAtOnce");

		Map<String, MonitorJob> monitors = connector.getMonitors();

		MonitorJob job = monitors.get("enclosure");

		assertTrue(job instanceof AllAtOnceMonitorJob, () -> "MonitorJob is expected to be a AllAtOnceMonitorJob");

		final AllAtOnceMonitorJob allAtOnceMonitorJob = (AllAtOnceMonitorJob) job;

		AllAtOnce allAtOnce = allAtOnceMonitorJob.getAllAtOnce();

		assertNotNull(allAtOnce);

		final Map<String, Source> expectedSources = new HashMap<>(
			Map.of(
				"source(1)",
				WbemSource
					.builder()
					.type("wbem")
					.query("SELECT __PATH,Model,EMCSerialNumber FROM EMC_ArrayChassis")
					.namespace("root/emc")
					.key("${source::monitors.enclosure.allAtOnce.sources.source(1)}")
					.build(),
				"source(2)",
				WbemSource
					.builder()
					.type("wbem")
					.query("SELECT Antecedent,Dependent FROM EMC_ComputerSystemPackage")
					.namespace("root/emc")
					.key("${source::monitors.enclosure.allAtOnce.sources.source(2)}")
					.build(),
				"source(3)",
				WbemSource
					.builder()
					.type("wbem")
					.query("SELECT Antecedent,Dependent FROM EMC_SystemPackaging")
					.namespace("root/emc")
					.key("${source::monitors.enclosure.allAtOnce.sources.source(3)}")
					.build(),
				"source(4)",
				TableUnionSource
					.builder()
					.type("tableUnion")
					.tables(
						new ArrayList<>(
							List.of(
								"$monitors.enclosure.discovery.source(1)$",
								"$monitors.enclosure.discovery.source(2)$"
							)
						)
					)
					.key("${source::monitors.enclosure.allAtOnce.sources.source(4)}")
					.build(),
				"source(5)",
				WbemSource
					.builder()
					.type("wbem")
					.query("SELECT __PATH,ElementName,Description,OtherIdentifyingInfo,OperationalStatus FROM EMC_StorageSystem")
					.namespace("root/emc")
					.key("${source::monitors.enclosure.allAtOnce.sources.source(5)}")
					.build(),
				"source(6)",
				TableJoinSource
					.builder()
					.type("tableJoin")
					.leftTable("${source::monitors.enclosure.discovery.sources.source(1)}")
					.rightTable("${source::monitors.enclosure.discovery.sources.source(4)}")
					.leftKeyColumn(1)
					.rightKeyColumn(1)
					.keyType("WBEM")
					.defaultRightLine(";;")
					.key("${source::monitors.enclosure.allAtOnce.sources.source(6)}")
					.build(),
				"source(7)",
				TableJoinSource
					.builder()
					.type("tableJoin")
					.leftTable("${source::monitors.enclosure.discovery.sources.source(6)}")
					.rightTable("${source::monitors.enclosure.discovery.sources.source(5)}")
					.leftKeyColumn(5)
					.rightKeyColumn(1)
					.keyType("WBEM")
					.defaultRightLine(";;;;")
					.key("${source::monitors.enclosure.allAtOnce.sources.source(7)}")
					.build()
			)
		);

		assertEquals(expectedSources, allAtOnce.getSources());

		final Mapping mapping = allAtOnce.getMapping();

		final Mapping expectedMapping = Mapping
			.builder()
			.source("${source::monitors.enclosure.discovery.sources.Source(7)}")
			.attributes(
				Map.of(
					"id", "buildId($column(6))",
					"parent", "",
					"name", "buildName(Storage, EMC, $column(2), (, $column(7), ))",
					"model", "$column(2)",
					"vendor", "EMC",
					"serial_number", "$column(3)",
					"type", "Storage"
				)
			)
			.conditionalCollection(Map.of("hw.status", "$column(10)"))
			.metrics(Map.of("hw.status", "$column(10)"))
			.build();

		assertEquals(expectedMapping, mapping);
	}
}
