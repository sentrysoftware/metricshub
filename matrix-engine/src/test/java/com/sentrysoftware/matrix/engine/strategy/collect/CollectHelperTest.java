package com.sentrysoftware.matrix.engine.strategy.collect;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_TIME_PARAMETER;
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

import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;

class CollectHelperTest {

	private static final List<String> ROW = Arrays.asList("0", "100", "400");
	private static final String HOST_NAME = "host";
	private static final String ID = "enclosure_1";
	private static final String VALUE_TABLE = "Enclosure.Collect.Source(1)";
	private static final String ECS1_01 = "ecs1-01";
	private static Long collectTime = new Date().getTime();

	@Test
	void testTranslateState() {
		assertNull(CollectHelper.translateState(
				null,
				Status::interpret,
				ID,
				HOST_NAME,
				STATUS_PARAMETER)
		);

		assertEquals(Status.OK, CollectHelper.translateState(
				"0",
				Status::interpret,
				ID,
				HOST_NAME,
				STATUS_PARAMETER)
		);

		assertEquals(Status.OK, CollectHelper.translateState(
				"OK",
				Status::interpret,
				ID,
				HOST_NAME,
				STATUS_PARAMETER)
		);

		assertEquals(Status.DEGRADED, CollectHelper.translateState(
				"1",
				Status::interpret,
				ID,
				HOST_NAME,
				STATUS_PARAMETER)
		);

		assertEquals(Status.DEGRADED, CollectHelper.translateState(
				"WARN",
				Status::interpret,
				ID,
				HOST_NAME,
				STATUS_PARAMETER)
		);

		assertEquals(Status.FAILED, CollectHelper.translateState(
				"2",
				Status::interpret,
				ID,
				HOST_NAME,
				STATUS_PARAMETER)
		);

		assertEquals(Status.FAILED, CollectHelper.translateState(
				"ALARM",
				Status::interpret,
				ID,
				HOST_NAME,
				STATUS_PARAMETER)
		);

		assertNull(CollectHelper.translateState(
				"SOMETHING_UNKNOWN",
				Status::interpret,
				ID,
				HOST_NAME,
				STATUS_PARAMETER)
		);

	}


