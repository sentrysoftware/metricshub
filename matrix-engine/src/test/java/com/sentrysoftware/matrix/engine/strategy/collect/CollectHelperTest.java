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
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

class CollectHelperTest {

	private static final List<String> ROW = Arrays.asList("0", "100", "400");
	private static final ParameterState UNKNOWN_STATUS_WARN = ParameterState.WARN;
	private static final ParameterState UNKNOWN_STATUS_OK = ParameterState.OK;
	private static final ParameterState UNKNOWN_STATUS_ALARM = ParameterState.ALARM;
	private static final String HOST_NAME = "host";
	private static final String ID = "enclosure_1";
	private static final String VALUE_TABLE = "Enclosure.Collect.Source(1)";
	private static final String ECS1_01 = "ecs1-01";
	private static Long collectTime = new Date().getTime();

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

	@Test
	void testGetStatusParamState() {
		Map<String, IParameterValue> parameters = new HashMap<>();
		StatusParam statusParam = StatusParam.builder().name("status").state(ParameterState.OK).build();
		parameters.put("status", statusParam);
		Monitor monitor = Monitor.builder().parameters(parameters).build();
		assertNull(CollectHelper.getStatusParamState(monitor, "wrongStatus"));
		assertEquals(ParameterState.OK, CollectHelper.getStatusParamState(monitor, "status"));
	}


