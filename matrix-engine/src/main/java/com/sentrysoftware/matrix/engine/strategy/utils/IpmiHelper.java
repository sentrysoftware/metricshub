package com.sentrysoftware.matrix.engine.strategy.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.helpers.ArrayHelper;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.ListHelper;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;

import lombok.NonNull;

public class IpmiHelper {

	private IpmiHelper() {}

	/**
	 * Process what we got from the IPMI WMI provider and return a pretty table.
	 * @param wmiComputerSystem
	 * @param wmiNumericSensors
	 * @param wmiDiscreteSensors
	 * @return
	 */
	public static List<List<String>> ipmiTranslateFromWmi(@NonNull List<List<String>> wmiComputerSystem,
			@NonNull List<List<String>> wmiNumericSensors,
			@NonNull List<List<String>> wmiDiscreteSensors) {
		List<List<String>> ipmiTable = new ArrayList<>();

		// Process compute system data
		List<String> wmiComputerSystemTranslated = translateWmiComputerSystem(wmiComputerSystem);
		if (wmiComputerSystemTranslated != null) {
			ipmiTable.add(wmiComputerSystemTranslated);
		}

		// Process numeric sensors
		List<List<String>> wmiNumericSensorsTranslated = translateWmiNumericSensors(wmiNumericSensors);
		if (!wmiNumericSensorsTranslated.isEmpty()) {
			ipmiTable.addAll(wmiNumericSensorsTranslated);
		}

		// Process discrete sensors
		Collection<List<String>> wmiDiscreteSensorsTranslated = translateWmiDiscreteSensors(wmiDiscreteSensors);
		if (!wmiDiscreteSensorsTranslated.isEmpty()) {
			ipmiTable.addAll(wmiDiscreteSensorsTranslated);
		}

		return ipmiTable;
	}

	/**
	 * Process the wmiComputerSystem request result into a pretty table.
	 * @param wmiComputerSystem
	 * @return
	 */
	private static List<String> translateWmiComputerSystem(final List<List<String>> wmiComputerSystem) {
		if (!wmiComputerSystem.isEmpty()) {
			List<String> wmiComputerSystemLine = wmiComputerSystem.get(0);

			if (wmiComputerSystemLine.size() > 2) {
				return Arrays.asList(
						"FRU",
						wmiComputerSystemLine.get(2),
						wmiComputerSystemLine.get(1),
						wmiComputerSystemLine.get(0));
			}
		}
		return null;
	}

