package com.sentrysoftware.matrix.engine.strategy.collect;

import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ENCLOSURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

class CollectHelperTest {

	private static final List<String> ROW = Arrays.asList("0", "100", "400");
	private static final ParameterState UNKNOWN_STATUS_WARN = ParameterState.WARN;
	private static final ParameterState UNKNOWN_STATUS_OK = ParameterState.OK;
	private static final ParameterState UNKNOWN_STATUS_ALARM = ParameterState.ALARM;
	private static final String HOST_NAME = "host";
	private static final String ID = "enclosure_1";
	private static final String VALUE_TABLE = "Enclosure.Collect.Source(1)";

	@Test
	void testTranslateStatus() {
		assertNull(CollectHelper.translateStatus(null,
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.OK, CollectHelper.translateStatus("0",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.OK, CollectHelper.translateStatus("OK",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(UNKNOWN_STATUS_WARN, CollectHelper.translateStatus("1",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(UNKNOWN_STATUS_WARN, CollectHelper.translateStatus("WARN",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.ALARM, CollectHelper.translateStatus("2",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.ALARM, CollectHelper.translateStatus("ALARM",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.ALARM, CollectHelper.translateStatus("ON",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.WARN, CollectHelper.translateStatus("BLINKING",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(ParameterState.OK, CollectHelper.translateStatus("OFF",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(UNKNOWN_STATUS_WARN, CollectHelper.translateStatus("SOMETHING_UNKNOWN",
				UNKNOWN_STATUS_WARN,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(UNKNOWN_STATUS_OK, CollectHelper.translateStatus("SOMETHING_UNKNOWN",
				UNKNOWN_STATUS_OK,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));

		assertEquals(UNKNOWN_STATUS_ALARM, CollectHelper.translateStatus("SOMETHING_UNKNOWN",
				UNKNOWN_STATUS_ALARM,
				ID,
				HOST_NAME,
				HardwareConstants.STATUS_PARAMETER));
	}


	@Test
	void testGetValueTableColumnValue() {

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				Collections.emptyList(),
				null));

		assertEquals("100", CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(2)"));

		assertEquals("400", CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(3)"));

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(4)"));

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(string)"));

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(-1)"));

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				HardwareConstants.STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(0)"));
	}

	@Test
	void testGetDoubleValue() {
		assertNull(CollectHelper.getDoubleValue(null));
		assertEquals(1.0, CollectHelper.getDoubleValue(1));
		assertEquals(1.0, CollectHelper.getDoubleValue(1F));
		assertEquals(1.0, CollectHelper.getDoubleValue(1L));
		assertEquals(1.0, CollectHelper.getDoubleValue(1D));
	}

	@Test
	void testGetNumberParamRawValue() {
		final NumberParam numberParam = NumberParam.builder()
				.value(10.0)
				.rawValue(100.0)
				.build();

		final Monitor monitor = Monitor.builder()
				.parameters(Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER, numberParam))
				.build();

		assertEquals(100.0, CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, false));
		assertNull(CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, true));
		assertNull(CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER, true));
		assertNull(CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER, false));

		numberParam.reset();

		assertEquals(100.0, CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, true));
		assertNull(CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, false));
	}

	@Test
	void testGetNumberParamCollectTime() {
		final long collectTime = new Date().getTime();

		final NumberParam numberParam = NumberParam.builder()
				.value(10.0)
				.rawValue(100.0)
				.collectTime(collectTime)
				.build();

		final Monitor monitor = Monitor.builder()
				.parameters(Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER, numberParam))
				.build();

		assertEquals(collectTime, CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, false));
		assertNull(CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, true));
		assertNull(CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER, true));
		assertNull(CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER, false));

		numberParam.reset();

		assertEquals(collectTime, CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, true));
		assertNull(CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, false));
	}

	@Test
	void testSubtract() {
		assertEquals(10, CollectHelper.subtract(HardwareConstants.ENERGY_USAGE_PARAMETER, 50D, 40D));
		assertNull(CollectHelper.subtract(HardwareConstants.ENERGY_USAGE_PARAMETER, 30D, 40D));
		assertNull(CollectHelper.subtract(HardwareConstants.ENERGY_USAGE_PARAMETER, null, 40D));
		assertNull(CollectHelper.subtract(HardwareConstants.ENERGY_USAGE_PARAMETER, 30D, null));
	}

	@Test
	void testDivide() {
		assertEquals(2, CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, 50D, 25D));
		assertNull(CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, 30D, 0D));
		assertNull(CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, null, 40D));
		assertNull(CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, 30D, null));
		assertNull(CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, -50D, 25D));
		assertNull(CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, 50D, -25D));
	}

	@Test
	void testMultiply() {
		assertEquals(500, CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, 50D, 10D));
		assertEquals(-0D, CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, -30D, 0D));
		assertNull(CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, null, 40D));
		assertNull(CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, 30D, null));
		assertNull(CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, -50D, 25D));
		assertNull(CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, 50D, -25D));
	}

	@Test
	void testRate() {
		final Long collectTime = new Date().getTime();
		final Long previousCollectTime = collectTime - (2 * 60 * 1000);

		assertEquals(1, CollectHelper.rate(HardwareConstants.ENERGY_USAGE_PARAMETER,
				240000D, 120000D,
				collectTime.doubleValue(), previousCollectTime.doubleValue()));
	}

	@Test
	void testGetNumberParamValue() {
		Map<String, IParameterValue> parameters = new HashMap<>();
		NumberParam numberParam = NumberParam.builder().value(10.0).build();
		String parameterName = "parameter";
		parameters.put(parameterName, numberParam);
		Monitor monitor = Monitor.builder().parameters(parameters).build();
		assertNull(CollectHelper.getNumberParamValue(monitor, "wrongParameter"));
		assertEquals(10.0, CollectHelper.getNumberParamValue(monitor, parameterName));
	}
}
