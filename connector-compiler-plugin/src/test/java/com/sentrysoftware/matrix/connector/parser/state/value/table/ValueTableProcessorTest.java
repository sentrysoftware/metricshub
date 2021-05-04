package com.sentrysoftware.matrix.connector.parser.state.value.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;

public class ValueTableProcessorTest {

	private ValueTableProcessor valueTableProcessor;
	private Connector connector;

	private static final String TYPE = "Type";
	private static final String VALUE_TABLE = "ValueTable";
	private static final String DEVICE_ID = "DeviceId";
	private static final String STATUS = "Status";
	private static final String STATUS_INFORMATION = "StatusInformation";
	private static final String INTRUSION_STATUS = "IntrusionStatus";
	private static final String ENERGY_USAGE = "EnergyUsage";

	private static final String TYPE_KEY = "Enclosure.Collect.Type";
	private static final String VALUE_TABLE_KEY = "Enclosure.Collect.ValueTable";
	private static final String DEVICE_ID_KEY = "Enclosure.Collect.DeviceId";
	private static final String STATUS_KEY = "Enclosure.Collect.Status";
	private static final String STATUS_INFORMATION_KEY = "Enclosure.Collect.StatusInformation";
	private static final String INTRUSION_STATUS_KEY = "Enclosure.Collect.IntrusionStatus";
	private static final String ENERGY_USAGE_KEY = "Enclosure.Collect.EnergyUsage";

	private static final String MONO_INSTANCE = "monoinstance";
	private static final String VALUE_TABLE_EXAMPLE = "%Enclosure.Collect.Source(5)%";
	private static final String DEVICE_ID_EXAMPLE = "ValueTable.Column(1)";
	private static final String STATUS_EXAMPLE = "ValueTable.Column(2)";
	private static final String STATUS_INFORMATION_EXAMPLE = "ValueTable.Column(3)";
	private static final String INTRUSION_STATUS_EXAMPLE = "ValueTable.Column(5)";
	private static final String ENERGY_USAGE_EXAMPLE = "ValueTable.Column(7)";

	@BeforeEach
	void setUp() {
		connector = new Connector();
		valueTableProcessor = new ValueTableProcessor();
	}

	@Test
	void testDetect() {
		assertFalse(valueTableProcessor.detect(null, null, null));
		assertFalse(valueTableProcessor.detect(null, MONO_INSTANCE, null));
		assertFalse(valueTableProcessor.detect(TYPE_KEY, null, null));
		assertTrue(valueTableProcessor.detect(TYPE_KEY, MONO_INSTANCE, null));

		assertTrue(valueTableProcessor.detect(TYPE_KEY, MONO_INSTANCE, connector));
		assertTrue(valueTableProcessor.detect(VALUE_TABLE_KEY, VALUE_TABLE_EXAMPLE, connector));
		assertTrue(valueTableProcessor.detect(DEVICE_ID_KEY, DEVICE_ID_EXAMPLE, connector));
		assertTrue(valueTableProcessor.detect(STATUS_KEY, STATUS_EXAMPLE, connector));
		assertTrue(valueTableProcessor.detect(STATUS_INFORMATION_KEY, STATUS_INFORMATION_EXAMPLE, connector));
		assertTrue(valueTableProcessor.detect(INTRUSION_STATUS_KEY, INTRUSION_STATUS_EXAMPLE, connector));
		assertTrue(valueTableProcessor.detect(ENERGY_USAGE_KEY, ENERGY_USAGE_EXAMPLE, connector));
	}

	@Test
	void testParse() {
		/*
		 * Parsing of : 
		 * Enclosure.Collect.Type="MonoInstance"
		 * Enclosure.Collect.ValueTable=%Enclosure.Collect.Source(5)%
		 * Enclosure.Collect.DeviceID=ValueTable.Column(1)
		 * Enclosure.Collect.Status=ValueTable.Column(2)
		 * Enclosure.Collect.StatusInformation=ValueTable.Column(3)
		 * Enclosure.Collect.IntrusionStatus=ValueTable.Column(5)
		 * Enclosure.Collect.EnergyUsage=ValueTable.Column(7)
		 */
		valueTableProcessor.parse(TYPE_KEY, MONO_INSTANCE, connector);
		valueTableProcessor.parse(VALUE_TABLE_KEY, VALUE_TABLE_EXAMPLE, connector);
		valueTableProcessor.parse(DEVICE_ID_KEY, DEVICE_ID_EXAMPLE, connector);
		valueTableProcessor.parse(STATUS_KEY, STATUS_EXAMPLE, connector);
		valueTableProcessor.parse(STATUS_INFORMATION_KEY, STATUS_INFORMATION_EXAMPLE, connector);
		valueTableProcessor.parse(INTRUSION_STATUS_KEY, INTRUSION_STATUS_EXAMPLE, connector);
		valueTableProcessor.parse(ENERGY_USAGE_KEY, ENERGY_USAGE_EXAMPLE, connector);

		HardwareMonitor hardwareMonitor = connector.getHardwareMonitors().get(0);

		assertEquals(MonitorType.ENCLOSURE, hardwareMonitor.getType());

		Collect collect = hardwareMonitor.getCollect();

		assertEquals(MONO_INSTANCE, collect.getParameters().get(TYPE));
		assertEquals(VALUE_TABLE_EXAMPLE, collect.getParameters().get(VALUE_TABLE));
		assertEquals(DEVICE_ID_EXAMPLE, collect.getParameters().get(DEVICE_ID));
		assertEquals(STATUS_EXAMPLE, collect.getParameters().get(STATUS));
		assertEquals(STATUS_INFORMATION_EXAMPLE, collect.getParameters().get(STATUS_INFORMATION));
		assertEquals(INTRUSION_STATUS_EXAMPLE, collect.getParameters().get(INTRUSION_STATUS));
		assertEquals(ENERGY_USAGE_EXAMPLE, collect.getParameters().get(ENERGY_USAGE));
	}
}