	@Test
	void testGetValueTableColumnValue() {

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				STATUS_PARAMETER,
				ENCLOSURE,
				Collections.emptyList(),
				null));

		assertEquals("100", CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(2)"));

		assertEquals("400", CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(3)"));

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Valuetable.Column(4)"));

		assertEquals("Server", CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				STATUS_PARAMETER,
				ENCLOSURE,
				ROW,
				"Server"));

		assertNull(CollectHelper.getValueTableColumnValue(VALUE_TABLE,
				STATUS_PARAMETER,
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
				.parameters(Map.of(ENERGY_USAGE_PARAMETER, numberParam))
				.build();

		assertEquals(100.0, CollectHelper.getNumberParamRawValue(monitor, ENERGY_USAGE_PARAMETER, false));
		assertNull(CollectHelper.getNumberParamRawValue(monitor, ENERGY_USAGE_PARAMETER, true));
		assertNull(CollectHelper.getNumberParamRawValue(monitor, POWER_CONSUMPTION_PARAMETER, true));
		assertNull(CollectHelper.getNumberParamRawValue(monitor, POWER_CONSUMPTION_PARAMETER, false));

		numberParam.save();

		assertEquals(100.0, CollectHelper.getNumberParamRawValue(monitor, ENERGY_USAGE_PARAMETER, true));
		assertEquals(100.0, CollectHelper.getNumberParamRawValue(monitor, ENERGY_USAGE_PARAMETER, false));
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
				.parameters(Map.of(ENERGY_USAGE_PARAMETER, numberParam))
				.build();

		assertEquals(collectTime, CollectHelper.getNumberParamCollectTime(monitor, ENERGY_USAGE_PARAMETER, false));
		assertNull(CollectHelper.getNumberParamCollectTime(monitor, ENERGY_USAGE_PARAMETER, true));
		assertNull(CollectHelper.getNumberParamCollectTime(monitor, POWER_CONSUMPTION_PARAMETER, true));
		assertNull(CollectHelper.getNumberParamCollectTime(monitor, POWER_CONSUMPTION_PARAMETER, false));

		numberParam.save();

		assertEquals(collectTime, CollectHelper.getNumberParamCollectTime(monitor, ENERGY_USAGE_PARAMETER, true));
		assertEquals(collectTime, CollectHelper.getNumberParamCollectTime(monitor, ENERGY_USAGE_PARAMETER, false));
	}

	@Test
	void testSubtract() {
		assertEquals(10, CollectHelper.subtract(ENERGY_USAGE_PARAMETER, 50D, 40D));
		assertNull(CollectHelper.subtract(ENERGY_USAGE_PARAMETER, 30D, 40D));
		assertNull(CollectHelper.subtract(ENERGY_USAGE_PARAMETER, null, 40D));
		assertNull(CollectHelper.subtract(ENERGY_USAGE_PARAMETER, 30D, null));
	}

	@Test
	void testDivide() {
		assertEquals(2, CollectHelper.divide(ENERGY_USAGE_PARAMETER, 50D, 25D));
		assertNull(CollectHelper.divide(ENERGY_USAGE_PARAMETER, 30D, 0D));
		assertNull(CollectHelper.divide(ENERGY_USAGE_PARAMETER, null, 40D));
		assertNull(CollectHelper.divide(ENERGY_USAGE_PARAMETER, 30D, null));
		assertNull(CollectHelper.divide(ENERGY_USAGE_PARAMETER, -50D, 25D));
		assertNull(CollectHelper.divide(ENERGY_USAGE_PARAMETER, 50D, -25D));
	}

	@Test
	void testMultiply() {
		assertEquals(500, CollectHelper.multiply(ENERGY_USAGE_PARAMETER, 50D, 10D));
		assertEquals(-0D, CollectHelper.multiply(ENERGY_USAGE_PARAMETER, -30D, 0D));
		assertNull(CollectHelper.multiply(ENERGY_USAGE_PARAMETER, null, 40D));
		assertNull(CollectHelper.multiply(ENERGY_USAGE_PARAMETER, 30D, null));
		assertNull(CollectHelper.multiply(ENERGY_USAGE_PARAMETER, -50D, 25D));
		assertNull(CollectHelper.multiply(ENERGY_USAGE_PARAMETER, 50D, -25D));
	}

	@Test
	void testRate() {
		final long collectTime = new Date().getTime();
		final long previousCollectTime = collectTime - (2 * 60 * 1000);

		assertEquals(1, CollectHelper.rate(ENERGY_USAGE_PARAMETER,
				240000D, 120000D,
			(double) collectTime, (double) previousCollectTime));
	}

	@Test
	void testRateWithPreviousValuesComputations() {

		// parameterName is null
		assertNull(CollectHelper.rate(null, null, null, null));

		// parameterName is not null, currentValue is null
		assertNull(CollectHelper.rate(USED_TIME_PARAMETER, null, null, null));

		// parameterName is not null, currentValue is not null, currentCollectTimeInMilliseconds is null
		Double currentValue = 200.0;
		assertNull(CollectHelper.rate(USED_TIME_PARAMETER, currentValue, null, null));

		// parameterName is not null, currentValue is not null, currentCollectTimeInMilliseconds is not null,
		// monitor is null
		final Long currentCollectTimeInMilliseconds = new Date().getTime();
		assertNull(CollectHelper.rate(USED_TIME_PARAMETER, currentValue, currentCollectTimeInMilliseconds, null));

		// parameterName is not null, currentValue is not null, currentCollectTimeInMilliseconds is not null,
		// monitor is not null, previousValue is null
		final Monitor monitor = Monitor.builder().id("monitorId").monitorType(MonitorType.GPU).build();
		assertNull(CollectHelper.rate(USED_TIME_PARAMETER, currentValue, currentCollectTimeInMilliseconds, monitor));

		// parameterName is not null, currentValue is not null, currentCollectTimeInMilliseconds is not null,
		// monitor is not null, previousValue is not null, previousCollectTimeInMilliseconds is null
		NumberParam usedTimeParameter = NumberParam
			.builder()
			.name(USED_TIME_PARAMETER)
			.unit(TIME_PARAMETER_UNIT)
			.rawValue(140.0)
			.value(140.0)
			.build();
		usedTimeParameter.save();
		monitor.addParameter(usedTimeParameter);
		assertNull(CollectHelper.rate(USED_TIME_PARAMETER, currentValue, currentCollectTimeInMilliseconds, monitor));

		// parameterName is not null, currentValue is not null, currentCollectTimeInMilliseconds is not null,
		// monitor is not null, previousValue is not null, previousCollectTimeInMilliseconds is not null
		usedTimeParameter = NumberParam
			.builder()
			.name(USED_TIME_PARAMETER)
			.unit(TIME_PARAMETER_UNIT)
			.collectTime(currentCollectTimeInMilliseconds - 120000) // 2 minutes ago
			.rawValue(140.0)
			.value(140.0)
			.build();
		usedTimeParameter.save();
		monitor.addParameter(usedTimeParameter);
		assertEquals(0.5, CollectHelper.rate(USED_TIME_PARAMETER, currentValue, currentCollectTimeInMilliseconds, monitor));
	}

	@Test
	void testGetNumberParamValue() {
		Map<String, IParameter> parameters = new HashMap<>();
		NumberParam numberParam = NumberParam.builder().value(10.0).build();
		String parameterName = "parameter";
		parameters.put(parameterName, numberParam);
		Monitor monitor = Monitor.builder().parameters(parameters).build();
		assertNull(CollectHelper.getNumberParamValue(monitor, "wrongParameter"));
		assertEquals(10.0, CollectHelper.getNumberParamValue(monitor, parameterName));
	}

	@Test
	void testGetParamState() {
		Map<String, IParameter> parameters = new HashMap<>();
		DiscreteParam statusParam = DiscreteParam.builder().name("status").state(Status.OK).build();
		parameters.put("status", statusParam);
		Monitor monitor = Monitor.builder().parameters(parameters).build();
		assertNull(CollectHelper.getParameterState(monitor, "wrongStatus"));
		assertEquals(Status.OK, CollectHelper.getParameterState(monitor, "status"));
	}


	@Test
	void testUpdateNumberParameter() {
		{
			final Monitor monitor = Monitor.builder().build();
			CollectHelper.updateNumberParameter(
				monitor,
				ENERGY_USAGE_PARAMETER,
				ENERGY_USAGE_PARAMETER_UNIT,
				collectTime,
				100D,
				1500D
			);

			final NumberParam expected = NumberParam
					.builder()
					.name(ENERGY_USAGE_PARAMETER)
					.unit(ENERGY_USAGE_PARAMETER_UNIT)
					.collectTime(collectTime)
					.value(100D)
					.rawValue(1500D)
					.build();

			assertEquals(expected, monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class));
		}

		{
			final NumberParam previousParameter = NumberParam
					.builder()
					.name(ENERGY_USAGE_PARAMETER)
					.unit(ENERGY_USAGE_PARAMETER_UNIT)
					.collectTime(collectTime)
					.value(100D)
					.rawValue(1500D)
					.build();

			previousParameter.save();

			final Monitor monitor = Monitor.builder().parameters(new HashMap<>(
					Map.of(ENERGY_USAGE_PARAMETER, previousParameter)))
					.build();
			CollectHelper.updateNumberParameter(
				monitor,
				ENERGY_USAGE_PARAMETER,
				ENERGY_USAGE_PARAMETER_UNIT,
				collectTime + (2 * 60 * 1000),
				50D,
				1550D
			);

			final NumberParam expected = NumberParam
					.builder()
					.name(ENERGY_USAGE_PARAMETER)
					.unit(ENERGY_USAGE_PARAMETER_UNIT)
					.collectTime(collectTime + (2 * 60 * 1000))
					.value(50D)
					.rawValue(1550D)
					.build();
			expected.setPreviousCollectTime(collectTime);
			expected.setPreviousRawValue(1500D);

			assertEquals(expected, monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class));
		}
	}

	@Test
	void testUpdateDiscreteParameter() {
		{
			final Monitor monitor = Monitor.builder().build();
			CollectHelper.updateDiscreteParameter(monitor, STATUS_PARAMETER, collectTime, Status.OK);

			final DiscreteParam expected = DiscreteParam
					.builder()
					.name(STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(Status.OK)
					.build();

			assertEquals(expected, monitor.getParameter(STATUS_PARAMETER, DiscreteParam.class));
		}

		{
			final DiscreteParam previousParameter = DiscreteParam.builder()
					.name(STATUS_PARAMETER)
					.collectTime(collectTime - 120000)
					.state(Status.FAILED)
					.build();

			previousParameter.save();

			final Monitor monitor = Monitor.builder().parameters(new HashMap<>(
					Map.of(STATUS_PARAMETER, previousParameter)))
					.build();

			CollectHelper.updateDiscreteParameter(monitor, STATUS_PARAMETER, collectTime, Status.OK);

			final DiscreteParam expected = DiscreteParam
					.builder()
					.name(STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(Status.OK)
					.build();

			expected.setPreviousState(Status.FAILED);
			expected.setPreviousCollectTime(collectTime - 120000);

			assertEquals(expected, monitor.getParameter(STATUS_PARAMETER, DiscreteParam.class));
		}
	}

	@Test
	void testCollectPowerWithEnergyUsageFirstCollect() {

		final Monitor monitor = Monitor.builder()
				.monitorType(MonitorType.ENCLOSURE)
				.build();

		CollectHelper.collectPowerFromEnergyUsage(monitor, collectTime, 3138.358D, ECS1_01);

		assertNull(monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class).getValue());
		assertNull(monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertEquals(3138.358D, monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class).getRawValue());

	}

	@Test
	void testCollectPowerFromEnergyUsage() {

		final NumberParam energyUsage = NumberParam
				.builder()
				.name(ENERGY_USAGE_PARAMETER)
				.unit(ENERGY_USAGE_PARAMETER_UNIT)
				.collectTime(collectTime)
				.value(null)
				.rawValue(3138.358D) // kWatt-hours
				.build();
		energyUsage.save();

		final Monitor monitor = Monitor.builder().monitorType(MonitorType.ENCLOSURE).parameters(new HashMap<>(
				Map.of(ENERGY_USAGE_PARAMETER, energyUsage)))
				.build();

		CollectHelper.collectPowerFromEnergyUsage(monitor, collectTime + (2 * 60 * 1000), 3138.360, ECS1_01);

		Double joules = monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class).getValue();
		joules  = Math.round(joules * 100000D) / 100000D;

		assertEquals(7200, joules); // Joules (Energy)

		final double watts = Math.round(monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue());
		assertEquals(60.0, watts); // Watts (Power)

	}

	@Test
	void testCollectEnergyUsageFromPowerFirstCollect() {

		final Monitor monitor = Monitor.builder()
				.monitorType(MonitorType.ENCLOSURE)
				.build();

		CollectHelper.collectEnergyUsageFromPower(monitor, collectTime, 60D, ECS1_01);

		assertNull(monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class));
		assertEquals(60, monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue());
		assertEquals(60, monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getRawValue());
	}

	@Test
	void testCollectEnergyUsageFromPower() {

		final NumberParam powerConsumption = NumberParam
				.builder()
				.name(POWER_CONSUMPTION_PARAMETER)
				.unit(POWER_CONSUMPTION_PARAMETER_UNIT)
				.collectTime(collectTime)
				.value(null)
				.rawValue(60.0)
				.build();

		powerConsumption.save();

		final NumberParam energyUsage = NumberParam
			.builder()
			.name(ENERGY_USAGE_PARAMETER)
			.unit(ENERGY_USAGE_PARAMETER_UNIT)
			.collectTime(collectTime)
			.value(null)
			.rawValue(999.0)
			.build();

		energyUsage.save();

		final Monitor monitor = Monitor
			.builder()
			.monitorType(MonitorType.ENCLOSURE)
			.parameters(new HashMap<>(
				Map
					.of(POWER_CONSUMPTION_PARAMETER, powerConsumption,
						ENERGY_USAGE_PARAMETER, energyUsage)))
			.build();

		CollectHelper.collectEnergyUsageFromPower(monitor, collectTime + (2 * 60 * 1000), 64D, ECS1_01);

		assertEquals(64, monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()); // Watts
		assertEquals(64, monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getRawValue());

		assertEquals(7680.0, monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class).getValue()); // Joules
		assertEquals(7680.0, monitor.getParameter(ENERGY_PARAMETER, NumberParam.class).getValue()); // Joules
	}

	@Test
	void testCollectEnergyUsageFromPowerManyCollects() {

		// Collect 1
		final Monitor monitor = Monitor
			.builder()
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		CollectHelper.collectEnergyUsageFromPower(monitor, collectTime , 64D, ECS1_01);

		assertEquals(64, monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()); // Watts
		assertNull(monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class)); // Joules
		assertNull(monitor.getParameter(ENERGY_PARAMETER, NumberParam.class)); // Joules

		// Collect 2 (first collect time + 2 minutes)
		monitor.getParameters().values().forEach(IParameter::save);

		CollectHelper.collectEnergyUsageFromPower(monitor, collectTime + (2 * 60 * 1000), 60D, ECS1_01);

		assertEquals(60D, monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()); // Watts
		assertEquals(7200.0,monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class).getValue()); // Joules
		assertEquals(7200.0,monitor.getParameter(ENERGY_PARAMETER, NumberParam.class).getValue()); // Joules

		// Collect 3  (first collect time + 4 minutes)
		monitor.getParameters().values().forEach(IParameter::save);

		CollectHelper.collectEnergyUsageFromPower(monitor, collectTime + (4 * 60 * 1000), 64D, ECS1_01);

		assertEquals(64, monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()); // Watts
		assertEquals(7680.0,monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class).getValue()); // Joules
		assertEquals(14880.0,monitor.getParameter(ENERGY_PARAMETER, NumberParam.class).getValue()); // Joules
	}

}
