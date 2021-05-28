package com.sentrysoftware.matrix.engine.strategy.collect;

import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ENCLOSURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.model.monitor.Monitor;
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
	void testGetOptionalDouble() {
		assertEquals(OptionalDouble.empty(), CollectHelper.getOptionalDouble(null));
		assertEquals(OptionalDouble.of(1.0), CollectHelper.getOptionalDouble(1));
		assertEquals(OptionalDouble.of(1.0), CollectHelper.getOptionalDouble(1F));
		assertEquals(OptionalDouble.of(1.0), CollectHelper.getOptionalDouble(1L));
		assertEquals(OptionalDouble.of(1.0), CollectHelper.getOptionalDouble(1D));
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

		assertEquals(OptionalDouble.of(100.0), CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, false));
		assertEquals(OptionalDouble.empty(), CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, true));
		assertEquals(OptionalDouble.empty(), CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER, true));
		assertEquals(OptionalDouble.empty(), CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER, false));

		numberParam.reset();

		assertEquals(OptionalDouble.of(100.0), CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, true));
		assertEquals(OptionalDouble.empty(), CollectHelper.getNumberParamRawValue(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, false));
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

		assertEquals(OptionalDouble.of(collectTime), CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, false));
		assertEquals(OptionalDouble.empty(), CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, true));
		assertEquals(OptionalDouble.empty(), CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER, true));
		assertEquals(OptionalDouble.empty(), CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER, false));

		numberParam.reset();

		assertEquals(OptionalDouble.of(collectTime), CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, true));
		assertEquals(OptionalDouble.empty(), CollectHelper.getNumberParamCollectTime(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER, false));
	}

	@Test
	void testSubtract() {
		assertEquals(OptionalDouble.of(10), CollectHelper.subtract(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(50), OptionalDouble.of(40)));
		assertEquals(OptionalDouble.empty(), CollectHelper.subtract(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(30), OptionalDouble.of(40)));
		assertEquals(OptionalDouble.empty(), CollectHelper.subtract(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.empty(), OptionalDouble.of(40)));
		assertEquals(OptionalDouble.empty(), CollectHelper.subtract(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(30), OptionalDouble.empty()));
	}

	@Test
	void testDivide() {
		assertEquals(OptionalDouble.of(2), CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(50), OptionalDouble.of(25)));
		assertEquals(OptionalDouble.empty(), CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(30), OptionalDouble.of(0)));
		assertEquals(OptionalDouble.empty(), CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.empty(), OptionalDouble.of(40)));
		assertEquals(OptionalDouble.empty(), CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(30), OptionalDouble.empty()));
		assertEquals(OptionalDouble.empty(), CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(-50), OptionalDouble.of(25)));
		assertEquals(OptionalDouble.empty(), CollectHelper.divide(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(50), OptionalDouble.of(-25)));
	}

	@Test
	void testMultiply() {
		assertEquals(OptionalDouble.of(500), CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(50), OptionalDouble.of(10)));
		assertEquals(-0D, CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(-30), OptionalDouble.of(0)).getAsDouble());
		assertEquals(OptionalDouble.empty(), CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.empty(), OptionalDouble.of(40)));
		assertEquals(OptionalDouble.empty(), CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(30), OptionalDouble.empty()));
		assertEquals(OptionalDouble.empty(), CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(-50), OptionalDouble.of(25)));
		assertEquals(OptionalDouble.empty(), CollectHelper.multiply(HardwareConstants.ENERGY_USAGE_PARAMETER, OptionalDouble.of(50), OptionalDouble.of(-25)));
	}

	@Test
	void testRate() {
		final long collectTime = new Date().getTime();
		final long previousCollectTime = collectTime - (2 * 60 * 1000);

		assertEquals(OptionalDouble.of(1), CollectHelper.rate(HardwareConstants.ENERGY_USAGE_PARAMETER,
				OptionalDouble.of(240000), OptionalDouble.of(120000),
				OptionalDouble.of(collectTime), OptionalDouble.of(previousCollectTime)));
	}

}
