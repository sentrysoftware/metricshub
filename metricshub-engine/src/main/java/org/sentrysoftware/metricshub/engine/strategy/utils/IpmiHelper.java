package org.sentrysoftware.metricshub.engine.strategy.utils;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.TABLE_SEP;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.WHITE_SPACE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.common.helpers.ArrayHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.ListHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.NumberHelper;

/**
 * Utility class for IPMI (Intelligent Platform Management Interface) operations and data processing.
 * This class provides methods for cleaning, processing, and translating IPMI-related data.
 */
public class IpmiHelper {

	private static final Pattern PATTERN_BMC_REQ = Pattern.compile("(?m)^(BMC req|--).*");

	private static final String SENSOR_ID_REGEX = "^Sensor ID.*";

	private static final String BOARD2 = " Board ";

	private static final String OEM_SPECIFIC = "OEM Specific";

	private static final String POOR_LIST = "poorList";

	private static final String GOOD_LIST = "goodList";

	private static final String EQUALS_0 = "=0";

	private static final String EQUALS_1 = "=1";

	private static final String DEASSERTED = "=Deasserted";

	private static final String ASSERTED = "=Asserted";

	private static final String STATE_DEASSERTED = "=State Deasserted";

	private static final String STATE_ASSERTED = "=State Asserted";

	private static final String DEVICE_ABSENT = "=Device Absent";

	private static final String FRU_LIST = "fruList";

	private static final String NOT_READING = "Not Reading";

	private static final String STATES_ASSERTED = "States Asserted";

	private static final String SENSOR_ID = "Sensor ID ";

	private static final Pattern NUM_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

