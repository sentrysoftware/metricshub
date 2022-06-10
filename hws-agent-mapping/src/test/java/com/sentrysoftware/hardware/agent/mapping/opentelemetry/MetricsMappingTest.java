package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;
import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.mapping.MappingHelper;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.AbstractIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

class MetricsMappingTest {

	private static final Map<MonitorType, String> MONITOR_TYPE_TO_HW_TYPE_ATTRIBUTE_VALUE;

	static {
		final Map<MonitorType, String> map = new HashMap<>();
		map.put(MonitorType.BATTERY, BatteryMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.BLADE, BladeMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.CPU_CORE, CpuCoreMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.CPU, CpuMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.DISK_CONTROLLER, DiskControllerMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.ENCLOSURE, EnclosureMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.FAN, FanMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.GPU, GpuMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.LED, LedMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.LOGICAL_DISK, LogicalDiskMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.LUN, LunMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.MEMORY, MemoryMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.NETWORK_CARD, NetworkCardMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.OTHER_DEVICE, OtherDeviceMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.PHYSICAL_DISK, PhysicalDiskMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.POWER_SUPPLY, PowerSupplyMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.ROBOTICS, RoboticsMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.TAPE_DRIVE, TapeDriveMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.TEMPERATURE, TemperatureMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.VM, VmMapping.HW_TYPE_ATTRIBUTE_VALUE);
		map.put(MonitorType.VOLTAGE, VoltageMapping.HW_TYPE_ATTRIBUTE_VALUE);
		MONITOR_TYPE_TO_HW_TYPE_ATTRIBUTE_VALUE = Collections.unmodifiableMap(map);
	}

