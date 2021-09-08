package com.sentrysoftware.matrix.engine.strategy.utils;

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

import com.sentrysoftware.matrix.common.helpers.ArrayHelper;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.ListHelper;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;

import lombok.NonNull;

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

	private static final Pattern PATTERN_IS_NUMERICAL = Pattern.compile("-?\\d+(\\.\\d+)?");

	private static final Pattern PATTERN_BTW_BRACKETS = Pattern.compile("\\((.*?)\\)");
	private static final Pattern PATTERN_SENSORID = Pattern.compile(SENSOR_ID_REGEX, Pattern.MULTILINE);
	private static final Pattern PATTERN_ENTITYID = Pattern.compile("^ *Entity ID.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_SENSOR_READING = Pattern.compile("^ *Sensor Reading.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_THRESHOLD_UPPER_NON_CRITICAL = Pattern.compile(".*Upper non-critical.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_THRESHOLD_UPPER_CRITICAL = Pattern.compile(".*Upper critical.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_THRESHOLD_UPPER_NON_RECOVERABLE = Pattern.compile(".*Upper non-recoverable.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_THRESHOLD_LOWER_NON_CRITICAL = Pattern.compile(".*Lower non-critical.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_THRESHOLD_LOWER_CRITICAL = Pattern.compile(".*Lower critical.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_THRESHOLD_LOWER_NON_RECOVERABLE = Pattern.compile(".*Lower non-recoverable.*", Pattern.MULTILINE);
	private static final Pattern PATTERN_OEM_SPECIFIC = Pattern.compile("States Asserted +: 0x[0-9a-zA-Z]+ +OEM Specific",
			Pattern.MULTILINE);

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
	public static List<List<String>> ipmiTranslateFromWmi(@NonNull List<List<String>> wmiComputerSystem,
			@NonNull List<List<String>> wmiNumericSensors,
			@NonNull List<List<String>> wmiDiscreteSensors) {
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
						wmiComputerSystemLine.get(0));
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
	 *
	 * @param wmiDiscreteSensors The WMI discrete sensors result table
	 * @return List of List (Table)
	 */
	private static List<List<String>> translateWmiDiscreteSensors(final List<List<String>> wmiDiscreteSensors) {

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
			if (deviceLine != null) {
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
						sensorName + HardwareConstants.EQUAL + state);
				deviceMap.put(entityId, deviceSensorList);
			}
		}

		return deviceMap.values().stream()
				.filter(line -> line.size() >= 7 && !ListHelper.getValueAtIndex(line, 6, "").contains("=Device Removed/Device Absent"))
				.map(line ->
				{
					line.set(6, line.get(6)
							.replace(STATE_ASSERTED, EQUALS_1)
							.replace(STATE_DEASSERTED, EQUALS_0)
							.replace(DEASSERTED, EQUALS_0));
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
	 *
	 * @param unitModifier The modifier used to convert the current value
	 * @param currentValue The current reading value
	 * @return String value
	 */
	private static String energyRow(final int unitModifier, double currentValue) {
		// Convert values based on unitModifier
		// Joule conversion.
		return String.valueOf(currentValue * Math.pow(10, unitModifier) / 3600000);
	}


	/**
	 * Process what we got from ipmitool and return a pretty table
	 *
	 * @param fruResult
	 * @param sdrResult
	 * @return
	 */
	public static List<List<String>> ipmiTranslateFromIpmitool(String fruResult, String sdrResult) {
		List<List<String>> result = new ArrayList<>();

		sdrResult = cleanSensorCommandResult(sdrResult);
		List<String> ipmiTable = ipmiBuildDeviceListFromIpmitool(fruResult, sdrResult);

		//  Now process the numeric sensor list
		ipmiTable = ipmiAddHardwareSensorInfo(sdrResult, ipmiTable);
		// convert imptTable to list<list<String>>
		ipmiTable.stream().forEach(line -> result.add(Stream.of(line.split(";")).collect(Collectors.toList())));

		return result;

	}

	/**
	 * Add Temperature, Fan, Voltage, Current, PowerConsumption info from Sensors to the given ipmiTable
	 * @param sdrResult
	 * @param ipmiTable
	 * @return
	 */
	public static List<String> ipmiAddHardwareSensorInfo(String sdrResult, List<String> ipmiTable) {

		for (String sensorEntry : sdrResult.split(HardwareConstants.NEW_LINE)) {
			sensorEntry = sensorEntry.replace(HardwareConstants.SEMICOLON, HardwareConstants.NEW_LINE);
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
			String sensorIdLine = checkPatternAndReturnDelimitedString(
					sensorEntry, PATTERN_SENSORID, HardwareConstants.EMPTY,HardwareConstants.EMPTY);
			if (sensorIdLine.isEmpty()) {
				continue;
			}
			sensorName = sensorIdLine.substring(sensorIdLine.indexOf(HardwareConstants.COLON) + 1, sensorIdLine.indexOf(HardwareConstants.OPENING_PARENTHESIS)).trim();
			Matcher matcher = PATTERN_BTW_BRACKETS.matcher(sensorIdLine);
			if (matcher.find()) {
				sensorId = matcher.group(1).replace("0x", HardwareConstants.EMPTY);
			} else {
				continue; // we should have a sensorId !!!!
			}

			// Sensor attached to which device?
			// Entry ==> Entity ID : 39.0 (External Environment)
			// Output ==> entityID = 39.0 && deviceType = External Environment && deviceId = 0 && location = External Environment 0
			entityId = checkPatternAndReturnDelimitedString(
					sensorEntry, PATTERN_ENTITYID, HardwareConstants.COLON, HardwareConstants.OPENING_PARENTHESIS);
			String entityIdLine =  checkPatternAndReturnDelimitedString(
					sensorEntry, PATTERN_ENTITYID, HardwareConstants.EMPTY, HardwareConstants.EMPTY);

			if (!entityIdLine.isEmpty() && entityIdLine.contains(":")) {
				matcher = PATTERN_BTW_BRACKETS.matcher(entityIdLine);
				if (matcher.find()) {
					deviceType = matcher.group(1);
					// check if deviceType still contains bracket ==> example : Entity ID             : 224.0 (Unknown (0xE0))
					if (deviceType.contains(HardwareConstants.OPENING_PARENTHESIS)) {
						deviceType = deviceType.substring(0, deviceType.indexOf(HardwareConstants.OPENING_PARENTHESIS)).trim();
					}
				}
				if (entityId.contains(".")) {
					deviceId = entityId.split("\\.")[1];
				}
				location = deviceType + " " + deviceId;
			} // Entity ID

			// Sensor Reading        : 1.513 (+/- 0.005) Volts
			// careful it also can be Sensor Reading        : No Reading
			String sensorReadingLine = checkPatternAndReturnDelimitedString(
					sensorEntry, PATTERN_SENSOR_READING, HardwareConstants.EMPTY, HardwareConstants.EMPTY);

			if (!sensorReadingLine.isEmpty() && sensorReadingLine.contains(HardwareConstants.OPENING_PARENTHESIS)) {
				String valueReading = sensorReadingLine
						.substring(sensorReadingLine.indexOf(":") + 1,
								sensorReadingLine.indexOf(HardwareConstants.OPENING_PARENTHESIS))
						.trim();
				if (!PATTERN_IS_NUMERICAL.matcher(valueReading).matches()) {
					continue;
				}
				String unit = sensorReadingLine
						.substring(sensorReadingLine.indexOf(")") + 1)
						.trim();
				//Depending on the unit, get different fields and display the result
				switch (unit) {
				case "degrees C": // Temperature
					String degreesResult = getTemperatureFromSensor(sensorEntry,
							sensorName, sensorId, location, valueReading);
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
					ipmiTable.add( String.format("PowerConsumption;%s;%s;%s;%s", sensorId, sensorName, location, valueReading));
					break;

				default : break;
				}
			}

		} // end for sensorEntry
		return ipmiTable;
	}

	/**
	 * Get Voltage info for the given Sensor entry
	 * threshold1 = by priority if exists : Lower non-critical > Lower critical > Lower non-recoverable
	 * threshold2 =  by priority if exists : Upper non-critical > Upper critical > Upper non-recoverable
	 * @param sensorEntry
	 * @param sensorName
	 * @param sensorId
	 * @param location
	 * @param valueReading
	 * @return
	 */
	public static String getVoltageFromSensor(String sensorEntry, String sensorName, String sensorId, String location,
			String valueReading) {

		String threshold1 = checkPatternAndReturnDelimitedString(
				sensorEntry, PATTERN_THRESHOLD_LOWER_NON_CRITICAL, HardwareConstants.COLON, HardwareConstants.EMPTY);
		if (threshold1.isEmpty() || !PATTERN_IS_NUMERICAL.matcher(threshold1).matches()
				|| Double.parseDouble(threshold1) == 0) {
			// if the result is not numeric or == 0 then check Lower critical
			threshold1 = checkPatternAndReturnDelimitedString(
					sensorEntry, PATTERN_THRESHOLD_LOWER_CRITICAL, HardwareConstants.COLON, HardwareConstants.EMPTY);
			if (threshold1.isEmpty() || !PATTERN_IS_NUMERICAL.matcher(threshold1).matches()
					|| Double.parseDouble(threshold1) == 0) {

				// if the result is not numeric or == 0 then check Lower non-recoverable
				threshold1 = checkPatternAndReturnDelimitedString(
						sensorEntry, PATTERN_THRESHOLD_LOWER_NON_RECOVERABLE, HardwareConstants.COLON, HardwareConstants.EMPTY);
				if (!PATTERN_IS_NUMERICAL.matcher(threshold1).matches() || Double.parseDouble(threshold1) == 0) {
					// if the result is not numeric reset the value
					threshold1 = HardwareConstants.EMPTY;
				}
			}
		}

		String threshold2 = checkPatternAndReturnDelimitedString(
				sensorEntry, PATTERN_THRESHOLD_UPPER_NON_CRITICAL, HardwareConstants.COLON, HardwareConstants.EMPTY);
		if (threshold2.isEmpty() || !PATTERN_IS_NUMERICAL.matcher(threshold2).matches()
				|| Double.parseDouble(threshold2) == 0) {

			// if the result is not numeric then check Upper critical
			threshold2 = checkPatternAndReturnDelimitedString(
					sensorEntry, PATTERN_THRESHOLD_UPPER_CRITICAL, HardwareConstants.COLON, HardwareConstants.EMPTY);
			if (threshold2.isEmpty() || !PATTERN_IS_NUMERICAL.matcher(threshold2).matches()
					|| Double.parseDouble(threshold2) == 0) {
				// if the result is not numeric then check Upper non-recoverable
				threshold2 = checkPatternAndReturnDelimitedString(
						sensorEntry, PATTERN_THRESHOLD_UPPER_NON_RECOVERABLE, HardwareConstants.COLON, HardwareConstants.EMPTY);
				if (!PATTERN_IS_NUMERICAL.matcher(threshold2).matches() || Double.parseDouble(threshold2) == 0) {
					// if the result is not numeric reset the value
					threshold2 = HardwareConstants.EMPTY;
				}
			}
		}
		if (PATTERN_IS_NUMERICAL.matcher(threshold1).matches()) {
			threshold1 = Double.toString(Double.parseDouble(threshold1) * 1000);
		}
		if (PATTERN_IS_NUMERICAL.matcher(threshold2).matches()) {
			threshold2 = Double.toString(Double.parseDouble(threshold2) * 1000);
		}
		if (PATTERN_IS_NUMERICAL.matcher(valueReading).matches()) {
			valueReading = Double.toString(Double.parseDouble(valueReading) * 1000);
		}
		return String.format("Voltage;%s;%s;%s;%s;%s;%s", sensorId, sensorName, location, valueReading, threshold1, threshold2);
	}

	/**
	 * Get Fan info for the given Sensor entry
	 * threshold1 = if exists : Lower non-critical
	 * threshold2 =  by priority if exists : Lower critical > Lower non-recoverable
	 * @param sensorEntry
	 * @param sensorName
	 * @param sensorId
	 * @param location
	 * @param valueReading
	 * @return
	 */
	public static String getFanFromSensor(String sensorEntry, String sensorName, String sensorId,
			String location, String valueReading) {
		String threshold1 = HardwareConstants.EMPTY;
		Matcher matcherThreshold =
				PATTERN_THRESHOLD_LOWER_NON_CRITICAL.matcher(sensorEntry);
		if (matcherThreshold.find()) {
			threshold1 = matcherThreshold.group(0).trim();
			threshold1= threshold1.substring(threshold1.indexOf(HardwareConstants.COLON)+1).trim();

			if (!PATTERN_IS_NUMERICAL.matcher(threshold1).matches()) {// if the result is not numeric reset the value
				threshold1 = HardwareConstants.EMPTY;
			}
		}
		String threshold2 = HardwareConstants.EMPTY;
		matcherThreshold =
				PATTERN_THRESHOLD_LOWER_CRITICAL.matcher(sensorEntry);
		if (matcherThreshold.find()) {
			threshold2 = matcherThreshold.group(0).trim();
			threshold2= threshold2.substring(threshold2.indexOf(HardwareConstants.COLON)+1).trim();
		}
		if (!PATTERN_IS_NUMERICAL.matcher(threshold2).matches()) {// if the result is not numeric check Upper
																	// non-recoverable
			matcherThreshold = PATTERN_THRESHOLD_LOWER_NON_RECOVERABLE.matcher(sensorEntry);
			if (matcherThreshold.find()) {
				threshold2 = matcherThreshold.group(0).trim();
				threshold2 = threshold2.substring(threshold2.indexOf(":") + 1).trim();
			}
			if (!PATTERN_IS_NUMERICAL.matcher(threshold2).matches()) {// if the result is not numeric reset the value
				threshold2 = HardwareConstants.EMPTY;
			}
		}
		return String.format("Fan;%s;%s;%s;%s;%s;%s", sensorId, sensorName, location, valueReading, threshold1, threshold2);

	}

	/**
	 * Get Temperature info for the given Sensor entry
	 * threshold1 = if exists : Upper non-critical
	 * threshold2 =  by priority of exists : Upper critical > Upper non-recoverable
	 * @param sensorEntry
	 * @param sensorName
	 * @param sensorId
	 * @param location
	 * @param valueReading
	 * @return
	 */
	public static String getTemperatureFromSensor(String sensorEntry,
			String sensorName, String sensorId, String location, String valueReading) {
		String threshold1 = checkPatternAndReturnDelimitedString(
					sensorEntry,
					PATTERN_THRESHOLD_UPPER_NON_CRITICAL,
					HardwareConstants.COLON,
					HardwareConstants.EMPTY);
		// priority threashold 1 = Upper non-critical >
		if (!PATTERN_IS_NUMERICAL.matcher(threshold1).matches()) {// if the result is not numeric reset the value
			threshold1 = HardwareConstants.EMPTY;
		}

		// priority threashold 1 = Upper critical > Upper non-recoverable
		String threshold2 = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_THRESHOLD_UPPER_CRITICAL,
				HardwareConstants.COLON, HardwareConstants.EMPTY);

		if (!PATTERN_IS_NUMERICAL.matcher(threshold2).matches()) {// if the result is not numeric check Upper
																	// non-recoverable
			threshold2 = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_THRESHOLD_UPPER_NON_RECOVERABLE,
					HardwareConstants.COLON, HardwareConstants.EMPTY);
			if (!PATTERN_IS_NUMERICAL.matcher(threshold2).matches()) {// if the result is not numeric reset the value
				threshold2 = HardwareConstants.EMPTY;
			}
		}

		return String.format("Temperature;%s;%s;%s;%s;%s;%s", sensorId, sensorName, location, valueReading, threshold1, threshold2);
	}

	/**
	 * Process what we got from ipmitool and return a pretty device table
	 * @param fruResult
	 * @param sdrResult
	 * @return
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
	 * Parse SDR records in order to extract each sensor and complete the device list
	 * @param sdrResult
	 * @param fruMap
	 * @param deviceList
	 * @return
	 */
	public static List<String> processSdrRecords(String sdrResult, Map<String, List<String>> fruMap, List<String> deviceList) {
		// Parse the SDR records
		for (String sensorEntry : sdrResult.split(HardwareConstants.NEW_LINE)) {
			if (!sensorEntry.startsWith(SENSOR_ID)
					|| !sensorEntry.contains(STATES_ASSERTED)
					|| sensorEntry.contains(NOT_READING)) { // Bypass sensors with no state asserted or sensors with "no reading"
				continue;
			}

			sensorEntry = sensorEntry.replace(HardwareConstants.SEMICOLON, HardwareConstants.NEW_LINE);

			// Get name, ID, entity ID and device type
			// ID, Name
			// example Sensor ID : Ambient (0x1)
			// sensorName = Ambient && sensorID = 1
			String sensorName = checkPatternAndReturnDelimitedString(
					sensorEntry,
					PATTERN_SENSORID,
					HardwareConstants.COLON,
					HardwareConstants.OPENING_PARENTHESIS);

			// Sensor attached to which device?
			// Entity ID : 39.0 (External Environment)
			// entityID = 39.0 && deviceType = External Environment
			String entityId = checkPatternAndReturnDelimitedString(
					sensorEntry,
					PATTERN_ENTITYID,
					HardwareConstants.COLON,
					HardwareConstants.OPENING_PARENTHESIS);

			if (entityId == null || entityId.isEmpty()) {
				continue;
			}
			String entityIdLine = checkPatternAndReturnDelimitedString(sensorEntry,
					PATTERN_ENTITYID,
					HardwareConstants.EMPTY,
					HardwareConstants.EMPTY);
			String deviceType = checkPatternAndReturnDelimitedString(
							entityIdLine,
							PATTERN_BTW_BRACKETS,
							HardwareConstants.EMPTY,
							HardwareConstants.EMPTY)
					.replace(HardwareConstants.OPENING_PARENTHESIS,
							HardwareConstants.EMPTY)
					.replace(HardwareConstants.CLOSING_PARENTHESIS,
							HardwareConstants.EMPTY);
			String deviceId = entityId.contains(HardwareConstants.DOT) ? entityId.split("\\.")[1] : HardwareConstants.EMPTY;

			String statusArray = getSensorStatusArray(sensorEntry, sensorName);

			if (statusArray == null || statusArray.isEmpty() || statusArray.equals(HardwareConstants.PIPE)) {
				continue;
			}

			deviceList = addSensorElementToDeviceList(deviceList, sdrResult, deviceType,
					deviceId, entityId, statusArray, fruMap.get(FRU_LIST));

		} // end of sensorEntry
		return deviceList;
	}

	/**
	 * Parse entry and get the line that matches the pattern and return the substring delimited by the given parameters
	 * @param entry
	 * @param patternToMatch
	 * @param leftLimit
	 * @param rightLimit
	 * @return
	 */
	public static String checkPatternAndReturnDelimitedString(String entry, Pattern patternToMatch, String leftLimit, String rightLimit) {
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
					if ( matchedLine.contains(leftLimit)
							&& matchedLine.contains(rightLimit)
							&& matchedLine.indexOf(rightLimit) > matchedLine.indexOf(leftLimit) + 1) {
						return matchedLine
								.substring(matchedLine.indexOf(leftLimit) + 1, matchedLine.indexOf(rightLimit)).trim();
					}
				}
			}

		}
		return HardwareConstants.EMPTY;
	}

	/**
	 * Add sensor information to the global device list
	 * Check if this entityID was already put into the list then complete its description with the given status,
	 * otherwise insert new sensor entry
	 * @param deviceList
	 * @param sdrResult
	 * @param deviceType
	 * @param deviceId
	 * @param entityId
	 * @param statusArray
	 * @return
	 */
	public static List<String> addSensorElementToDeviceList(List<String> deviceList, String sdrResult, String deviceType,
			String deviceId, String entityId, String statusArray, List<String> fruList) {

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
			Pattern p = Pattern.compile(";Entity ID +: " + Pattern.quote(entityId) + " .*;Logical FRU Device .*", Pattern.MULTILINE);
			Matcher m = p.matcher(sdrResult);

			if (m.find()) {
				// first match
				String fruDevice = m.group(0).replace(";", "\n");
				// get the corresponding fruID
				Optional<String> findFirstFruDevice = Arrays.stream(fruDevice.split("\n"))
						.filter(role -> role.trim().startsWith("Logical FRU Device")).findFirst();
				fruDevice = findFirstFruDevice.isPresent() ? findFirstFruDevice.get() : HardwareConstants.EMPTY;
				if (!fruDevice.isEmpty() && fruDevice.contains(HardwareConstants.COLON)) {
					// example : Logical FRU Device     : 09h which corresponds to FRU device ID = 9
					int fruId = Integer
							.parseInt(fruDevice.split(HardwareConstants.COLON)[1].trim().replace("h", ""), 16); // convert
																														// to
																														// hexadecimal
					// Retrieve the vendor/model/serial from the corresponding FRU
					String elt = fruList.stream().filter(fru -> fru.startsWith(fruId + HardwareConstants.SEMICOLON)).findFirst().orElse("");
					if (!elt.isEmpty()) {
						String[] fruSplit = elt.split(HardwareConstants.SEMICOLON);
						vendor = fruSplit[1];
						model = fruSplit[2];
						serialNumber = fruSplit[3];
					}
				}

			}

			deviceList.add(String.format("%s;%s;%s;%s;%s;%s;%s",
					deviceType,
					deviceId,
					deviceType + " " + deviceId,
					vendor,
					model,
					serialNumber,
					statusArray));
		} else {
			// If this entityID was already present in the list, we just need to add the
			// statusArray to it
			int index = deviceList.indexOf(matchedElt.get());
			deviceList.set(index, matchedElt.get() + HardwareConstants.PIPE + statusArray);
		}
		return deviceList;
	}

	/**
	 * Get the actual status of the given sensor
	 * Look for all states listed (States Asserted and Assertion Events) which are located before the "Assertions Enabled" entry
	 * @param sensorEntry
	 * @param sensorName
	 * @return
	 */
	public static String getSensorStatusArray(String sensorEntry, String sensorName) {
		if (sensorName.isEmpty()) {
			sensorName = checkPatternAndReturnDelimitedString(sensorEntry, PATTERN_SENSORID,
					HardwareConstants.SEMICOLON, HardwareConstants.OPENING_PARENTHESIS);
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
				assertion.append(HardwareConstants.NEW_LINE).append(matcher.group(1).trim());
			}

			statusArrayBuilder.append(sensorName).append(HardwareConstants.EQUAL)
					.append(assertion.toString().trim().replaceAll(HardwareConstants.NEW_LINE,
							HardwareConstants.NEW_LINE + sensorName + HardwareConstants.EQUAL));
			statusArray = statusArrayBuilder.toString().replaceAll(HardwareConstants.NEW_LINE, HardwareConstants.PIPE);
		}
		return statusArray;
	}

	/**
	 * Retrieve States Asserted entry line and reformat the entry code to deduce the
	 * correct status of the given sensor
	 * Look for all states listed (States Asserted and Assertion Events)
	 * which are located before the "Assertions Enabled" entry
	 * @param sensorName
	 * @param matcher
	 * @return
	 */
	public static String processOemSpecific(String sensorName, String sensorEntry) {
		if (sensorName.isEmpty()) {
			Pattern patternSensorId = Pattern.compile(SENSOR_ID_REGEX, Pattern.MULTILINE);
			sensorName = checkPatternAndReturnDelimitedString(sensorEntry, patternSensorId, ":", HardwareConstants.OPENING_PARENTHESIS);
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
		return HardwareConstants.EMPTY;
	}

	/**
	 * Process the raw result of the FRU command and return the list of good FRU list and poor FRU list
	 * @param fruResult
	 * @return
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
			String fruID = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_FRUID, HardwareConstants.COLON, HardwareConstants.EMPTY).trim();
			// retrieve the ID between brackets ==> 0
			fruID = checkPatternAndReturnDelimitedString(fruID, PATTERN_BTW_BRACKETS, HardwareConstants.OPENING_PARENTHESIS, HardwareConstants.CLOSING_PARENTHESIS)
					.replace("ID ", HardwareConstants.EMPTY)
					.trim();
			String fruVendor = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_VENDOR, HardwareConstants.COLON, HardwareConstants.EMPTY).trim();
			String fruModel = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_MODEL, HardwareConstants.COLON, HardwareConstants.EMPTY).trim();
			String fruSN = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_SERIAL, HardwareConstants.COLON, HardwareConstants.EMPTY).trim();
			String boardVendor = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_BORD_VENDOR, HardwareConstants.COLON, HardwareConstants.EMPTY).trim();
			String boardModel = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_BORD_MODEL, HardwareConstants.COLON, HardwareConstants.EMPTY).trim();
			String boardSN = checkPatternAndReturnDelimitedString(fruEntry, PATTERN_BORD_SERIAL, HardwareConstants.COLON, HardwareConstants.EMPTY).trim();

			String fruEntryResult = String.format("FRU;%s;%s;%s", fruVendor,fruModel,fruSN);
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

			fruList.add(fruID + HardwareConstants.SEMICOLON + fruEntryResult);
		}
		ipmiTable.put(GOOD_LIST, goodFruList);
		ipmiTable.put(POOR_LIST, poorFruList);
		ipmiTable.put(FRU_LIST, fruList);
		return ipmiTable;
	}

	/**
	 * Reformat the ipmitoolSdr list so we have one line per sdr entry Remove lines
	 * that starts with BMC req and --
	 *
	 * @param sdrResult
	 * @return
	 */
	public static String cleanSensorCommandResult(String sdrResult) {
		if (sdrResult == null || sdrResult.isEmpty()) {
			return sdrResult;
		}

		// exclude rows that start with "^BMC req" and "-- "
		// in order to differentiate blocs of sensorID and the empty lines that will be
		// created by the replace operation
		sdrResult = PATTERN_BMC_REQ.matcher(sdrResult).replaceAll(HardwareConstants.EMPTY);
		sdrResult = sdrResult.replace(HardwareConstants.SEMICOLON, HardwareConstants.COMMA);
		sdrResult = sdrResult.replace(HardwareConstants.NEW_LINE, HardwareConstants.SEMICOLON);
		sdrResult = sdrResult.replace(";;", HardwareConstants.NEW_LINE);

		return sdrResult;
	}

}
