package com.sentrysoftware.hardware.cli.component.cli.printer;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DISPLAY_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOST_FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ID_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

class PrettyPrinterTest {

	private static final String PARAMETER_ACTIVATION_STATUS_METADATA = "parameteractivation.status";
	private static final String HOSTNAME = UUID.randomUUID().toString();

	@Test
	void testPrint() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor host = Monitor
			.builder()
			.id(HOSTNAME)
			.parentId(null)
			.hostId(HOSTNAME)
			.name(HOSTNAME)
			.monitorType(MonitorType.HOST)
			.metadata(Map.of(FQDN, HOSTNAME))
			.build();

		hostMonitoring.addMonitor(host);

		final Map<String, String> enclosureMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		enclosureMetadata.put(ID_COUNT, "1");
		enclosureMetadata.put(VENDOR, "Pure");
		enclosureMetadata.put(MODEL, "FA-X20R2");
		enclosureMetadata.put(SERIAL_NUMBER, "FA-123456789");
		enclosureMetadata.put(HOST_FQDN, HOSTNAME);
		enclosureMetadata.put(PARAMETER_ACTIVATION_STATUS_METADATA, "OK");
		enclosureMetadata.put(DISPLAY_ID, "Pure-1");

		final Monitor enclosure = Monitor
			.builder()
			.id("localhost@connector1_enclosure_1")
			.name("PureStorage FA-X20R2")
			.parentId(HOSTNAME)
			.hostId(HOSTNAME)
			.monitorType(MonitorType.ENCLOSURE)
			.extendedType(COMPUTER)
			.metadata(enclosureMetadata)
			.build();

		hostMonitoring.addMonitor(enclosure);

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintWriter writer = new PrintWriter(out);

		PrettyPrinter.print(writer, hostMonitoring, true, true);

		final String actual = new String(out.toByteArray(), StandardCharsets.UTF_8).toLowerCase();

		assertFalse(actual.contains(PARAMETER_ACTIVATION_STATUS_METADATA));
		assertFalse(actual.contains(ID_COUNT.toLowerCase()));
		assertFalse(actual.contains(DISPLAY_ID.toLowerCase()));
	}

}