	@Test
	void testGetAttributesMap() {
		MonitorType.MONITOR_TYPES.forEach(monitorType -> assertNotNull(MetricsMapping.getAttributesMap(monitorType)));
		// Null attributes that are already defined as metrics.
		assertNull(MetricsMapping.getAttributesMap(MonitorType.PHYSICAL_DISK).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.LOGICAL_DISK).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.MEMORY).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.PHYSICAL_DISK).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.GPU).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.CPU)
				.get(MappingHelper.camelCaseToSnakeCase(MAXIMUM_SPEED)));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.POWER_SUPPLY)
				.get(MappingHelper.camelCaseToSnakeCase(POWER_SUPPLY_POWER)));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.LUN)
				.get(MappingHelper.camelCaseToSnakeCase(EXPECTED_PATH_COUNT)));

		assertEquals(HardwareConstants.HOSTNAME, MetricsMapping.getAttributesMap(MonitorType.VM)
				.get(VmMapping.VM_HOST_NAME_ATTRIBUTE_KEY));

		MonitorType
			.MONITOR_TYPES
			.stream()
			.filter(monitorType -> MonitorType.HOST != monitorType && MonitorType.CONNECTOR != monitorType)
			.forEach(monitorType -> 
				assertEquals(
					HardwareConstants.IDENTIFYING_INFORMATION,
					MetricsMapping.getAttributesMap(monitorType).get(MappingConstants.INFO_ATTRIBUTE_KEY),
					String.format(
						"The %s metadata name is not overridden for monitor type %s.",
						HardwareConstants.IDENTIFYING_INFORMATION,
						monitorType
					)
				)
			);

		assertEquals(HardwareConstants.VOLTAGE_TYPE, MetricsMapping.getAttributesMap(MonitorType.VOLTAGE)
				.get(MappingConstants.SENSOR_LOCATION_ATTRIBUTE_KEY));
		assertEquals(HardwareConstants.TEMPERATURE_TYPE, MetricsMapping.getAttributesMap(MonitorType.TEMPERATURE)
				.get(MappingConstants.SENSOR_LOCATION_ATTRIBUTE_KEY));
		assertEquals(HardwareConstants.FAN_TYPE, MetricsMapping.getAttributesMap(MonitorType.FAN)
				.get(MappingConstants.SENSOR_LOCATION_ATTRIBUTE_KEY));
		assertEquals(HardwareConstants.COMPILED_FILE_NAME, MetricsMapping.getAttributesMap(MonitorType.CONNECTOR)
				.get(ConnectorMapping.CONNECTOR_ID_ATTRIBUTE_KEY));

	}

	@Test
	void testGetMetricInfo() {

		final Map<String, Set<String>> metricDescriptions = new HashMap<>();
		for (MonitorType monitorType : MonitorType.MONITOR_TYPES) {

			assertMetrics(metricDescriptions, monitorType, MetricsMapping.getMatrixParamToMetricMap().get(monitorType));
			assertMetrics(metricDescriptions, monitorType, MetricsMapping.getMatrixMetadataToMetricMap().get(monitorType));
		}

		// Check metric descriptions
		for (Entry<String, Set<String>> metricEntry : metricDescriptions.entrySet()) {
			assertEquals(
				1,
				metricEntry.getValue().size(),
				String.format(
					"The metric %s is incorrectly mapped. This metric is mapped using %s descriptions: \n%s"
							+ ".\nOnly one description must be defined.",
					metricEntry.getKey(),
					metricEntry.getValue().size(),
					metricEntry.getValue().stream().collect(Collectors.joining("\n"))
				)
			);
		}

		final Optional<List<MetricInfo>> batteryChargeInfo = MetricsMapping.getMetricInfoList(MonitorType.BATTERY, "charge");

		assertTrue(batteryChargeInfo.isPresent());
		assertTrue(MetricsMapping.getMetricInfoList(MonitorType.BATTERY, "undefinedMatrixParamName").isEmpty());
		assertTrue(MetricsMapping.getMetricInfoList(MonitorType.BATTERY, "").isEmpty());

	}

	/**
	 * Check the given collection of matrixKeyToMetricInfo
	 * 
	 * @param metricDescriptions
	 * @param monitorType
	 * @param matrixKeyToMetricInfo
	 */
	void assertMetrics(Map<String, Set<String>> metricDescriptions, MonitorType monitorType, final Map<String, List<MetricInfo>> matrixKeyToMetricInfo) {
		final Set<String> metricIds = new HashSet<>();
		if (matrixKeyToMetricInfo != null) {
			for (Entry<String, List<MetricInfo>> entry : matrixKeyToMetricInfo.entrySet())  {
				for (MetricInfo metricInfo : entry.getValue()) {
					assertNotNull(metricInfo);
					String metricId;
					List<AbstractIdentifyingAttribute> identifyingAttributesList = metricInfo
						.getIdentifyingAttributes();
					if (identifyingAttributesList != null) {
						for (AbstractIdentifyingAttribute identifyingAttribute : identifyingAttributesList) {
							assertNotNull(identifyingAttribute.getKey());
							assertNotNull(identifyingAttribute.getValue());
						}
						final String identifyingAttributes = identifyingAttributesList
							.stream()
							.map(id -> String.format("%s.%s", id.getKey(), id.getValue()))
							.collect(Collectors.joining("."));

						metricId = String.format("%s.%s", metricInfo.getName(), identifyingAttributes);
					} else {
						metricId = metricInfo.getName();
					}

					// TODO: resolve conflict with 2 statuses for controller in HW Sentry KM
					if (metricInfo.getAdditionalId() != null) {
						metricId = String.format("%s.%s", metricId, metricInfo.getAdditionalId());
					}

					// Check if the metric is not defined multiple times
					assertFalse(
						metricIds.contains(metricId),
						String.format(
							"This metric (%s) is incorrectly mapped for monitor type %s. Metric id: %s."
									+ "\nIf no identifying attributes are defined for this metric, the metric name must be unique."
									+ "\nIf this metric is mapped with identifying attributes, the identifying attributes must be unique.",
							metricInfo.getName(),
							monitorType,
							metricId
						)
					);

					metricIds.add(metricId);

					metricDescriptions.computeIfAbsent(metricInfo.getName(), descriptions -> new HashSet<>());
					metricDescriptions.get(metricInfo.getName()).add(metricInfo.getDescription());

					// Assert the UpDownCounter metrics
					if (metricInfo.getName().endsWith("status")
							|| metricInfo.getPredicate() != null
							|| metricInfo.getName().equals(LogicalDiskMapping.USAGE_METRIC_NAME)) {
						assertEquals(
							MetricType.UP_DOWN_COUNTER,
							metricInfo.getType(),
							String.format("This metric (%s) must be an UpDownCounter.", metricInfo.getName())
						);
					}

					// Check status metrics
					assertFalse(
						metricInfo.getName().matches("^hw\\.\\w+\\.status$"),
						String.format(
							"This metric (%s) is not accepted. Metric should be named hw.status",
							metricInfo.getName()
						)
					);

					// Check energy and power metrics
					if (MonitorType.HOST != monitorType && MonitorType.ENCLOSURE != monitorType) {
						if (metricInfo.getName().endsWith("energy")) {
							assertEquals(
								"hw.energy", metricInfo.getName(),
								String.format(
									"This metric (%s) is not accepted. Metric must be named hw.energy.",
									metricInfo.getName()
								)
							);
							assertMetricWithHwType(monitorType, identifyingAttributesList, "hw.energy");
						}

						if (metricInfo.getName().endsWith("power")) {
							assertEquals(
								"hw.power", metricInfo.getName(),
								String.format(
									"This metric (%s) is not accepted. Metric must be named hw.power.",
									metricInfo.getName()
								)
							);
							assertMetricWithHwType(monitorType, identifyingAttributesList, "hw.power");
						}
					}

					if ("hw.status".equals(metricInfo.getName())) {
						assertMetricWithHwType(monitorType, identifyingAttributesList, "hw.status");
					}
				}
			}
		}
	}

	/**
	 * Verify the given metric having the hw.type attribute
	 * 
	 * @param monitorType
	 * @param identifyingAttributesList
	 * @param metricName
	 */
	private void assertMetricWithHwType(final MonitorType monitorType,
			final List<AbstractIdentifyingAttribute> identifyingAttributesList, final String metricName) {

		final List<AbstractIdentifyingAttribute> hwTypeAttributeList = identifyingAttributesList
				.stream()
				.filter(id -> HW_TYPE_ATTRIBUTE_KEY.equals(id.getKey()))
				.collect(Collectors.toList());

		assertEquals(
			1,
			hwTypeAttributeList.size(),
			String.format(
				"The identifying attribute %s on metric %s must be defined only once. Monitor type %s.",
				HW_TYPE_ATTRIBUTE_KEY,
				metricName,
				monitorType
			)
		);

		assertNotNull(
			hwTypeAttributeList.get(0).getValue(),
			String.format(
				"The identifying attribute %s value on metric %s is not defined. Monitor type %s.",
				HW_TYPE_ATTRIBUTE_KEY,
				metricName,
				monitorType
			)
		);

		assertEquals(
			MONITOR_TYPE_TO_HW_TYPE_ATTRIBUTE_VALUE.get(monitorType),
			hwTypeAttributeList.get(0).getValue(),
			String.format(
				"Wrong identifying attribute %s value on metric %s. Monitor type %s.",
				HW_TYPE_ATTRIBUTE_KEY,
				metricName,
				monitorType
			)
		);
	}

	@Test
	void testGetMetadataAsMetricInfo() {

		assertTrue(MetricsMapping.getMetadataAsMetricInfoList(MonitorType.PHYSICAL_DISK, SIZE).isPresent());
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfoList(MonitorType.MEMORY, "undefinedMatrixMetadataName"));
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfoList(MonitorType.MEMORY, ""));
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfoList(MonitorType.MEMORY, null));
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfoList(MonitorType.ENCLOSURE, ""));

	}

}
