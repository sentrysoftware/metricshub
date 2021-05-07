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
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.CollectType;

class ValueTableProcessorTest {

	private CollectTypeProcessor collectTypeProcessor;
	private ValueTableProcessor valueTableProcessor;
	private CollectParameterProcessor collectParameterProcessor;
	private Connector connector;

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

	private static final String MONO_INSTANCE = "MonoInstance";
	private static final String MULTI_INSTANCE = "MultiInstance";
	private static final String VALUE_TABLE_EXAMPLE = "%Enclosure.Collect.Source(5)%";
	private static final String DEVICE_ID_EXAMPLE = "ValueTable.Column(1)";
	private static final String STATUS_EXAMPLE = "ValueTable.Column(2)";
	private static final String STATUS_INFORMATION_EXAMPLE = "ValueTable.Column(3)";
	private static final String INTRUSION_STATUS_EXAMPLE = "ValueTable.Column(5)";
	private static final String ENERGY_USAGE_EXAMPLE = "ValueTable.Column(7)";

	private static final String VALUE_TABLE_RESULT = "Enclosure.Collect.Source(5)";

	@BeforeEach
	void setUp() {
		connector = new Connector();
		collectTypeProcessor = new CollectTypeProcessor();
		valueTableProcessor = new ValueTableProcessor();
		collectParameterProcessor = new CollectParameterProcessor();
	}

	@Test
	void testDetect() {
		assertFalse(collectTypeProcessor.detect(null, null, null));
		assertFalse(collectTypeProcessor.detect(null, MONO_INSTANCE, null));
		assertFalse(collectTypeProcessor.detect(TYPE_KEY, null, null));
		assertTrue(collectTypeProcessor.detect(TYPE_KEY, MONO_INSTANCE, null));

		assertFalse(valueTableProcessor.detect(null, null, null));
		assertFalse(valueTableProcessor.detect(null, VALUE_TABLE_EXAMPLE, null));
		assertFalse(valueTableProcessor.detect(VALUE_TABLE_KEY, null, null));
		assertTrue(valueTableProcessor.detect(VALUE_TABLE_KEY, VALUE_TABLE_EXAMPLE, null));

		assertFalse(collectParameterProcessor.detect(null, null, null));
		assertFalse(collectParameterProcessor.detect(null, DEVICE_ID_EXAMPLE, null));
		assertFalse(collectParameterProcessor.detect(DEVICE_ID_KEY, null, null));
		assertTrue(collectParameterProcessor.detect(DEVICE_ID_KEY, DEVICE_ID_EXAMPLE, null));

		assertTrue(collectTypeProcessor.detect(TYPE_KEY, MONO_INSTANCE, connector));
		assertTrue(valueTableProcessor.detect(VALUE_TABLE_KEY, VALUE_TABLE_EXAMPLE, connector));
		assertTrue(collectParameterProcessor.detect(DEVICE_ID_KEY, DEVICE_ID_EXAMPLE, connector));
		assertTrue(collectParameterProcessor.detect(STATUS_KEY, STATUS_EXAMPLE, connector));
		assertTrue(collectParameterProcessor.detect(STATUS_INFORMATION_KEY, STATUS_INFORMATION_EXAMPLE, connector));
		assertTrue(collectParameterProcessor.detect(INTRUSION_STATUS_KEY, INTRUSION_STATUS_EXAMPLE, connector));
		assertTrue(collectParameterProcessor.detect(ENERGY_USAGE_KEY, ENERGY_USAGE_EXAMPLE, connector));

		assertFalse(collectTypeProcessor.detect(VALUE_TABLE_KEY, VALUE_TABLE_EXAMPLE, connector));
		assertFalse(collectTypeProcessor.detect(DEVICE_ID_KEY, DEVICE_ID_EXAMPLE, connector));

		assertFalse(valueTableProcessor.detect(TYPE_KEY, MONO_INSTANCE, connector));
		assertFalse(valueTableProcessor.detect(DEVICE_ID_KEY, DEVICE_ID_EXAMPLE, connector));

		assertFalse(collectParameterProcessor.detect(VALUE_TABLE_KEY, VALUE_TABLE_EXAMPLE, connector));
		assertFalse(collectParameterProcessor.detect(TYPE_KEY, MONO_INSTANCE, connector));
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
		collectTypeProcessor.parse(TYPE_KEY, MONO_INSTANCE, connector);
		valueTableProcessor.parse(VALUE_TABLE_KEY, VALUE_TABLE_EXAMPLE, connector);
		collectParameterProcessor.parse(DEVICE_ID_KEY, DEVICE_ID_EXAMPLE, connector);
		collectParameterProcessor.parse(STATUS_KEY, STATUS_EXAMPLE, connector);
		collectParameterProcessor.parse(STATUS_INFORMATION_KEY, STATUS_INFORMATION_EXAMPLE, connector);
		collectParameterProcessor.parse(INTRUSION_STATUS_KEY, INTRUSION_STATUS_EXAMPLE, connector);
		collectParameterProcessor.parse(ENERGY_USAGE_KEY, ENERGY_USAGE_EXAMPLE, connector);

		HardwareMonitor hardwareMonitor = connector.getHardwareMonitors().get(0);

		assertEquals(MonitorType.ENCLOSURE, hardwareMonitor.getType());

		Collect collect = hardwareMonitor.getCollect();

		assertEquals(CollectType.MONO_INSTANCE, collect.getType());
		assertEquals(VALUE_TABLE_RESULT, collect.getValueTable());
		assertEquals(DEVICE_ID_EXAMPLE, collect.getParameters().get(DEVICE_ID));
		assertEquals(STATUS_EXAMPLE, collect.getParameters().get(STATUS));
		assertEquals(STATUS_INFORMATION_EXAMPLE, collect.getParameters().get(STATUS_INFORMATION));
		assertEquals(INTRUSION_STATUS_EXAMPLE, collect.getParameters().get(INTRUSION_STATUS));
		assertEquals(ENERGY_USAGE_EXAMPLE, collect.getParameters().get(ENERGY_USAGE));

		// Parsing of Enclosure.Collect.Type="MultiInstance"
		collectTypeProcessor.parse(TYPE_KEY, MULTI_INSTANCE, connector);
		assertEquals(CollectType.MULTI_INSTANCE, collect.getType());
	}
}