	private static final Pattern PATTERN_BTW_BRACKETS = Pattern.compile("\\((.*?)\\)");
	private static final Pattern PATTERN_SENSORID = Pattern.compile(SENSOR_ID_REGEX, Pattern.MULTILINE);
	private static final Pattern PATTERN_ENTITYID = Pattern.compile("^ *Entity ID.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_SENSOR_READING = Pattern.compile("^ *Sensor Reading.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_THRESHOLD_UPPER_NON_CRITICAL = Pattern.compile(
		".*Upper non-critical.*",
		Pattern.MULTILINE
	);
	private static final Pattern PATTERN_THRESHOLD_UPPER_CRITICAL = Pattern.compile(
		".*Upper critical.*",
		Pattern.MULTILINE
	);
	private static final Pattern PATTERN_THRESHOLD_UPPER_NON_RECOVERABLE = Pattern.compile(
		".*Upper non-recoverable.*",
		Pattern.MULTILINE
	);
	private static final Pattern PATTERN_THRESHOLD_LOWER_NON_CRITICAL = Pattern.compile(
		".*Lower non-critical.*",
		Pattern.MULTILINE
	);
	private static final Pattern PATTERN_THRESHOLD_LOWER_CRITICAL = Pattern.compile(
		".*Lower critical.*",
		Pattern.MULTILINE
	);
	private static final Pattern PATTERN_THRESHOLD_LOWER_NON_RECOVERABLE = Pattern.compile(
		".*Lower non-recoverable.*",
		Pattern.MULTILINE
	);
	private static final Pattern PATTERN_OEM_SPECIFIC = Pattern.compile(
		"States Asserted +: 0x[0-9a-zA-Z]+ +OEM Specific",
		Pattern.MULTILINE
	);

	private static final Pattern PATTERN_FRUID = Pattern.compile("^FRU Device Description.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_VENDOR = Pattern.compile(" Product Manufacturer.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_MODEL = Pattern.compile(" Product Name.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_SERIAL = Pattern.compile(" Product Serial.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_BORD_VENDOR = Pattern.compile(" Board Mfg +:.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_BORD_MODEL = Pattern.compile(" Board Product.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_BORD_SERIAL = Pattern.compile(" Board Serial.*", Pattern.MULTILINE);

	private IpmiHelper() {}

	/**
	 * Process what we got from the IPMI WMI provider and return a pretty table.
	 *
	 * @param wmiComputerSystem  The WMI computer system result table
	 * @param wmiNumericSensors  The WMI numeric sensors result table
	 * @param wmiDiscreteSensors The WMI discrete sensors result table
	 * @return List of List (Table)
	 */
	public static List<List<String>> ipmiTranslateFromWmi(
		@NonNull List<List<String>> wmiComputerSystem,
		@NonNull List<List<String>> wmiNumericSensors,
		@NonNull List<List<String>> wmiDiscreteSensors
	) {
		List<List<String>> ipmiTable = new ArrayList<>();

		// Process compute system data
		List<String> wmiComputerSystemTranslated = translateWmiComputerSystem(wmiComputerSystem);
		if (!wmiComputerSystemTranslated.isEmpty()) {
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
	 *
	 * @param wmiComputerSystem The WMI computer system result table
	 * @return Single row as a {@link List} for the computer system
	 */
	private static List<String> translateWmiComputerSystem(final List<List<String>> wmiComputerSystem) {
		if (!wmiComputerSystem.isEmpty()) {
			List<String> wmiComputerSystemLine = wmiComputerSystem.get(0);

			if (wmiComputerSystemLine.size() > 2) {
				return Arrays.asList(
					"FRU",
					wmiComputerSystemLine.get(2),
					wmiComputerSystemLine.get(1),
					wmiComputerSystemLine.get(0)
				);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Process the wmiNumericSensors request result into a pretty table.
	 *
	 * @param wmiNumericSensors  The WMI numeric sensors result table
	 * @return List of List (Table)
	 */
	private static List<List<String>> translateWmiNumericSensors(final List<List<String>> wmiNumericSensors) {
		List<List<String>> result = new ArrayList<>();

		// BaseUnits,CurrentReading,Description,LowerThresholdCritical,LowerThresholdNonCritical,SensorType,UnitModifier,UpperThresholdCritical,UpperThresholdNonCritical
		for (List<String> line : wmiNumericSensors) {
			// Process the 'Description' field which contains everything about how to identify the sensor
			String description = ListHelper.getValueAtIndex(line, 2, "");
			if (description.isEmpty()) {
				continue;
			}

			String[] sensorSplit = description.split("\\(");
			String sensorId = ArrayHelper.getValueAtIndex(sensorSplit, 1, "").split("\\)")[0];
			String sensorName = sensorSplit[0];
			description = ArrayHelper.getValueAtIndex(description.split(":"), 1, "");

			String deviceId;

			int lookupIndex = description.indexOf(" for ");
			if (lookupIndex != -1) {
				deviceId = description.substring(lookupIndex + 5);
			} else {
				deviceId = "";
			}

			String baseUnit = ListHelper.getValueAtIndex(line, 0, "0");
			int unitModifier;

			unitModifier = NumberHelper.parseInt(ListHelper.getValueAtIndex(line, 6, "0"), 0);

			String sensorType = ListHelper.getValueAtIndex(line, 5, "0");

			double currentValue = NumberHelper.parseDouble(ListHelper.getValueAtIndex(line, 1, "0D"), 0D);

			// Temperature
			// Check the unit (must be Celsius degrees)
			// 2 -> Celsius, 3 -> Farenheit, 4 -> Kelvin
			if (sensorType.equals("2") && currentValue != 0 && List.of("2", "3", "4").contains(baseUnit)) {
				List<String> temperatureList = temperatureRow(unitModifier, currentValue, baseUnit, line);
				result.add(
					Arrays.asList(
						"Temperature",
						sensorId,
						sensorName,
						deviceId,
						temperatureList.get(0),
						temperatureList.get(1),
						temperatureList.get(2)
					)
				);
			} else if (sensorType.equals("5") && baseUnit.equals("19") && currentValue != 0) { // Fans
				List<String> fanList = fanRow(unitModifier, currentValue, line);

				result.add(
					Arrays.asList("Fan", sensorId, sensorName, deviceId, fanList.get(0), fanList.get(1), fanList.get(2))
				);
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
						voltageList.get(2)
					)
				);
			} else if (sensorType.equals("4") && baseUnit.equals("6") && currentValue != 0) { // Current
				// Convert values based on unitModifier
				currentValue = currentValue * Math.pow(10, unitModifier);
				// Add the sensor to the table
				result.add(Arrays.asList("Current", sensorId, sensorName, deviceId, String.valueOf(currentValue)));
			} else if (sensorType.equals("1") && (baseUnit.equals("7") || (baseUnit.equals("8") && currentValue != 0))) { // Power consumption.
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
							powerList.get(2)
						)
					);
				} else {
					result.add(
						Arrays.asList("EnergyUsage", sensorId, sensorName, deviceId, energyRow(unitModifier, currentValue))
					);
				}
			}
		}

		return result;
	}

	/**
	 * Process the wmiDiscreteSensors request result into a pretty table.
	 *
	 * @param wmiDiscreteSensors The WMI discrete sensors result table
	 * @return List of List (Table)
	 */
	private static List<List<String>> translateWmiDiscreteSensors(final List<List<String>> wmiDiscreteSensors) {
		Map<String, List<String>> deviceMap = new HashMap<>();

		for (List<String> line : wmiDiscreteSensors) {
			String description = ListHelper.getValueAtIndex(line, 1, "");
			if (description.isEmpty()) {
				continue;
			}

			String sensorName = description.split("\\(")[0];
			description = ArrayHelper.getValueAtIndex(description.split(":"), 1, "");

			String entityId;

			int lookupIndex = description.indexOf(" for ");
			if (lookupIndex == -1) {
				continue;
			}

			entityId = description.substring(lookupIndex + 5);
			lookupIndex = entityId.lastIndexOf(WHITE_SPACE);
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

			state = state.replace(".,", "|" + sensorName + "=");

			// Get the line of this device
			List<String> deviceLine = deviceMap.get(entityId);
			if (deviceLine != null) {
				if (deviceLine.size() < 7) {
					continue;
				}

				// Add the list of states of this sensor to the device
				deviceLine.set(6, String.format("%s|%s=%s", deviceLine.get(6), sensorName, state));

				// Re-add that to the device list
				deviceMap.put(entityId, deviceLine);
			} else {
				// This is the first time we find this device ID
				List<String> deviceSensorList = Arrays.asList(
					deviceType,
					deviceId,
					entityId,
					"",
					"",
					"",
					String.format("%s=%s", sensorName, state)
				);
				deviceMap.put(entityId, deviceSensorList);
			}
		}

		return deviceMap
			.values()
			.stream()
			.filter(line ->
				line.size() >= 7 && !ListHelper.getValueAtIndex(line, 6, "").contains("=Device Removed/Device Absent")
			)
			.map(line -> {
				line.set(
					6,
					line
						.get(6)
						.replace(STATE_ASSERTED, EQUALS_1)
						.replace(STATE_DEASSERTED, EQUALS_0)
						.replace(DEASSERTED, EQUALS_0)
				);
				return line;
			})
			.collect(Collectors.toList());
	}

	/**
	 * Convert from Fahrenheit to Celsius, rounded to two decimals.
	 *
	 * @param fahrenheit The value we wish to convert
	 * @return double value
	 */
	public static double convertFromFahrenheitToCelsius(double fahrenheit) {
		return Math.round((fahrenheit - 32) * 55.56) / 100D;
	}

	/**
	 * Convert from Kelvin to Celsius.
	 * @param kelvin The value we wish to convert
	 * @return double value
	 */
	public static double convertFromKelvinToCelsius(double kelvin) {
		return kelvin - 273.15;
	}

	/**
	 * Calculate the current value and thresholds for the temperature row.
	 *
	 * @param unitModifier The modifier used to convert the current value and thresholds
	 * @param currentValue The current reading value
	 * @param baseUnit     The base unit unit to convert the value and the thresholds in case of Kelvin or Fahrenheit units
	 * @param line         The line used to extract the threshold values
	 * @return List of values
	 */
	private static List<String> temperatureRow(
		final int unitModifier,
		double currentValue,
		final String baseUnit,
		final List<String> line
	) {
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
			threshold1 = "";
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
			threshold2 = "";
		}

		return Arrays.asList(String.valueOf(currentValue), threshold1, threshold2);
	}

	/**
	 * Calculate the current value and thresholds for the fan row.
	 *
	 * @param unitModifier The modifier used to convert the current value and thresholds
	 * @param currentValue The current reading value
	 * @param line         The results from which we want to extract the thresholds
	 * @return List of values
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
			threshold1 = "";
		}

		Double threshold2Double = NumberHelper.parseDouble(threshold2, null);
		if (threshold2Double != null) {
			threshold2 = String.valueOf(threshold2Double * Math.pow(10, unitModifier));
		} else {
			threshold2 = "";
		}

		return Arrays.asList(String.valueOf(currentValue), threshold1, threshold2);
	}

	/**
	 * Calculate the current value and thresholds for the voltage row.
	 *
	 * @param unitModifier The modifier used to convert the current value and thresholds
	 * @param currentValue The current reading value
	 * @param line         The results from which we want to extract the thresholds
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
			threshold1 = "";
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
			threshold2 = "";
		}

		return Arrays.asList(String.valueOf(currentValue), threshold1, threshold2);
	}

	/**
	 * Calculate the current value and thresholds for the power row.
	 * @param unitModifier The modifier used to convert the current value and thresholds
	 * @param currentValue The current reading value
	 * @param line         The results from which we want to extract the thresholds
	 * @return List of values
	 */
	private static List<String> powerRow(final int unitModifier, double currentValue, final List<String> line) {
		String threshold1 = ListHelper.getValueAtIndex(line, 8, "0.0");
		String threshold2 = ListHelper.getValueAtIndex(line, 7, "0.0");

		Double threshold1Double = NumberHelper.parseDouble(threshold1, null);

		if (threshold1Double != null) {
			threshold1Double = threshold1Double * Math.pow(10, unitModifier);
			threshold1 = String.valueOf(threshold1Double);
		} else {
			threshold1 = "";
		}

		Double threshold2Double = NumberHelper.parseDouble(threshold2, null);

		if (threshold2Double != null) {
			threshold2Double = threshold2Double * Math.pow(10, unitModifier);
			threshold2 = String.valueOf(threshold2Double);
		} else {
			threshold2 = "";
		}

		// Convert values based on unitModifier
		currentValue = currentValue * Math.pow(10, unitModifier);

		return Arrays.asList(String.valueOf(currentValue), threshold1, threshold2);
	}

	/**
	 * Calculate the current value for the energy row.
	 *
	 * @param unitModifier The modifier used to convert the current value
	 * @param currentValue The current reading value
	 * @return String value
	 */
	private static String energyRow(final int unitModifier, double currentValue) {
		// Convert values based on unitModifier
		// Joule conversion.
		return String.valueOf((currentValue * Math.pow(10, unitModifier)) / 3600000);
	}

	/**
	 * Process what we got from ipmitool and return a pretty table.
	 *
	 * @param fruResult  The result from the FRU command.
	 * @param sdrResult  The result from the SDR command.
	 * @return A List of Lists representing the translated ipmiTable.
	 */
	public static List<List<String>> ipmiTranslateFromIpmitool(String fruResult, String sdrResult) {
		List<List<String>> result = new ArrayList<>();

		sdrResult = cleanSensorCommandResult(sdrResult);
		List<String> ipmiTable = ipmiBuildDeviceListFromIpmitool(fruResult, sdrResult);

		//  Now process the numeric sensor list
		ipmiTable = ipmiAddHardwareSensorInfo(sdrResult, ipmiTable);
		// convert ipmiTable to list<list<String>>
		ipmiTable.forEach(line -> result.add(Stream.of(line.split(";")).collect(Collectors.toList())));

		return result;
	}

	/**
	 * Add temperature, fan, voltage, current, and power consumption information from sensors to the given ipmiTable.
	 *
	 * @param sdrResult  The result from the SDR command.
	 * @param ipmiTable  The existing ipmiTable to which sensor information will be added.
	 * @return The updated ipmiTable with sensor information.
	 */
	public static List<String> ipmiAddHardwareSensorInfo(String sdrResult, List<String> ipmiTable) {
		for (String sensorEntry : sdrResult.split(NEW_LINE)) {
			sensorEntry = sensorEntry.replace(TABLE_SEP, NEW_LINE);
			String sensorName = "";
			String sensorId = "";
			String entityId = "";
			String deviceType = "";
			String deviceId = "";
			String location = "";

			// Get name, ID, entity ID and device type
			// ID, Name
			// example Sensor ID : Ambient (0x1)
			// sensorName = Ambient && sensorID = 1
			String sensorIdLine = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_SENSORID, "", "");
			if (sensorIdLine.isEmpty()) {
				continue;
			}
			sensorName = sensorIdLine.substring(sensorIdLine.indexOf(":") + 1, sensorIdLine.indexOf("(")).trim();
			Matcher matcher = PATTERN_BTW_BRACKETS.matcher(sensorIdLine);
			if (matcher.find()) {
				sensorId = matcher.group(1).replace("0x", "");
			} else {
				continue; // we should have a sensorId !!!!
			}

			// Sensor attached to which device?
			// Entry ==> Entity ID : 39.0 (External Environment)
			// Output ==> entityID = 39.0 && deviceType = External Environment && deviceId = 0 && location = External Environment 0
			entityId = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_ENTITYID, ":", "(");
			String entityIdLine = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_ENTITYID, "", "");

			if (!entityIdLine.isEmpty() && entityIdLine.contains(":")) {
				matcher = PATTERN_BTW_BRACKETS.matcher(entityIdLine);
				if (matcher.find()) {
					deviceType = matcher.group(1);
					// check if deviceType still contains bracket ==> example : Entity ID             : 224.0 (Unknown (0xE0))
					if (deviceType.contains("(")) {
						deviceType = deviceType.substring(0, deviceType.indexOf("(")).trim();
					}
				}
				if (entityId.contains(".")) {
					deviceId = entityId.split("\\.")[1];
				}
				location = String.format("%s %s", deviceType, deviceId);
			} // Entity ID

			// Sensor Reading        : 1.513 (+/- 0.005) Volts
			// careful it also can be Sensor Reading        : No Reading
			String sensorReadingLine = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_SENSOR_READING, "", "");

			if (!sensorReadingLine.isEmpty() && sensorReadingLine.contains("(")) {
				String valueReading = sensorReadingLine
					.substring(sensorReadingLine.indexOf(":") + 1, sensorReadingLine.indexOf("("))
					.trim();
				if (!NUM_PATTERN.matcher(valueReading).matches()) {
					continue;
				}
				String unit = sensorReadingLine.substring(sensorReadingLine.indexOf(")") + 1).trim();
				//Depending on the unit, get different fields and display the result
				switch (unit) {
					case "degrees C": // Temperature
						String degreesResult = getTemperatureFromSensor(sensorEntry, sensorName, sensorId, location, valueReading);
						ipmiTable.add(degreesResult);
						break;
					case "RPM": // Tachometers (fans)
						String fanResult = getFanFromSensor(sensorEntry, sensorName, sensorId, location, valueReading);
						ipmiTable.add(fanResult);
						break;
					case "Volts": // Voltages
						String voltageResult = getVoltageFromSensor(sensorEntry, sensorName, sensorId, location, valueReading);
						ipmiTable.add(voltageResult);
						break;
					case "Amps":
						ipmiTable.add(String.format("Current;%s;%s;%s;%s", sensorId, sensorName, location, valueReading));
						break;
					case "Watts": // Power consumption
						ipmiTable.add(String.format("PowerConsumption;%s;%s;%s;%s", sensorId, sensorName, location, valueReading));
						break;
					default:
						break;
				}
			}
		} // end for sensorEntry
		return ipmiTable;
	}

	/**
	 * Get voltage information for the given sensor entry.
	 * Threshold1 is determined by priority: Lower Non-Critical > Lower Critical > Lower Non-Recoverable.
	 * Threshold2 is determined by priority: Upper Non-Critical > Upper Critical > Upper Non-Recoverable.
	 *
	 * @param sensorEntry The sensor entry to extract voltage information from.
	 * @param sensorName The name of the sensor.
	 * @param sensorId The ID of the sensor.
	 * @param location The location of the sensor.
	 * @param valueReading The current value reading of the sensor.
	 * @return A formatted string containing voltage information.
	 */
	public static String getVoltageFromSensor(
		String sensorEntry,
		String sensorName,
		String sensorId,
		String location,
		String valueReading
	) {
		String threshold1 = checkPatternAndReturnDelimitedString(
			sensorEntry,
			PATTERN_THRESHOLD_LOWER_NON_CRITICAL,
			":",
			""
		);
		if (threshold1.isEmpty() || !NUM_PATTERN.matcher(threshold1).matches() || Double.parseDouble(threshold1) == 0) {
			// if the result is not numeric or == 0 then check Lower critical
			threshold1 = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_THRESHOLD_LOWER_CRITICAL, ":", "");
			if (threshold1.isEmpty() || !NUM_PATTERN.matcher(threshold1).matches() || Double.parseDouble(threshold1) == 0) {
				// if the result is not numeric or == 0 then check Lower non-recoverable
				threshold1 =
					checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_THRESHOLD_LOWER_NON_RECOVERABLE, ":", "");
				if (!NUM_PATTERN.matcher(threshold1).matches() || Double.parseDouble(threshold1) == 0) {
					// if the result is not numeric reset the value
					threshold1 = "";
				}
			}
		}

		String threshold2 = checkPatternAndReturnDelimitedString(
			sensorEntry,
			PATTERN_THRESHOLD_UPPER_NON_CRITICAL,
			":",
			""
		);
		if (threshold2.isEmpty() || !NUM_PATTERN.matcher(threshold2).matches() || Double.parseDouble(threshold2) == 0) {
			// if the result is not numeric then check Upper critical
			threshold2 = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_THRESHOLD_UPPER_CRITICAL, ":", "");
			if (threshold2.isEmpty() || !NUM_PATTERN.matcher(threshold2).matches() || Double.parseDouble(threshold2) == 0) {
				// if the result is not numeric then check Upper non-recoverable
				threshold2 =
					checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_THRESHOLD_UPPER_NON_RECOVERABLE, ":", "");
				if (!NUM_PATTERN.matcher(threshold2).matches() || Double.parseDouble(threshold2) == 0) {
					// if the result is not numeric reset the value
					threshold2 = "";
				}
			}
		}
		if (NUM_PATTERN.matcher(threshold1).matches()) {
			threshold1 = Double.toString(Double.parseDouble(threshold1) * 1000);
		}
		if (NUM_PATTERN.matcher(threshold2).matches()) {
			threshold2 = Double.toString(Double.parseDouble(threshold2) * 1000);
		}
		if (NUM_PATTERN.matcher(valueReading).matches()) {
			valueReading = Double.toString(Double.parseDouble(valueReading) * 1000);
		}
		return String.format(
			"Voltage;%s;%s;%s;%s;%s;%s",
			sensorId,
			sensorName,
			location,
			valueReading,
			threshold1,
			threshold2
		);
	}

	/**
	 * Get fan information for the given sensor entry.
	 * Threshold1 is Lower Non-Critical if it exists.
	 * Threshold2 is determined by priority: Lower Critical > Lower Non-Recoverable.
	 *
	 * @param sensorEntry The sensor entry to extract fan information from.
	 * @param sensorName The name of the sensor.
	 * @param sensorId The ID of the sensor.
	 * @param location The location of the sensor.
	 * @param valueReading The current value reading of the sensor.
	 * @return A formatted string containing fan information.
	 */
	public static String getFanFromSensor(
		String sensorEntry,
		String sensorName,
		String sensorId,
		String location,
		String valueReading
	) {
		String threshold1 = "";
		Matcher matcherThreshold = PATTERN_THRESHOLD_LOWER_NON_CRITICAL.matcher(sensorEntry);
		if (matcherThreshold.find()) {
			threshold1 = matcherThreshold.group(0).trim();
			threshold1 = threshold1.substring(threshold1.indexOf(":") + 1).trim();

			if (!NUM_PATTERN.matcher(threshold1).matches()) { // if the result is not numeric reset the value
				threshold1 = "";
			}
		}
		String threshold2 = "";
		matcherThreshold = PATTERN_THRESHOLD_LOWER_CRITICAL.matcher(sensorEntry);
		if (matcherThreshold.find()) {
			threshold2 = matcherThreshold.group(0).trim();
			threshold2 = threshold2.substring(threshold2.indexOf(":") + 1).trim();
		}
		if (!NUM_PATTERN.matcher(threshold2).matches()) { // if the result is not numeric check Upper
			// non-recoverable
			matcherThreshold = PATTERN_THRESHOLD_LOWER_NON_RECOVERABLE.matcher(sensorEntry);
			if (matcherThreshold.find()) {
				threshold2 = matcherThreshold.group(0).trim();
				threshold2 = threshold2.substring(threshold2.indexOf(":") + 1).trim();
			}
			if (!NUM_PATTERN.matcher(threshold2).matches()) { // if the result is not numeric reset the value
				threshold2 = "";
			}
		}
		return String.format("Fan;%s;%s;%s;%s;%s;%s", sensorId, sensorName, location, valueReading, threshold1, threshold2);
	}

	/**
	 * Get temperature information for the given sensor entry.
	 * Threshold1 is Upper Non-Critical if it exists.
	 * Threshold2 is determined by priority: Upper Critical > Upper Non-Recoverable.
	 *
	 * @param sensorEntry The sensor entry to extract temperature information from.
	 * @param sensorName The name of the sensor.
	 * @param sensorId The ID of the sensor.
	 * @param location The location of the sensor.
	 * @param valueReading The current value reading of the sensor.
	 * @return A formatted string containing temperature information.
	 */
	public static String getTemperatureFromSensor(
		String sensorEntry,
		String sensorName,
		String sensorId,
		String location,
		String valueReading
	) {
		String threshold1 = checkPatternAndReturnDelimitedString(
			sensorEntry,
			PATTERN_THRESHOLD_UPPER_NON_CRITICAL,
			":",
			""
		);
		// priority threashold 1 = Upper non-critical >
		if (!NUM_PATTERN.matcher(threshold1).matches()) { // if the result is not numeric reset the value
			threshold1 = "";
		}

		// priority threashold 1 = Upper critical > Upper non-recoverable
		String threshold2 = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_THRESHOLD_UPPER_CRITICAL, ":", "");

		if (!NUM_PATTERN.matcher(threshold2).matches()) { // if the result is not numeric check Upper
			// non-recoverable
			threshold2 = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_THRESHOLD_UPPER_NON_RECOVERABLE, ":", "");
			if (!NUM_PATTERN.matcher(threshold2).matches()) { // if the result is not numeric reset the value
				threshold2 = "";
			}
		}

		return String.format(
			"Temperature;%s;%s;%s;%s;%s;%s",
			sensorId,
			sensorName,
			location,
			valueReading,
			threshold1,
			threshold2
		);
	}

	/**
	 * Process the data obtained from ipmitool and return a formatted device table.
	 *
	 * @param fruResult The result from the FRU command.
	 * @param sdrResult The result from the SDR command.
	 * @return The formatted device table.
	 */
	public static List<String> ipmiBuildDeviceListFromIpmitool(String fruResult, String sdrResult) {
		List<String> result = new ArrayList<>();
		Map<String, List<String>> fruMap = processFruResult(fruResult);

		List<String> deviceList = new ArrayList<>();
		deviceList = processSdrRecords(sdrResult, fruMap, deviceList);
		// Remove devices that are marked as "removed" or "absent"
		deviceList.removeIf(elt -> elt.contains(DEVICE_ABSENT));
		// Replace "State Asserted" and "State Deasserted" by 1 and 0
		deviceList.replaceAll(elt -> elt.replace(STATE_ASSERTED, EQUALS_1));
		deviceList.replaceAll(elt -> elt.replace(STATE_DEASSERTED, EQUALS_0));
		deviceList.replaceAll(elt -> elt.replace(ASSERTED, EQUALS_1));
		deviceList.replaceAll(elt -> elt.replace(DEASSERTED, EQUALS_0));

		result.addAll(fruMap.get(GOOD_LIST));
		result.addAll(fruMap.get(POOR_LIST));
		result.addAll(deviceList);
		return result;
	}

	/**
	 * Parse SDR records in order to extract each sensor and complete the device list.
	 *
	 * @param sdrResult   The result containing SDR records.
	 * @param fruMap      The map containing FRU information.
	 * @param deviceList  The list of devices to be updated.
	 * @return The updated device list.
	 */
	public static List<String> processSdrRecords(
		String sdrResult,
		Map<String, List<String>> fruMap,
		List<String> deviceList
	) {
		// Parse the SDR records
		for (String sensor : sdrResult.split(NEW_LINE)) {
			if (!sensor.startsWith(SENSOR_ID) || !sensor.contains(STATES_ASSERTED) || sensor.contains(NOT_READING)) {
				// Bypass sensors with no state asserted or sensors with "no reading"
				continue;
			}

			sensor = sensor.replace(TABLE_SEP, NEW_LINE);

			// Get name, ID, entity ID and device type
			// ID, Name
			// example Sensor ID : Ambient (0x1)
			// sensorName = Ambient && sensorID = 1
			String sensorName = checkPatternAndReturnDelimitedString(sensor, PATTERN_SENSORID, ":", "(");

			// Sensor attached to which device?
			// Entity ID : 39.0 (External Environment)
			// entityID = 39.0 && deviceType = External Environment
			String entityId = checkPatternAndReturnDelimitedString(sensor, PATTERN_ENTITYID, ":", "(");

			if (entityId == null || entityId.isEmpty()) {
				continue;
			}
			String entityIdLine = checkPatternAndReturnDelimitedString(sensor, PATTERN_ENTITYID, "", "");
			String deviceType = checkPatternAndReturnDelimitedString(entityIdLine, PATTERN_BTW_BRACKETS, "", "")
				.replace("(", "")
				.replace(")", "");
			String deviceId = entityId.contains(".") ? entityId.split("\\.")[1] : "";

			String statusArray = getSensorStatusArray(sensor, sensorName);

			if (statusArray == null || statusArray.isEmpty() || statusArray.equals("|")) {
				continue;
			}

			deviceList =
				addSensorElementToDeviceList(
					deviceList,
					sdrResult,
					deviceType,
					deviceId,
					entityId,
					statusArray,
					fruMap.get(FRU_LIST)
				);
		} // end of s
		return deviceList;
	}

	/**
	 * Parse the entry and get the line that matches the pattern, then return the substring delimited by the given parameters.
	 *
	 * @param entry          The input string to parse.
	 * @param patternToMatch The pattern to match in the entry.
	 * @param leftLimit      The left delimiter of the substring.
	 * @param rightLimit     The right delimiter of the substring.
	 * @return The substring delimited by leftLimit and rightLimit in the matched line, or an empty string if not found.
	 */
	public static String checkPatternAndReturnDelimitedString(
		String entry,
		Pattern patternToMatch,
		String leftLimit,
		String rightLimit
	) {
		Matcher matcher = patternToMatch.matcher(entry);

		boolean noLeftLimit = leftLimit == null || leftLimit.isEmpty();
		boolean noRightLimit = rightLimit == null || rightLimit.isEmpty();

		if (matcher.find()) {
			String matchedLine = matcher.group(0).trim();
			if (noLeftLimit && noRightLimit) { // if there is no limit to the substring, return the
				// first line that matches the pattern
				return matchedLine;
			}

			if (!matchedLine.isEmpty()) {
				if (noRightLimit) {
					return matchedLine.substring(matchedLine.indexOf(leftLimit) + 1).trim();
				} else if (noLeftLimit) {
					return matchedLine.substring(0, matchedLine.indexOf(rightLimit)).trim();
				} else {
					if (
						matchedLine.contains(leftLimit) &&
						matchedLine.contains(rightLimit) &&
						matchedLine.indexOf(rightLimit) > matchedLine.indexOf(leftLimit) + 1
					) {
						return matchedLine.substring(matchedLine.indexOf(leftLimit) + 1, matchedLine.indexOf(rightLimit)).trim();
					}
				}
			}
		}
		return "";
	}

	/**
	 * Add sensor information to the global device list. Check if this entityID was already put into the list,
	 * then complete its description with the given status; otherwise, insert a new sensor entry.
	 *
	 * @param deviceList   The global device list to which the sensor information is added.
	 * @param sdrResult    The result of the SDR command.
	 * @param deviceType   The type of the device.
	 * @param deviceId     The ID of the device.
	 * @param entityId     The entity ID of the sensor.
	 * @param statusArray  The status array of the sensor.
	 * @param fruList      The list of FRU entries.
	 * @return The updated global device list.
	 */
	public static List<String> addSensorElementToDeviceList(
		List<String> deviceList,
		String sdrResult,
		String deviceType,
		String deviceId,
		String entityId,
		String statusArray,
		List<String> fruList
	) {
		// Check whether this entityID was already put into the list
		String deviceElement = String.format(";%s %s;", deviceType, deviceId);
		Optional<String> matchedElt = deviceList.stream().filter(elt -> elt.contains(deviceElement)).findFirst();

		if (!matchedElt.isPresent()) {
			String vendor = "";
			String model = "";
			String serialNumber = "";
			// try to get the corresponding FRU in order to retrieve it serialNumber, model
			// and vendor
			// It's the first time we meet this entityID, look up its FRU entry
			Pattern p = Pattern.compile(
				";Entity ID +: " + Pattern.quote(entityId) + " .*;Logical FRU Device .*",
				Pattern.MULTILINE
			);
			Matcher m = p.matcher(sdrResult);

			if (m.find()) {
				// first match
				String fruDevice = m.group(0).replace(";", "\n");
				// get the corresponding fruID
				Optional<String> findFirstFruDevice = Arrays
					.stream(fruDevice.split("\n"))
					.filter(role -> role.trim().startsWith("Logical FRU Device"))
					.findFirst();
				fruDevice = findFirstFruDevice.isPresent() ? findFirstFruDevice.get() : "";
				if (!fruDevice.isEmpty() && fruDevice.contains(":")) {
					// example : Logical FRU Device     : 09h which corresponds to FRU device ID = 9
					int fruId = Integer.parseInt(fruDevice.split(":")[1].trim().replace("h", ""), 16); // convert
					// to
					// hexadecimal
					// Retrieve the vendor/model/serial from the corresponding FRU
					String elt = fruList.stream().filter(fru -> fru.startsWith(fruId + TABLE_SEP)).findFirst().orElse("");
					if (!elt.isEmpty()) {
						String[] fruSplit = elt.split(TABLE_SEP);
						vendor = fruSplit[1];
						model = fruSplit[2];
						serialNumber = fruSplit[3];
					}
				}
			}

			deviceList.add(
				String.format(
					"%s;%s;%s;%s;%s;%s;%s",
					deviceType,
					deviceId,
					String.format("%s %s", deviceType, deviceId),
					vendor,
					model,
					serialNumber,
					statusArray
				)
			);
		} else {
			// If this entityID was already present in the list, we just need to add the
			// statusArray to it
			int index = deviceList.indexOf(matchedElt.get());
			deviceList.set(index, matchedElt.get() + "|" + statusArray);
		}
		return deviceList;
	}

	/**
	 * Get the actual status of the given sensor. It looks for all states listed (States Asserted and Assertion Events)
	 * which are located before the "Assertions Enabled" entry.
	 *
	 * @param sensorEntry The entry containing information about the sensor.
	 * @param sensorName  The name of the sensor.
	 * @return A string representing the status array of the sensor.
	 */
	public static String getSensorStatusArray(String sensorEntry, String sensorName) {
		if (sensorName.isEmpty()) {
			sensorName = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_SENSORID, TABLE_SEP, "(");
		}

		StringBuilder statusArrayBuilder = new StringBuilder();
		String statusArray = "";
		// check if OEM Specific
		if (sensorEntry.contains(STATES_ASSERTED) && sensorEntry.contains(OEM_SPECIFIC)) {
			statusArray = processOemSpecific(sensorName, sensorEntry);
		} else {
			if (!sensorEntry.contains("Assertions Enabled ")) {
				return null; // next statement will continue to the next sensor (continue the loop)
			}
			Pattern patternBtwBrackets = Pattern.compile("\\[(.*?)\\]");
			sensorEntry = sensorEntry.substring(0, sensorEntry.indexOf("Assertions Enabled "));
			// get the values between the first and the last [, ]
			Matcher matcher = patternBtwBrackets.matcher(sensorEntry);
			StringBuilder assertion = new StringBuilder();

			while (matcher.find()) {
				assertion.append(NEW_LINE).append(matcher.group(1).trim());
			}

			statusArrayBuilder
				.append(sensorName)
				.append("=")
				.append(assertion.toString().trim().replaceAll(NEW_LINE, NEW_LINE + sensorName + "="));
			statusArray = statusArrayBuilder.toString().replaceAll(NEW_LINE, "|");
		}
		return statusArray;
	}

	/**
	 * Retrieve States Asserted entry line and reformat the entry code to deduce the
	 * correct status of the given sensor
	 * Look for all states listed (States Asserted and Assertion Events)
	 * which are located before the "Assertions Enabled" entry
	 * @param sensorName Name of the sensor
	 * @param sensorEntry Entry of the sensor
	 * @return string
	 */
	public static String processOemSpecific(String sensorName, String sensorEntry) {
		if (sensorName.isEmpty()) {
			Pattern patternSensorId = Pattern.compile(SENSOR_ID_REGEX, Pattern.MULTILINE);
			sensorName = checkPatternAndReturnDelimitedString(sensorEntry, patternSensorId, ":", "(");
		}

		Matcher matcher = PATTERN_OEM_SPECIFIC.matcher(sensorEntry);
		if (matcher.find()) {
			String oemLine = matcher.group(0).trim();
			// => sensorEntry = " States Asserted : 0x181 OEM Specific\r\n"
			String oemSpecific = oemLine.split(": 0x")[1].trim().split("\\s")[0].trim();
			// convert to decimal, check the last bit and then re-convert to hex
			oemSpecific = Integer.toHexString((Integer.parseInt(oemSpecific, 16) | 32768));
			// complete with 0 in the left in order to reach 4 characters
			oemSpecific = String.format("%1$4s", oemSpecific).replace(' ', '0');
			return sensorName + "=0x" + oemSpecific;
		}
		return "";
	}

	/**
	 * Process the raw result of the FRU command and return the list of good FRU and poor FRU.
	 *
	 * @param fruResult The raw result of the FRU command.
	 * @return A map containing lists of good FRU, poor FRU, and all FRU entries.
	 */
	public static Map<String, List<String>> processFruResult(String fruResult) {
		List<String> goodFruList = new ArrayList<>();
		List<String> poorFruList = new ArrayList<>();
		List<String> fruList = new ArrayList<>();
		Map<String, List<String>> ipmiTable = new HashMap<>();

		// extract each FRU bloc, which are separated by an empty line
		for (String fruEntry : fruResult.split("\n\n")) {
			boolean board = fruEntry.contains(BOARD2);
			// spot line FRU Device Description : Built in FRU Device (ID 0)
			String fruID = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_FRUID, ":", "").trim();
			// retrieve the ID between brackets ==> 0
			fruID = checkPatternAndReturnDelimitedString(fruID, PATTERN_BTW_BRACKETS, "(", ")").replace("ID ", "").trim();
			String fruVendor = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_VENDOR, ":", "").trim();
			String fruModel = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_MODEL, ":", "").trim();
			String fruSN = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_SERIAL, ":", "").trim();
			String boardVendor = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_BORD_VENDOR, ":", "").trim();
			String boardModel = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_BORD_MODEL, ":", "").trim();
			String boardSN = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_BORD_SERIAL, ":", "").trim();

			String fruEntryResult = String.format("FRU;%s;%s;%s", fruVendor, fruModel, fruSN);
			if (!board) {
				if (!fruModel.isEmpty() && !fruSN.isEmpty()) {
					goodFruList.add(fruEntryResult);
				} else if (!fruVendor.isEmpty()) {
					poorFruList.add(fruEntryResult);
				}
			} else if (!boardVendor.isEmpty()) {
				fruEntryResult = String.format("FRU;%s;%s;%s", boardVendor, boardModel, boardSN);
				poorFruList.add(fruEntryResult);
			}

			fruList.add(fruID + TABLE_SEP + fruEntryResult);
		}
		ipmiTable.put(GOOD_LIST, goodFruList);
		ipmiTable.put(POOR_LIST, poorFruList);
		ipmiTable.put(FRU_LIST, fruList);
		return ipmiTable;
	}

	/**
	 * Reformat the ipmitoolSdr list so that each SDR entry is on a separate line.
	 * Removes lines that start with "BMC req" and "-- ".
	 *
	 * @param sdrResult The input string containing the ipmitool SDR list.
	 * @return The reformatted string with one line per SDR entry.
	 */
	public static String cleanSensorCommandResult(String sdrResult) {
		if (sdrResult == null || sdrResult.isEmpty()) {
			return sdrResult;
		}

		// exclude rows that start with "^BMC req" and "-- "
		// in order to differentiate blocs of sensorID and the empty lines that will be
		// created by the replace operation
		sdrResult = PATTERN_BMC_REQ.matcher(sdrResult).replaceAll("");
		sdrResult = sdrResult.replace(TABLE_SEP, ",");
		sdrResult = sdrResult.replace(NEW_LINE, TABLE_SEP);
		sdrResult = sdrResult.replace(";;", NEW_LINE);

		return sdrResult;
	}
}