	/**
	 * Process the wmiNumericSensors request result into a pretty table.
	 * @param wmiNumericSensors
	 * @return
	 */
	private static List<List<String>> translateWmiNumericSensors(final List<List<String>> wmiNumericSensors) {
		List<List<String>> result = new ArrayList<>();

		// BaseUnits,CurrentReading,Description,LowerThresholdCritical,LowerThresholdNonCritical,SensorType,UnitModifier,UpperThresholdCritical,UpperThresholdNonCritical
		for (List<String> line : wmiNumericSensors) {
			// Process the 'Description' field which contains everything about how to identify the sensor
			String description = ListHelper.getValueAtIndex(line, 2, HardwareConstants.EMPTY);
			if (description.isEmpty()) {
				continue;
			}

			String[] sensorSplit = description.split("\\(");
			String sensorId = ArrayHelper.getValueAtIndex(sensorSplit, 1, HardwareConstants.EMPTY).split("\\)")[0];
			String sensorName = sensorSplit[0];
			description = ArrayHelper.getValueAtIndex(description.split(":"), 1, HardwareConstants.EMPTY);

			String deviceId;

			int lookupIndex = description.indexOf(" for ");
			if (lookupIndex != -1) {
				deviceId = description.substring(lookupIndex + 5);
			} else {
				deviceId = HardwareConstants.EMPTY;
			}

			String baseUnit = ListHelper.getValueAtIndex(line, 0, "0");
			int unitModifier;

			unitModifier = NumberHelper.parseInt(ListHelper.getValueAtIndex(line, 6, "0"), 0);

			String sensorType = ListHelper.getValueAtIndex(line, 5, "0");

			double currentValue = NumberHelper.parseDouble(ListHelper.getValueAtIndex(line, 1, "0D"), 0D);

			// Temperature
			// Check the unit (must be Celsius degrees)
			// 2 -> Celsius, 3 -> Farenheit, 4 -> Kelvin
			if (sensorType.equals("2") && currentValue != 0 && (baseUnit.equals("2") || baseUnit.equals("3") || baseUnit.equals("4"))) {
				List<String> temperatureList = temperatureRow(unitModifier, currentValue, baseUnit, line);
				result.add(
						Arrays.asList(
								"Temperature",
								sensorId,
								sensorName,
								deviceId,
								temperatureList.get(0),
								temperatureList.get(1),
								temperatureList.get(2)));
			} else if (sensorType.equals("5") && baseUnit.equals("19") && currentValue != 0) { // Fans
				List<String> fanList = fanRow(unitModifier, currentValue, line);

				result.add(
						Arrays.asList(
								"Fan",
								sensorId,
								sensorName,
								deviceId,
								fanList.get(0),
								fanList.get(1),
								fanList.get(2)));
			} else if (sensorType.equals("3") && baseUnit.equals("5") && currentValue != 0) { // Voltage
				List<String> voltageList = voltageRow(unitModifier, currentValue, line);

				// Add the sensor to the table
				result.add(
						Arrays.asList(
								"Voltage",
								sensorId,
								sensorName,
								deviceId,
								voltageList.get(0),
								voltageList.get(1),
								voltageList.get(2)));
			} else if (sensorType.equals("4") && baseUnit.equals("6") && currentValue != 0) { // Current
				// Convert values based on unitModifier
				currentValue = currentValue * Math.pow(10, unitModifier);
				// Add the sensor to the table
				result.add(
						Arrays.asList(
								"Current",
								sensorId,
								sensorName,
								deviceId,
								String.valueOf(currentValue)));
			} else if (sensorType.equals("1") && (baseUnit.equals("7") || baseUnit.equals("8") && currentValue != 0)) { // Power consumption.

				// Depending on the unit, convert it to watts or not
				if (baseUnit.equals("7")) {
					List<String> powerList = powerRow(unitModifier, currentValue, line);
					result.add(
							Arrays.asList(
									"PowerConsumption",
									sensorId,
									sensorName,
									deviceId,
									powerList.get(0),
									powerList.get(1),
									powerList.get(2)));
				} else {
					result.add(
							Arrays.asList(
									"EnergyUsage",
									sensorId,
									sensorName,
									deviceId,
									energyRow(unitModifier, currentValue)));
				}
			}
		}

		return result;
	}

	/**
	 * Process the wmiDiscreteSensors request result into a pretty table.
	 * @param wmiDiscreteSensors
	 * @return
	 */
	private static Collection<List<String>> translateWmiDiscreteSensors(final List<List<String>> wmiDiscreteSensors) {

		Map<String, List<String>> deviceMap = new HashMap<>();

		for (List<String> line : wmiDiscreteSensors) {
			String description = ListHelper.getValueAtIndex(line, 1, HardwareConstants.EMPTY);
			if (description.isEmpty()) {
				continue;
			}

			String sensorName = description.split("\\(")[0];
			description = ArrayHelper.getValueAtIndex(description.split(":"), 1, HardwareConstants.EMPTY);

			String entityId;

			int lookupIndex = description.indexOf(" for ");
			if (lookupIndex == -1) {
				continue;
			}

			entityId = description.substring(lookupIndex + 5);
			lookupIndex = entityId.lastIndexOf(HardwareConstants.WHITE_SPACE);
			if (lookupIndex == -1) {
				continue;
			}

			String deviceType = entityId.substring(0, lookupIndex);
			String deviceId = entityId.substring(lookupIndex + 1);

			String state = ListHelper.getValueAtIndex(line, 0, "0.0");
			if (state == null || state.isEmpty() || "N/A".equals(state)) {
				continue;
			}

			if (state.length() > 18 && state.startsWith("OEM State,Value=")) {
				// Reverse the bytes of the WORD value
				state = "0x" + state.substring(18, 20) + state.substring(16, 18);
			}

			state = state.replace(HardwareConstants.DOT + HardwareConstants.COMMA, HardwareConstants.PIPE + sensorName + HardwareConstants.EQUAL);

			// Get the line of this device
			List<String> deviceLine = deviceMap.get(entityId);
			if(deviceLine != null) {
				if (deviceLine.size() < 7) {
					continue;
				}

				// Add the list of states of this sensor to the device
				deviceLine.set(6, deviceLine.get(6) 
						+ HardwareConstants.PIPE
						+ sensorName
						+ HardwareConstants.EQUAL
						+ state);

				// Re-add that to the device list
				deviceMap.put(entityId, deviceLine);
			} else {
				// This is the first time we find this device ID
				List<String> deviceSensorList = Arrays.asList(
						deviceType,
						deviceId,
						entityId,
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						sensorName 
						+ HardwareConstants.EQUAL 
						+ state);
				deviceMap.put(entityId, deviceSensorList);
			}
		}

		final List<List<String>> devices = deviceMap.values().stream()
				.filter(line -> line.size() >= 7 && !ListHelper.getValueAtIndex(line, 6, "").contains("=Device Removed/Device Absent"))
				.map(line ->
				{
					line.set(6, line.get(6)
							.replace("=State Asserted", "=1")
							.replace("=State Deasserted", "=0")
							.replace("=Deasserted", "=0"));
					return line;
				})
				.collect(Collectors.toList());

		return devices;
	}

