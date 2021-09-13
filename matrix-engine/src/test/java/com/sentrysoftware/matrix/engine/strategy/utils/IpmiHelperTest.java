package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;

class IpmiHelperTest {


	@Test
	void testIpmiTranslateFromWmi() {
		List<List<String>> wmiComputerSystem = new ArrayList<>();
		List<List<String>> wmiNumericSensors = new ArrayList<>();
		List<List<String>> wmiDiscreteSensors = new ArrayList<>();

		wmiComputerSystem.add(Arrays.asList("value0", "value1", "value2"));
		List<List<String>> expectedResult = new ArrayList<>();
		expectedResult.add(
				Arrays.asList(
						"FRU",
						"value2",
						"value1",
						"value0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Temperature sensor in Celsius.
		String description = "sensorName(sensorId):description for deviceId";
		wmiNumericSensors.add(Arrays.asList("2", "22.0", description, "LowerThresholdCritical", "LowerThresholdNonCritical", "2", "0", "40", "50"));
		expectedResult.add(
				Arrays.asList(
						"Temperature",
						"sensorId",
						"sensorName",
						"deviceId",
						"22.0",
						"50.0",
						"40.0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Temperature sensor in Farenheit.
		wmiNumericSensors.add(Arrays.asList("3", "50.0", description, "LowerThresholdCritical", "LowerThresholdNonCritical", "2", "0", "32", "50"));
		expectedResult.add(
				Arrays.asList(
						"Temperature",
						"sensorId",
						"sensorName",
						"deviceId",
						"10.0",
						"10.0",
						"0.0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Temperature sensor in Kelvin.
		wmiNumericSensors.add(Arrays.asList("4", "283.15", description, "LowerThresholdCritical", "LowerThresholdNonCritical", "2", "0", "300.15", "320.15"));
		expectedResult.add(
				Arrays.asList(
						"Temperature",
						"sensorId",
						"sensorName",
						"deviceId",
						"10.0",
						"47.0",
						"27.0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Fan sensor.
		wmiNumericSensors.add(Arrays.asList("19", "20", description, "30", "32", "5", "1"));
		expectedResult.add(
				Arrays.asList(
						"Fan",
						"sensorId",
						"sensorName",
						"deviceId",
						"200.0",
						"320.0",
						"300.0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Voltage sensor.
		wmiNumericSensors.add(Arrays.asList("5", "2", description, "4", "first threshold", "3", "2", "3", "second threshold"));
		expectedResult.add(
				Arrays.asList(
						"Voltage",
						"sensorId",
						"sensorName",
						"deviceId",
						"200000.0",
						"400000.0",
						"300000.0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Current sensor.
		wmiNumericSensors.add(Arrays.asList("6", "20", description, "40", "first threshold", "4", "0", "30", "second threshold"));
		expectedResult.add(
				Arrays.asList(
						"Current",
						"sensorId",
						"sensorName",
						"deviceId",
						"20.0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Power consumption sensor.
		wmiNumericSensors.add(Arrays.asList("7", "20", description, "40", "first threshold", "1", "0", "30", "40"));
		expectedResult.add(
				Arrays.asList(
						"PowerConsumption",
						"sensorId",
						"sensorName",
						"deviceId",
						"20.0",
						"40.0",
						"30.0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// EnergyUsage sensor.
		wmiNumericSensors.add(Arrays.asList("8", "3600000", description, "40", "first threshold", "1", "0", "7200000", "10800000"));
		expectedResult.add(
				Arrays.asList(
						"EnergyUsage",
						"sensorId",
						"sensorName",
						"deviceId",
						"1.0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Discrete sensor.
		description = "sensorName(sensorId):description for deviceType deviceId";
		wmiDiscreteSensors.add(Arrays.asList("OEM State,Value=0001", description));
		expectedResult.add(
				Arrays.asList(
						"deviceType",
						"deviceId",
						"deviceType deviceId",
						"",
						"",
						"",
						"sensorName=0x0100"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		description = "sensorName(sensorId):description for deviceType deviceId";
		wmiDiscreteSensors.add(Arrays.asList("OEM State.,Value=State Asserted=State Deasserted=Deasserted", description));
		expectedResult.set(expectedResult.size() - 1,
				Arrays.asList(
						"deviceType",
						"deviceId",
						"deviceType deviceId",
						"",
						"",
						"",
						"sensorName=0x0100|sensorName=OEM State|sensorName=Value=1=0=0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Absent device.
		wmiDiscreteSensors.add(Arrays.asList("Value=Device Removed/Device Absent", description));
		expectedResult.remove(expectedResult.size() - 1);
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Sensor alone.
		description = "sensorName(sensorId):description for deviceId";
		wmiNumericSensors = Collections.singletonList(Arrays.asList("2", "22.0", description, "LowerThresholdCritical", "LowerThresholdNonCritical", "2", "0", "40", "50"));
		expectedResult = Collections.singletonList(
				Arrays.asList(
						"Temperature",
						"sensorId",
						"sensorName",
						"deviceId",
						"22.0",
						"50.0",
						"40.0"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(new ArrayList<>(), wmiNumericSensors, new ArrayList<>()));

		// Discrete sensor alone.
		description = "sensorName(sensorId):description for deviceType deviceId";
		wmiDiscreteSensors = Collections.singletonList(Arrays.asList("OEM State,Value=0001", description));
		expectedResult = Collections.singletonList(
				Arrays.asList(
						"deviceType",
						"deviceId",
						"deviceType deviceId",
						"",
						"",
						"",
						"sensorName=0x0100"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(new ArrayList<>(), new ArrayList<>(), wmiDiscreteSensors));
	}

	@Test
	void testConvertFromFahrenheitToCelsius() {
		double fahrenheit = 50.0;
		assertEquals(10.0, IpmiHelper.convertFromFahrenheitToCelsius(fahrenheit));
	}

	@Test
	void testConvertFromKelvinToCelsius() {
		double kelvin = 283.15;
		assertEquals(10, IpmiHelper.convertFromKelvinToCelsius(kelvin));
	}

	@Test
	void testProcessFruResult() {
		String fruCommandResult = ResourceHelper.getResourceAsString("/data/IpmiFruTest", this.getClass());
		Map<String, List<String>> expectedMap = Map.of("goodList",
				Arrays.asList("FRU;FUJITSU;PRIMERGY RX300 S7;YLAR004219"), "poorList",
				Arrays.asList("FRU;FUJITSU;D2939;39159317", "FRU;FUJITSU;PRIMERGY RX300 S7;", "FRU;FUJITSU;D2939;39159317"));
		Map<String, List<String>> processFruResult = IpmiHelper.processFruResult(fruCommandResult);
		assertEquals(expectedMap.get("goodList"), processFruResult.get("goodList"));
		assertEquals(expectedMap.get("poorList"), processFruResult.get("poorList"));

	}

	@Test
	void testcleanSensorCommandResult() {
		String line = "line1;line1\nBMC req: line 2\nline3\n\nline3\n--ine4\nline5\nB line 6\n- line 7";
		String expected = "line1,line1\n" + "line3\n" + "line3\n" + "line5;B line 6;- line 7";
		assertEquals(expected, IpmiHelper.cleanSensorCommandResult(line));
		assertEquals(null, IpmiHelper.cleanSensorCommandResult(null));
		assertEquals("", IpmiHelper.cleanSensorCommandResult(""));
	}

	@Test
	void testIpmiAddHardwareSensorInfo() {
		String sensorInfo = "Sensor ID              : Ambient (0x1)\n"
				+ " Entity ID             : 39.0 (External Environment)\n"
				+ " Sensor Type (Threshold)  : Temperature (0x01)\n"
				+ " Sensor Reading        : 16.500 (+/- 0) degrees C\n" + " Status                : ok\n" + "\n"
				+ "Sensor ID              : Systemboard 1 (0x2)\n" + " Entity ID             : 7.0 (System Board)\n"
				+ " Sensor Type (Threshold)  : Temperature (0x01)\n" + " Sensor Reading        : 17 (+/- 0) degrees C\n"
				+ " Status                : ok\n" + "\n" + "Sensor ID              : P3V_BAT_SCALED (0x0)\n"
				+ " Entity ID             : 7.0 (System Board)\n" + " Sensor Type (Analog)  : Voltage\n"
				+ " Sensor Reading        : 3.023 (+/- 0.007) Volts\n" + " Status                : ok\n"
				+ " Upper critical        : 3.089\n" + " Lower non-recoverable : 2.706\n"
				+ " Lower critical        : 2.798\n" + "\n" + "Sensor ID              : FAN1 SYS (0x20)\n"
				+ " Entity ID             : 29.0 (Fan Device)\n" + " Sensor Type (Threshold)  : Fan (0x04)\n"
				+ " Sensor Reading        : 4680 (+/- 30) RPM\n" + " Status                : ok\n" + "\n"
				+ "Sensor ID              : P12V_CUR_SENS (0x3f)\n" + " Entity ID             : 7.0 (System Board)\n"
				+ " Sensor Type (Analog)  : Current\r\n" + " Sensor Reading        : 9.240 (+/- 0) Amps\n"
				+ " Status                : ok\n" + " Upper non-recoverable : 52.580\n"
				+ " Upper critical        : 49.060\n" + " Upper non-critical    : 45.540\n" + "\n"
				+ "Sensor ID              : CPU1 Power (0x27)\n" + " Entity ID             : 3.0 (Processor)\n"
				+ " Sensor Type (Threshold)  : Other (0x0b)\n" + " Sensor Reading        : 20 (+/- 0) Watts\n"
				+ " Assertions Enabled    :";
		sensorInfo = IpmiHelper.cleanSensorCommandResult(sensorInfo);
		List<String> ipmiTable = new ArrayList<String>(Arrays.asList("Line1", "Line2"));
		List<String> expected = new ArrayList<String>(Arrays.asList("Line1", "Line2",
				"Temperature;1;Ambient;External Environment 0;16.500;;",
				"Temperature;2;Systemboard 1;System Board 0;17;;",
				"Voltage;0;P3V_BAT_SCALED;System Board 0;3023.0;2798.0;3089.0", "Fan;20;FAN1 SYS;Fan Device 0;4680;;",
				"Current;3f;P12V_CUR_SENS;System Board 0;9.240", "PowerConsumption;27;CPU1 Power;Processor 0;20"));
		assertEquals(expected, IpmiHelper.ipmiAddHardwareSensorInfo(sensorInfo, ipmiTable));
	}

	@Test
	void testProcessOemSpecific() {
		// Classic use case
		String sensorEntry = "Sensor ID : DDR3_P2_D1_INFO (0x23)\n" + " Entity ID : 8.6 (Memory Module)\n"
				+ " Sensorm Type (Discrete): Memory\n" + " Sensor Reading : 0h\n"
				+ " Event Message Control : Per-threshold\n" + " States Asserted : 0x181 OEM Specific\n"
				+ " Assertion Events : 0x181 OEM Specific\n" + " Assertions Enabled : 0x7fff OEM Specific\n"
				+ " Deassertions Enabled : 0x7fff OEM Specific";
		String expected = "DDR3_P2_D1_INFO=0x8181";
		assertEquals(expected, IpmiHelper.processOemSpecific("DDR3_P2_D1_INFO", sensorEntry));
		// Try to guess the sensorName
		assertEquals(expected, IpmiHelper.processOemSpecific("", sensorEntry));

		// Without OEM specific
		sensorEntry = "Sensor ID              : Systemboard 1 (0x2)\n" + " Entity ID             : 7.0 (System Board)\n"
				+ " Sensor Type (Threshold)  : Temperature (0x01)\n" + " Sensor Reading        : 17 (+/- 0) degrees C\n"
				+ " Status                : ok\n" + " Positive Hysteresis   : 1.000\n"
				+ " Negative Hysteresis   : 1.000\n" + " Minimum sensor range  : Unspecified\n"
				+ " Maximum sensor range  : Unspecified\n" + " Event Message Control : Per-threshold";
		assertEquals("", IpmiHelper.processOemSpecific("Systemboard 1", sensorEntry));
		// Without OEM specific && without sensorName parameter
		assertEquals("", IpmiHelper.processOemSpecific("", ""));
		sensorEntry = "" + " Entity ID             : 7.0 (System Board)\n"
				+ " Sensor Type (Threshold)  : Temperature (0x01)\n" + " Sensor Reading        : 17 (+/- 0) degrees C\n"
				+ " Status                : ok\n" + " Positive Hysteresis   : 1.000\n"
				+ " Negative Hysteresis   : 1.000\n" + " Minimum sensor range  : Unspecified\n"
				+ " Maximum sensor range  : Unspecified\n" + " Event Message Control : Per-threshold";
		assertEquals("", IpmiHelper.processOemSpecific("", sensorEntry));
		// without sensorName
		sensorEntry = "" + " Entity ID : 8.6 (Memory Module)\n" + " Sensor Type (Discrete): Memory\n"
				+ " Sensor Reading : 0h\n" + " Event Message Control : Per-threshold\n"
				+ " States Asserted : 0x181 OEM Specific\n" + " Assertion Events : 0x181 OEM Specific\n"
				+ " Assertions Enabled : 0x7fff OEM Specific\n" + " Deassertions Enabled : 0x7fff OEM Specific";
		assertEquals("=0x8181", IpmiHelper.processOemSpecific("", sensorEntry));

	}

	@Test
	void testCheckPatternAndReturnDelimitedString() {
		// Classic case
		Pattern sensorIdPattern = Pattern.compile("^Sensor ID.*", Pattern.MULTILINE);
		String sensorEntry = "Sensor ID : DDR3_P2_D1_INFO (0x23)\n" + " Entity ID : 8.6 (Memory Module)\n"
				+ " Sensor Type (Discrete): Memory\n" + " Sensor Reading : 0h\n"
				+ " Event Message Control : Per-threshold\n" + " States Asserted : 0x181 OEM Specific\n"
				+ " Assertion Events : 0x181 OEM Specific\n" + " Assertions Enabled : 0x7fff OEM Specific\n"
				+ " Deassertions Enabled : 0x7fff OEM Specific";
		String sensorName = IpmiHelper.checkPatternAndReturnDelimitedString(sensorEntry, sensorIdPattern, ":",
				"(");
		assertEquals("DDR3_P2_D1_INFO", sensorName);

		// other pattern
		String test = "blabla: 0x111 (xyz) 222 aaa";
		Pattern testPattern = Pattern.compile("^blabla.*", Pattern.MULTILINE);
		assertEquals("xyz", IpmiHelper.checkPatternAndReturnDelimitedString(test, testPattern,
				"(", ")"));

		// pattern doesn't match
		assertEquals("", IpmiHelper.checkPatternAndReturnDelimitedString(test, sensorIdPattern,
				"(", ")"));

		// pattern match but leftlimits don't match
		assertEquals("", IpmiHelper.checkPatternAndReturnDelimitedString(test,
				Pattern.compile("^blabla.*", Pattern.MULTILINE), "-", ")"));
		assertEquals("", IpmiHelper.checkPatternAndReturnDelimitedString(test,
				Pattern.compile("^blabla.*", Pattern.MULTILINE), "-", "-"));
		assertEquals("", IpmiHelper.checkPatternAndReturnDelimitedString(test,
				Pattern.compile("^blabla.*", Pattern.MULTILINE), ")", "-"));
		// empty limit => return the original string if it matches the pattern
		assertEquals(test, IpmiHelper.checkPatternAndReturnDelimitedString(test,
				Pattern.compile("^blabla.*", Pattern.MULTILINE), "", ""));
		assertEquals(test, IpmiHelper.checkPatternAndReturnDelimitedString(test,
				Pattern.compile("^blabla.*", Pattern.MULTILINE), null, null));
		assertEquals(test, IpmiHelper.checkPatternAndReturnDelimitedString(test,
				Pattern.compile("^blabla.*", Pattern.MULTILINE), "", null));
		// empty right limit => go to the end
		assertEquals("0x111 (xyz) 222 aaa", IpmiHelper.checkPatternAndReturnDelimitedString(test,
				Pattern.compile("^blabla.*", Pattern.MULTILINE), ":", ""));
		assertEquals("0x111 (xyz) 222 aaa", IpmiHelper.checkPatternAndReturnDelimitedString(test,
				Pattern.compile("^blabla.*", Pattern.MULTILINE), ":", null));
		// empty left limit => start from the beginning
		assertEquals("blabla", IpmiHelper.checkPatternAndReturnDelimitedString(test,
				Pattern.compile("^blabla.*", Pattern.MULTILINE), "", ":"));
		assertEquals("blabla", IpmiHelper.checkPatternAndReturnDelimitedString(test,
				Pattern.compile("^blabla.*", Pattern.MULTILINE), null, ":"));
	}

	@Test
	void testGetStatusArray() {
		String sensorEntry = "Sensor ID              : LED_P2_FAULT (0x55)\n"
				+ " Entity ID             : 7.8 (System Board)\n" + " Sensor Type (Discrete): Platform Alert\n"
				+ " Sensor Reading        : 0h\n" + " Event Message Control : Per-threshold\n"
				+ " States Asserted       : 0x81   OEM Specific\n" + " Assertion Events      : 0x81   OEM Specific\n"
				+ " Assertions Enabled    : 0x3ff  OEM Specific";
		String actual = IpmiHelper.getSensorStatusArray(sensorEntry, "LED_P2_FAULT");
		String expected = "LED_P2_FAULT=0x8081";
		assertEquals(expected, actual);
		sensorEntry = "Sensor ID              : HDD_BP_PRS (0x40)\n"
				+ " Entity ID             : 15.0 (Drive Backplane)\n" + " Sensor Type (Discrete): Entity Presence\n"
				+ " Sensor Reading        : 0h\n" + " Event Message Control : Per-threshold\n"
				+ " States Asserted       : Availability State\n" + " Assertion Events      : Availability State\n"
				+ "                         [Device Present]\n" + " Assertions Enabled    : Availability State\n"
				+ "                         [Device Absent]\n" + "                         [Device Present]";
		actual = IpmiHelper.getSensorStatusArray(sensorEntry, "HDD_BP_PRS");
		expected = "HDD_BP_PRS=Device Present";
		assertEquals(expected, actual);

		sensorEntry = "Sensor ID              : HDD_BP_PRS (0x40)\n"
				+ " Entity ID             : 15.0 (Drive Backplane)\n" + " Sensor Type (Discrete): Entity Presence\n"
				+ " Sensor Reading        : 0h\n" + " Event Message Control : Per-threshold\n"
				+ " States Asserted       : Availability State\n" + " Assertion Events      : Availability State\n"
				+ "                         [Device Present]\n" + "                         [Device Present2]\n"
				+ " Assertions Enabled    : Availability State\n" + "                         [Device Absent]\n"
				+ "                         [Device Present]";
		actual = IpmiHelper.getSensorStatusArray(sensorEntry, "Int. Health LED");
		expected = "Int. Health LED=Device Present|Int. Health LED=Device Present2";
		assertEquals(expected, actual);

		sensorEntry = "Sensor ID              : HDD_BP_PRS (0x40)\n"
				+ " Entity ID             : 15.0 (Drive Backplane)\n" + " Sensor Type (Discrete): Entity Presence\n"
				+ " Sensor Reading        : 0h\n" + " Event Message Control : Per-threshold\n"
				+ " States Asserted       : Availability State\n" + "                         [Device Present11]\n"
				+ "                         [Device Present111]\n" + "                         [Device Present211]\n"
				+ " Assertion Events      : Availability State\n" + "                         [Device Present]\n"
				+ "                         [Device Present1]\n" + "                         [Device Present2]\n"
				+ " Assertions Enabled    : Availability State\n" + "                         [Device Absent]\n"
				+ "                         [Device Present]";
		actual = IpmiHelper.getSensorStatusArray(sensorEntry, "HDD_BP_PRS");
		expected = "HDD_BP_PRS=Device Present11|HDD_BP_PRS=Device Present111|HDD_BP_PRS=Device Present211|HDD_BP_PRS=Device Present|HDD_BP_PRS=Device Present1|HDD_BP_PRS=Device Present2";
		assertEquals(expected, actual);
	}

	@Test
	void testGetTEmperatureFromSensor() {
		// th1 = Upper non-critical && th2 = Upper critical
		String sensorTemperature = "Sensor ID              : FM_TEMP_SENS_IO (0x2f)\n"
				+ " Entity ID             : 7.0 (System Board)\n" + " Sensor Type (Analog)  : Temperature\n"
				+ " Sensor Reading        : 25 (+/- 0) degrees C\n" + " Status                : ok\n"
				+ " Upper non-recoverable : 55.000\n" + " Upper critical        : 45.000\n"
				+ " Upper non-critical    : 40.000";
		String actual = IpmiHelper.getTemperatureFromSensor(sensorTemperature, "FM_TEMP_SENS_IO", "2f",
				"System Board 0", "25");
		String expected = "Temperature;2f;FM_TEMP_SENS_IO;System Board 0;25;40.000;45.000";
		assertEquals(expected, actual);
		// th1 = Upper non-critical && th2 = non-recoverable
		sensorTemperature = "Sensor ID              : FM_TEMP_SENS_IO (0x2f)\n"
				+ " Entity ID             : 7.0 (System Board)\n" + " Sensor Type (Analog)  : Temperature\n"
				+ " Sensor Reading        : 25 (+/- 0) degrees C\n" + " Status                : ok\n"
				+ " Upper non-recoverable : 55.000\n" + " Upper critical        : - \n"
				+ " Upper non-critical    : 40.000";
		actual = IpmiHelper.getTemperatureFromSensor(sensorTemperature, "FM_TEMP_SENS_IO", "2f", "System Board 0",
				"25");
		expected = "Temperature;2f;FM_TEMP_SENS_IO;System Board 0;25;40.000;55.000";
		assertEquals(expected, actual);
		// th1 = - && th2 = Upper non-recoverable
		sensorTemperature = "Sensor ID              : FM_TEMP_SENS_IO (0x2f)\n"
				+ " Entity ID             : 7.0 (System Board)\n" + " Sensor Type (Analog)  : Temperature\n"
				+ " Sensor Reading        : 25 (+/- 0) degrees C\n" + " Status                : ok\n"
				+ " Upper non-recoverable : 55.000\n" + " Upper critical        : - \n" + " Upper non-critical    : -";
		actual = IpmiHelper.getTemperatureFromSensor(sensorTemperature, "FM_TEMP_SENS_IO", "2f", "System Board 0",
				"25");
		expected = "Temperature;2f;FM_TEMP_SENS_IO;System Board 0;25;;55.000";
		assertEquals(expected, actual);
		// th1 = - && th2 = -
		sensorTemperature = "Sensor ID              : FM_TEMP_SENS_IO (0x2f)\n"
				+ " Entity ID             : 7.0 (System Board)\n" + " Sensor Type (Analog)  : Temperature\n"
				+ " Sensor Reading        : 25 (+/- 0) degrees C\n" + " Status                : ok\n"
				+ " Upper non-recoverable : - \n" + " Upper critical        : - \n" + " Upper non-critical    : -";
		actual = IpmiHelper.getTemperatureFromSensor(sensorTemperature, "FM_TEMP_SENS_IO", "2f", "System Board 0",
				"25");
		expected = "Temperature;2f;FM_TEMP_SENS_IO;System Board 0;25;;";
		assertEquals(expected, actual);
		// th1 = - && th2 = -
		sensorTemperature = "Sensor ID              : FM_TEMP_SENS_IO (0x2f)\n"
				+ " Entity ID             : 7.0 (System Board)\n" + " Sensor Type (Analog)  : Temperature\n"
				+ " Sensor Reading        : 25 (+/- 0) degrees C\n" + " Status                : ok";
		actual = IpmiHelper.getTemperatureFromSensor(sensorTemperature, "FM_TEMP_SENS_IO", "2f", "System Board 0",
				"25");
		expected = "Temperature;2f;FM_TEMP_SENS_IO;System Board 0;25;;";
		assertEquals(expected, actual);

		String sensorVoltage = "Sensor ID              : P1V5_DDR3_P1 (0xa)\n"
				+ " Entity ID             : 3.1 (Processor)\n" + " Sensor Type (Analog)  : Voltage\n"
				+ " Sensor Reading        : 1.513 (+/- 0.005) Volts\n" + " Status                : ok\n"
				+ " Upper non-recoverable : 1.562\n" + " Upper non-critical        : 1.562\n"
				+ " Lower non-recoverable : 0.000\n" + " Lower critical        : 1.407\n";
		actual = IpmiHelper.getVoltageFromSensor(sensorVoltage, "P1V5_DDR3_P1", "a", "Processor 1", "1.513");
		expected = "Voltage;a;P1V5_DDR3_P1;Processor 1;1513.0;1407.0;1562.0";
		assertEquals(expected, actual);

		sensorVoltage = "Sensor ID              : P1V5_DDR3_P1 (0xa)\n" + " Entity ID             : 3.1 (Processor)\n"
				+ " Sensor Type (Analog)  : Voltage\n" + " Sensor Reading        : 1.513 (+/- 0.005) Volts\n"
				+ " Status                : ok\n" + " Upper non-recoverable : 1.562\n"
				+ " Upper critical        : 1.562\n" + " Lower non-recoverable : 0.000\n"
				+ " Lower non-critical        : 1.407\n";
		actual = IpmiHelper.getVoltageFromSensor(sensorVoltage, "P1V5_DDR3_P1", "a", "Processor 1", "1.513");
		expected = "Voltage;a;P1V5_DDR3_P1;Processor 1;1513.0;1407.0;1562.0";
		assertEquals(expected, actual);

		sensorVoltage = "Sensor ID              : P1V5_DDR3_P1 (0xa)\n" + " Entity ID             : 3.1 (Processor)\n"
				+ " Sensor Type (Analog)  : Voltage\n" + " Sensor Reading        : 1.513 (+/- 0.005) Volts\n"
				+ " Status                : ok\n" + " Upper non-recoverable : 1.562\n" + " Upper critical        : -\n"
				+ " Lower non-recoverable : 0.000\n" + " Lower critical        : -\n";
		actual = IpmiHelper.getVoltageFromSensor(sensorVoltage, "P1V5_DDR3_P1", "a", "Processor 1", "1.513");
		expected = "Voltage;a;P1V5_DDR3_P1;Processor 1;1513.0;;1562.0";
		assertEquals(expected, actual);

		sensorVoltage = "Sensor ID              : P1V5_DDR3_P1 (0xa)\n" + " Entity ID             : 3.1 (Processor)\n"
				+ " Sensor Type (Analog)  : Voltage\n" + " Sensor Reading        : 1.513 (+/- 0.005) Volts\n"
				+ " Status                : ok\n"

		;
		actual = IpmiHelper.getVoltageFromSensor(sensorVoltage, "P1V5_DDR3_P1", "a", "Processor 1", "1.513");
		expected = "Voltage;a;P1V5_DDR3_P1;Processor 1;1513.0;;";
		assertEquals(expected, actual);

		String sensorFan = "Sensor ID              : FAN1 SYS (0x20)\n" + " Entity ID             : 29.0 (Fan Device)\n"
				+ " Sensor Type (Threshold)  : Fan (0x04)\n" + " Sensor Reading        : 4680 (+/- 30) RPM\n"
				+ " Status                : ok\n" + " Positive Hysteresis   : Unspecified\n"
				+ " Negative Hysteresis   : 240.000\n" + " Minimum sensor range  : Unspecified\n"
				+ " Maximum sensor range  : Unspecified\n" + " Event Message Control : Per-threshold\n"
				+ " Readable Thresholds   : Thresholds Fixed\n" + " Settable Thresholds   : Thresholds Fixed";
		actual = IpmiHelper.getFanFromSensor(sensorFan, "FAN1 SYS", "20", "Fan Device 0", "4680");
		expected = "Fan;20;FAN1 SYS;Fan Device 0;4680;;";
		assertEquals(expected, actual);

		sensorFan = "Sensor ID              : FAN1 SYS (0x20)\n" + " Entity ID             : 29.0 (Fan Device)\n"
				+ " Sensor Type (Threshold)  : Fan (0x04)\n" + " Sensor Reading        : 4680 (+/- 30) RPM\n"
				+ " Status                : ok\n" + " Lower non-critical        : 20\n"
				+ " Lower non-recoverable : 30\n" + " Lower critical        : 10\n";
		actual = IpmiHelper.getFanFromSensor(sensorFan, "FAN1 SYS", "20", "Fan Device 0", "4680");
		expected = "Fan;20;FAN1 SYS;Fan Device 0;4680;20;10";
		assertEquals(expected, actual);

		sensorFan = "Sensor ID              : FAN1 SYS (0x20)\n" + " Entity ID             : 29.0 (Fan Device)\n"
				+ " Sensor Type (Threshold)  : Fan (0x04)\n" + " Sensor Reading        : 4680 (+/- 30) RPM\n"
				+ " Status                : ok\n" + " Lower non-critical        : 20\n"
				+ " Lower non-recoverable : 30\n" + " Lower critical        : -\n";
		actual = IpmiHelper.getFanFromSensor(sensorFan, "FAN1 SYS", "20", "Fan Device 0", "4680");
		expected = "Fan;20;FAN1 SYS;Fan Device 0;4680;20;30";
		assertEquals(expected, actual);

		sensorFan = "Sensor ID              : FAN1 SYS (0x20)\n" + " Entity ID             : 29.0 (Fan Device)\n"
				+ " Sensor Type (Threshold)  : Fan (0x04)\n" + " Sensor Reading        : 4680 (+/- 30) RPM\n"
				+ " Status                : ok\n" + " Lower non-critical        : -\n" + " Lower non-recoverable : 30\n"
				+ " Lower critical        : 20\n";
		actual = IpmiHelper.getFanFromSensor(sensorFan, "FAN1 SYS", "20", "Fan Device 0", "4680");
		expected = "Fan;20;FAN1 SYS;Fan Device 0;4680;;20";
		assertEquals(expected, actual);
	}

	@Test
	void testAddSensorElementotDeviceList() {
		List<String> deviceList = new ArrayList<>();
		String sdrResult = "Sensor ID              : BATT 3.0V (0x12); Entity ID             : 40.0 (Battery); Sensor Type (Threshold)  : Voltage (0x02); Sensor Reading        : 3.210(+/- 0) Volts; Status                : ok; Nominal Reading       : 3.000; Positive Hysteresis   : 0.075; Negative Hysteresis   : 0.075\n"
				+ ""
				+ "Device ID              : RDSA;Entity ID              : 40.0 (Battery 40.0);Device Access Address  : 20h;Logical FRU Device     : 01h;Channel Number         : 0h\n"
				+ "";
		String deviceType = "Battery";
		String deviceId = "0";
		String entityId = "40.0";
		String statusArray = "blabla";
		List<String> fruList = new ArrayList<>(Arrays.asList("0;FUJITSU;D2939;39159317",
				"1;FUJITSU;PRIMERGY RX300 S7;YLAR004219", "2;FUJITSU;D2939;39159317,"));
		assertEquals(Arrays.asList("Battery;0;Battery 0;FUJITSU;PRIMERGY RX300 S7;YLAR004219;blabla"),
				IpmiHelper.addSensorElementToDeviceList(deviceList, sdrResult, deviceType, deviceId, entityId,
						statusArray, fruList));

		// second status
		sdrResult = "Sensor ID              : BATT 3.0V (0x12); Entity ID             : 40.0 (Battery); Sensor Type (Threshold)  : Voltage (0x02); Sensor Reading        : 3.210(+/- 0) Volts; Status                : ok; Nominal Reading       : 3.000; Positive Hysteresis   : 0.075; Negative Hysteresis   : 0.075\n"
				+ "Sensor ID              : iRMC (0xfe); Entity ID             : 6.1 (System Management Module); Sensor Type (Discrete): Unknown (0xee); Sensor Reading        : No Reading; Event Message Control : Per-threshold; OEM                   : 0;bad values\r\n"
				+ ""
				+ "Device ID              : RDSA;Entity ID              : 40.0 (Battery 40.0);Device Access Address  : 20h;Logical FRU Device     : 01h;Channel Number         : 0h\n"
				+ "";
		deviceType = "Battery";
		deviceId = "0";
		entityId = "40.0";
		statusArray = "blablaBattery2";
		assertEquals(Arrays.asList("Battery;0;Battery 0;FUJITSU;PRIMERGY RX300 S7;YLAR004219;blabla|blablaBattery2"),
				IpmiHelper.addSensorElementToDeviceList(deviceList, sdrResult, deviceType, deviceId, entityId,
						statusArray, fruList));

		sdrResult = "Sensor ID              : BATT 3.0V (0x12); Entity ID             : 40.0 (Battery); Sensor Type (Threshold)  : Voltage (0x02); Sensor Reading        : 3.210(+/- 0) Volts; Status                : ok; Nominal Reading       : 3.000; Positive Hysteresis   : 0.075; Negative Hysteresis   : 0.075\n"
				+ ""
				+ "Device ID              : RDSA;Entity ID              : 40.0 (Battery 40.0);Device Access Address  : 20h;Logical FRU Device     : 01h;Channel Number         : 0h\n"
				+ "";
		deviceType = "Fan";
		deviceId = "4";
		entityId = "240.4";
		statusArray = "blablaFan";
		assertEquals(
				Arrays.asList("Battery;0;Battery 0;FUJITSU;PRIMERGY RX300 S7;YLAR004219;blabla|blablaBattery2",
						"Fan;4;Fan 4;;;;blablaFan"),
				IpmiHelper.addSensorElementToDeviceList(deviceList, sdrResult, deviceType, deviceId, entityId,
						statusArray, fruList));

	}

	@Test
	void testProcessSdrRecords() {
		String sdrResult ="Sensor ID              : iRMC (0xfe)\n"
				+ " Entity ID             : 6.0 (System Management Module)\n"
				+ " Sensor Type (Discrete): Unknown (0xee)\n"
				+ "\n"
				+ "Sensor ID              : Ambient (0x38)\n"
				+ " Entity ID             : 39.0 (External Environment)\n"
				+ " Sensor Type (Discrete): Temperature (0x01)\n"
				+ " Sensor Reading        : 1h\n"
				+ " Event Message Control : Per-threshold\n"
				+ " States Asserted       : Temperature\n"
				+ "                         [Device Present]\n"
				+ " Assertions Enabled    : Temperature\n"
				+ "                         [Device Absent]\n"
				+ "                         [Device Present]\n"
				+ "\n"
				+ "Sensor ID              : Ambient (0x37)\n"
				+ " Entity ID             : 38.0 (External Environment2)\n"
				+ " Sensor Type (Discrete): Temperature (0x01)\n"
				+ " Sensor Reading        : 1h\n"
				+ " Event Message Control : Per-threshold\n"
				+ " States Asserted       : Temperature\n"
				+ "                         [Device Present]\n"
				+ "\n"
				+ "Sensor ID              : CPU1 (0x39)\n"
				+ " Sensor Type (Discrete): Processor (0x07)\n"
				+ " Sensor Reading        : 0h\n"
				+ " Event Message Control : Per-threshold\n"
				+ " States Asserted       : Processor\n"
				+ "                         [Presence detected]\n"
				+ "\n"
				+ "Sensor ID              : PSU (0x3d)\n"
				+ " Entity ID             : 19.0 (Power Unit)\n"
				+ " Sensor Type (Discrete): Power Unit (0x09)\n"
				+ " Sensor Reading        : No Reading\n"
				+ " Event Message Control : Per-threshold";
		Map<String, List<String>> fruMap = new HashMap<>();
		List<String> deviceList = new ArrayList<>();
		sdrResult = IpmiHelper.cleanSensorCommandResult(sdrResult);
		List<String> actual = IpmiHelper.processSdrRecords(sdrResult, fruMap, deviceList);
		assertEquals(Arrays.asList("External Environment;0;External Environment 0;;;;Ambient=Device Present"), actual);
	}

	@Test
	void testIpmiTranslateFromIpmitool() {
		String fru = "/data/IpmiFruBabbage";
		String sensor = "/data/IpmiSensorBabbage";
		String expected = "/data/ipmiProcessingResult";
		String fruResult = ResourceHelper.getResourceAsString(fru, this.getClass());
		String sensorResult = ResourceHelper.getResourceAsString(sensor, this.getClass());
		String expectedResult = ResourceHelper.getResourceAsString(expected, this.getClass());
		List<List<String>> result = new ArrayList<>();
		Stream.of(expectedResult.split("\n")).forEach(line -> result.add(Arrays.asList(line.split(";"))));
		assertEquals(result, IpmiHelper.ipmiTranslateFromIpmitool(fruResult, sensorResult));
	}
}
