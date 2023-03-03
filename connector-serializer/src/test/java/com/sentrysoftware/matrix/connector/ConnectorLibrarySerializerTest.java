package com.sentrysoftware.matrix.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.task.Mapping;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WbemSource;

class ConnectorLibrarySerializerTest {

	@TempDir
	Path tempDir;

	@Test
	void testSerializeConnectorSources() throws IOException, ClassNotFoundException {
		ConnectorLibrarySerializer.main(new String[] { "src/test/resources/connector", tempDir.toAbsolutePath().toString() });

		final Connector connector;
		try (InputStream inputStream = new FileInputStream(tempDir.resolve("WBEMConnector").toFile());
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

			connector = (Connector) objectInputStream.readObject();

		}

		final Map<String, MonitorJob> monitors = connector.getMonitors();

		final MonitorJob job = monitors.get("enclosure");

		assertTrue(job instanceof StandardMonitorJob, () -> "MonitorJob is expected to be a StandardMonitorJob");

		final StandardMonitorJob standard = (StandardMonitorJob) job;

		final Discovery discovery = standard.getDiscovery();

		assertNotNull(discovery);

		final Map<String, Source> expectedSources = new LinkedHashMap<>() {

			private static final long serialVersionUID = 1L;

			{
				put(
					"source(1)",
					WbemSource
						.builder()
						.type("wbem")
						.query("SELECT __PATH,Model,EMCSerialNumber FROM EMC_ArrayChassis")
						.namespace("root/emc")
						.key("$monitors.enclosure.discovery.sources.source(1)$")
						.build()
				);
			}
			{
				put(
					"source(2)",
					WbemSource
						.builder()
						.type("wbem")
						.query("SELECT Antecedent,Dependent FROM EMC_ComputerSystemPackage")
						.namespace("root/emc")
						.key("$monitors.enclosure.discovery.sources.source(2)$")
						.build()
				);
			}
			{
				put(
					"source(3)",
					WbemSource
						.builder()
						.type("wbem")
						.query("SELECT Antecedent,Dependent FROM EMC_SystemPackaging")
						.namespace("root/emc")
						.key("$monitors.enclosure.discovery.sources.source(3)$")
						.build()
				);
			}
			{
				put(
					"source(4)",
					TableUnionSource
						.builder()
						.type("tableUnion")
						.tables(
							new ArrayList<>(
								List.of(
									"$monitors.enclosure.discovery.sources.source(2)$",
									"$monitors.enclosure.discovery.sources.source(3)$"
								)
							)
						)
						.key("$monitors.enclosure.discovery.sources.source(4)$")
						.build()
				);
			}
			{
				put(
					"source(5)",
					WbemSource
						.builder()
						.type("wbem")
						.query("SELECT __PATH,ElementName,Description,OtherIdentifyingInfo,OperationalStatus FROM EMC_StorageSystem")
						.namespace("root/emc")
						.key("$monitors.enclosure.discovery.sources.source(5)$")
						.build()
				);
			}
			{
				put(
					"source(6)",
					TableJoinSource
						.builder()
						.type("tableJoin")
						.leftTable("$monitors.enclosure.discovery.sources.source(1)$")
						.rightTable("$monitors.enclosure.discovery.sources.source(4)$")
						.leftKeyColumn(1)
						.rightKeyColumn(1)
						.keyType("WBEM")
						.defaultRightLine(";;")
						.key("$monitors.enclosure.discovery.sources.source(6)$")
						.build()
				);
			}
			{
				put(
					"source(7)",
					TableJoinSource
						.builder()
						.type("tableJoin")
						.leftTable("$monitors.enclosure.discovery.sources.source(6)$")
						.rightTable("$monitors.enclosure.discovery.sources.source(5)$")
						.leftKeyColumn(5)
						.rightKeyColumn(1)
						.keyType("WBEM")
						.defaultRightLine(";;;;")
						.key("$monitors.enclosure.discovery.sources.source(7)$")
						.build()
				);
			}
		};


		assertEquals(expectedSources, discovery.getSources());

		// Check order
		assertEquals(expectedSources.keySet().stream().toList(), discovery.getSources().keySet().stream().toList());

		final Mapping mapping = discovery.getMapping();

		final Mapping expectedMapping = Mapping
			.builder()
			.source("$monitors.enclosure.discovery.sources.source(7)$")
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

}