	/**
	 * Convert from Fahrenheit to Celsius, rounded to two decimals.
	 * @param fahrenheit
	 * @return
	 */
	public static double convertFromFahrenheitToCelsius(double fahrenheit) {
		return Math.round((fahrenheit - 32) * 55.56) / 100D;
	}

	/**
	 * Convert from Kelvin to Celsius.
	 * @param kelvin
	 * @return
	 */
	public static double convertFromKelvinToCelsius(double kelvin) {
		return kelvin - 273.15;
	}

	/**
	 * Calculate the current value and thresholds for the temperature row.
	 * @param unitModifier
	 * @param currentValue
	 * @param baseUnit
	 * @param line
	 * @return
	 */
	private static List<String> temperatureRow(final int unitModifier, double currentValue, final String baseUnit, final List<String> line) {
		// Convert values based on unitModifier
		currentValue = currentValue * Math.pow(10, unitModifier);

		String threshold1 = ListHelper.getValueAtIndex(line, 8, "0.0");
		String threshold2 = ListHelper.getValueAtIndex(line, 7, "0.0");

		if (baseUnit.equals("4")) {
			currentValue = convertFromKelvinToCelsius(currentValue);
		} else if (baseUnit.equals("3")) {
			currentValue = convertFromFahrenheitToCelsius(currentValue);
		}

		Double threshold1Double = NumberHelper.parseDouble(threshold1, null);

		if (threshold1Double != null) {
			threshold1Double = threshold1Double * Math.pow(10, unitModifier);
			if (baseUnit.equals("4")) {
				threshold1Double = convertFromKelvinToCelsius(threshold1Double);
			} else if (baseUnit.equals("3")) {
				threshold1Double = convertFromFahrenheitToCelsius(threshold1Double);
			}

			threshold1 = String.valueOf(threshold1Double);
		} else {
			threshold1 = HardwareConstants.EMPTY;
		}

		Double threshold2Double = NumberHelper.parseDouble(threshold2, null);

		if (threshold2Double != null) {
			threshold2Double = threshold2Double * Math.pow(10, unitModifier);

			if (baseUnit.equals("4")) {
				threshold2Double = convertFromKelvinToCelsius(threshold2Double);
			} else if (baseUnit.equals("3")) {
				threshold2Double = convertFromFahrenheitToCelsius(threshold2Double);
			}

			threshold2 = String.valueOf(threshold2Double);
		} else {
			threshold2 = HardwareConstants.EMPTY;
		}

		return Arrays.asList(
				String.valueOf(currentValue),
				threshold1,
				threshold2);
	}