	@Test
	void testUpdateNumberParameter() {
		{
			final Monitor monitor = Monitor.builder().build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.ENERGY_USAGE_PARAMETER,
				HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT,
				collectTime,
				100D,
				1500D
			);

			final NumberParam expected = NumberParam
					.builder()
					.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
					.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
					.collectTime(collectTime)
					.value(100D)
					.rawValue(1500D)
					.build();

			assertEquals(expected, monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class));
		}

		{
			final NumberParam previousParameter = NumberParam
					.builder()
					.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
					.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
					.collectTime(collectTime)
					.value(100D)
					.rawValue(1500D)
					.build();

			previousParameter.reset();

			final Monitor monitor = Monitor.builder().parameters(new HashMap<>(
					Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER, previousParameter)))
					.build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.ENERGY_USAGE_PARAMETER,
				HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT,
				collectTime + (2 * 60 * 1000),
				50D,
				1550D
			);

			final NumberParam expected = NumberParam
					.builder()
					.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
					.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
					.collectTime(collectTime + (2 * 60 * 1000))
					.value(50D)
					.rawValue(1550D)
					.build();
			expected.setPreviousCollectTime(collectTime);
			expected.setPreviousRawValue(1500D);

			assertEquals(expected, monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class));
		}
	}

	@Test
	void testUpdateStatusParameter() {
		{
			final Monitor monitor = Monitor.builder().build();
			CollectHelper.updateStatusParameter(monitor, HardwareConstants.STATUS_PARAMETER,
					HardwareConstants.STATUS_PARAMETER_UNIT, collectTime, ParameterState.OK, "Operable");

			final StatusParam expected = StatusParam
					.builder()
					.name(HardwareConstants.STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(ParameterState.OK)
					.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
					.statusInformation("status: 0 (Operable)")
					.build();

			assertEquals(expected, monitor.getParameter(HardwareConstants.STATUS_PARAMETER, StatusParam.class));
		}

		{
			final StatusParam previousParameter = StatusParam.builder()
					.name(HardwareConstants.STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(ParameterState.ALARM)
					.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
					.statusInformation("status: 2 (DOWN)").build();

			previousParameter.reset();

			final Monitor monitor = Monitor.builder().parameters(new HashMap<>(
					Map.of(HardwareConstants.STATUS_PARAMETER, previousParameter)))
					.build();

			CollectHelper.updateStatusParameter(monitor, HardwareConstants.STATUS_PARAMETER,
					HardwareConstants.STATUS_PARAMETER_UNIT, collectTime, ParameterState.OK, "Operable");

			final StatusParam expected = StatusParam
					.builder()
					.name(HardwareConstants.STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(ParameterState.OK)
					.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
					.statusInformation("status: 0 (Operable)")
					.build();

			expected.setPreviousState(ParameterState.ALARM);

			assertEquals(expected, monitor.getParameter(HardwareConstants.STATUS_PARAMETER, StatusParam.class));
		}
	}

	@Test
	void testCollectPowerWithEnergyUsageFirstCollect() {

		final Monitor monitor = Monitor.builder()
				.monitorType(MonitorType.ENCLOSURE)
				.build();

		CollectHelper.collectPowerFromEnergyUsage(monitor, collectTime, 3138.358D, ECS1_01);

		assertNull(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue());
		assertNull(monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertEquals(3138.358D, monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getRawValue());

	}

	@Test
	void testCollectPowerFromEnergyUsage() {

		final NumberParam energyUsage = NumberParam
				.builder()
				.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
				.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
				.collectTime(collectTime)
				.value(null)
				.rawValue(3138.358D) // kWatt-hours
				.build();
		energyUsage.reset();

		final Monitor monitor = Monitor.builder().monitorType(MonitorType.ENCLOSURE).parameters(new HashMap<>(
				Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER, energyUsage)))
				.build();

		CollectHelper.collectPowerFromEnergyUsage(monitor, collectTime + (2 * 60 * 1000), 3138.360, ECS1_01);

		Double joules = monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue();
		joules  = Math.round(joules * 100000D) / 100000D;

		assertEquals(7200, joules); // Joules (Energy)

		final double watts = Math.round(monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue());
		assertEquals(60.0, watts); // Watts (Power)

	}

	@Test
	void testCollectEnergyUsageFromPowerFirstCollect() {

		final Monitor monitor = Monitor.builder()
				.monitorType(MonitorType.ENCLOSURE)
				.build();

		CollectHelper.collectEnergyUsageFromPower(monitor, collectTime, 60D, ECS1_01);

		assertNull(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class));
		assertEquals(60, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue());
		assertEquals(60, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getRawValue());
	}

	@Test
	void testCollectEnergyUsageFromPower() {

		final NumberParam powerConsumption = NumberParam
				.builder()
				.name(HardwareConstants.POWER_CONSUMPTION_PARAMETER)
				.unit(HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT)
				.collectTime(collectTime)
				.value(null)
				.rawValue(60.0)
				.build();

		powerConsumption.reset();

		final NumberParam energyUsage = NumberParam
			.builder()
			.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
			.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
			.collectTime(collectTime)
			.value(null)
			.rawValue(999.0)
			.build();

		energyUsage.reset();

		final Monitor monitor = Monitor
			.builder()
			.monitorType(MonitorType.ENCLOSURE)
			.parameters(new HashMap<>(
				Map
					.of(HardwareConstants.POWER_CONSUMPTION_PARAMETER, powerConsumption,
						HardwareConstants.ENERGY_USAGE_PARAMETER, energyUsage)))
			.build();

		CollectHelper.collectEnergyUsageFromPower(monitor, collectTime + (2 * 60 * 1000), 64D, ECS1_01);

		assertEquals(64, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()); // Watts
		assertEquals(64, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getRawValue());

		assertEquals(7680.0, monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue()); // Joules
		assertEquals(7680.0, monitor.getParameter(HardwareConstants.ENERGY_PARAMETER, NumberParam.class).getValue()); // Joules
	}

	@Test
	void testCollectEnergyUsageFromPowerManyCollects() {

		// Collect 1
		final Monitor monitor = Monitor
			.builder()
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		CollectHelper.collectEnergyUsageFromPower(monitor, collectTime , 64D, ECS1_01);

		assertEquals(64, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()); // Watts
		assertNull(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class)); // Joules
		assertNull(monitor.getParameter(HardwareConstants.ENERGY_PARAMETER, NumberParam.class)); // Joules

		// Collect 2 (first collect time + 2 minutes)
		monitor.getParameters().values().forEach(IParameterValue::reset);

		CollectHelper.collectEnergyUsageFromPower(monitor, collectTime + (2 * 60 * 1000), 60D, ECS1_01);

		assertEquals(60D, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()); // Watts
		assertEquals(7200.0,monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue()); // Joules
		assertEquals(7200.0,monitor.getParameter(HardwareConstants.ENERGY_PARAMETER, NumberParam.class).getValue()); // Joules

		// Collect 3  (first collect time + 4 minutes)
		monitor.getParameters().values().forEach(IParameterValue::reset);

		CollectHelper.collectEnergyUsageFromPower(monitor, collectTime + (4 * 60 * 1000), 64D, ECS1_01);

		assertEquals(64, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()); // Watts
		assertEquals(7680.0,monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue()); // Joules
		assertEquals(14880.0,monitor.getParameter(HardwareConstants.ENERGY_PARAMETER, NumberParam.class).getValue()); // Joules
	}

}