	/**
	 * Calculate the current value and thresholds for the fan row.
	 * @param unitModifier
	 * @param currentValue
	 * @param line
	 * @return
	 */
	private static List<String> fanRow(final int unitModifier, double currentValue, final List<String> line) {
		String threshold1 = ListHelper.getValueAtIndex(line, 4, "0.0");
		String threshold2 = ListHelper.getValueAtIndex(line, 3, "0.0");

		// Convert values based on unitModifier
		currentValue = currentValue * Math.pow(10, unitModifier);

		Double threshold1Double = NumberHelper.parseDouble(threshold1, null);
		if (threshold1Double != null) {
			threshold1 = String.valueOf(threshold1Double * Math.pow(10, unitModifier)); 
		} else { 
			threshold1 = HardwareConstants.EMPTY; 
		}

		Double threshold2Double = NumberHelper.parseDouble(threshold2, null);
		if (threshold2Double != null) {
			threshold2 = String.valueOf(threshold2Double * Math.pow(10, unitModifier)); 
		} else { 
			threshold2 = HardwareConstants.EMPTY; 
		}

		return Arrays.asList(
				String.valueOf(currentValue),
				threshold1,
				threshold2);
	}

	/**
	 * Calculate the current value and thresholds for the voltage row.
	 * @param unitModifier
	 * @param currentValue
	 * @param line
	 * @return
	 */
	private static List<String> voltageRow(final int unitModifier, double currentValue, final List<String> line) {
		// Convert values based on unitModifier and then from Volts to milliVolts
		currentValue = currentValue * Math.pow(10, unitModifier) * 1000;

		String threshold1 = ListHelper.getValueAtIndex(line, 4, "0.0");
		Double threshold1Double = NumberHelper.parseDouble(threshold1, null);

		if (threshold1Double == null) {
			threshold1 = ListHelper.getValueAtIndex(line, 3, "0.0");
			threshold1Double = NumberHelper.parseDouble(threshold1, null);
		}

		if (threshold1Double != null) {
			threshold1Double = threshold1Double * Math.pow(10, unitModifier) * 1000;
			threshold1 = String.valueOf(threshold1Double);
		} else {
			threshold1 = HardwareConstants.EMPTY;
		}

		String threshold2 = ListHelper.getValueAtIndex(line, 8, "0.0");
		Double threshold2Double = NumberHelper.parseDouble(threshold2, null);

		if (threshold2Double == null) {
			threshold2 = ListHelper.getValueAtIndex(line, 7, "0.0");
			threshold2Double = NumberHelper.parseDouble(threshold2, null);
		}

		if (threshold2Double != null) {
			threshold2Double = threshold2Double * Math.pow(10, unitModifier) * 1000;
			threshold2 = String.valueOf(threshold2Double);
		} else {
			threshold2 = HardwareConstants.EMPTY;
		}

		return Arrays.asList(
				String.valueOf(currentValue),
				threshold1,
				threshold2);
	}

	/**
	 * Calculate the current value and thresholds for the power row.
	 * @param unitModifier
	 * @param currentValue
	 * @param line
	 * @return
	 */
	private static List<String> powerRow(final int unitModifier, double currentValue, final List<String> line) {
		String threshold1 = ListHelper.getValueAtIndex(line, 8, "0.0");
		String threshold2 = ListHelper.getValueAtIndex(line, 7, "0.0");

		Double threshold1Double = NumberHelper.parseDouble(threshold1, null);

		if (threshold1Double != null) {
			threshold1Double = threshold1Double * Math.pow(10, unitModifier);
			threshold1 = String.valueOf(threshold1Double);
		} else {
			threshold1 = HardwareConstants.EMPTY;
		}

		Double threshold2Double = NumberHelper.parseDouble(threshold2, null);

		if (threshold2Double != null) {
			threshold2Double = threshold2Double * Math.pow(10, unitModifier);
			threshold2 = String.valueOf(threshold2Double);
		} else {
			threshold2 = HardwareConstants.EMPTY;
		}

		// Convert values based on unitModifier
		currentValue = currentValue * Math.pow(10, unitModifier);

		return Arrays.asList(
				String.valueOf(currentValue),
				threshold1,
				threshold2);
	}

	/**
	 * Calculate the current value for the energy row.
	 * @param unitModifier
	 * @param currentValue
	 * @return
	 */
	private static String energyRow(final int unitModifier, double currentValue) {
		// Convert values based on unitModifier
		// Joule conversion.
		return String.valueOf(currentValue * Math.pow(10, unitModifier) / 3600000);
	}
}
