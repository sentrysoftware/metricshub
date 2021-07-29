package com.sentrysoftware.matrix.engine.strategy.utils;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

class PslUtilsTest {

	@Test
	void testPsl2JavaRegex() {

		// pslRegex is null
		assertEquals(EMPTY, PslUtils.psl2JavaRegex(null));

		// pslRegex is empty
		assertEquals(EMPTY, PslUtils.psl2JavaRegex(EMPTY));

		// pslRegex is not null and not empty, contains a '\' in a range
		assertEquals("[ab\\\\cd]", PslUtils.psl2JavaRegex("[ab\\cd]"));

		// pslRegex is not null and not empty, contains a '\' not in a range, next character is '<' or '>'
		assertEquals("ab\\bd\\be", PslUtils.psl2JavaRegex("ab\\<d\\>e"));

		// pslRegex is not null and not empty, contains a '\' not in a range, next character is special
		assertEquals("ab\\+", PslUtils.psl2JavaRegex("ab\\+"));

		// pslRegex is not null and not empty, contains a '\' not in a range, next character is regular
		assertEquals("abcd", PslUtils.psl2JavaRegex("ab\\cd"));

		// pslRegex is not null and not empty, ends with '\'
		assertEquals("ab\\", PslUtils.psl2JavaRegex("ab\\"));

		// pslRegex is not null and not empty, contains '(', ')', '|', '{' or '}'
		assertEquals("\\(\\)\\|\\{\\}", PslUtils.psl2JavaRegex("()|{}"));
	}

	@Test
	void testNthArgf() {

		// text is null
		assertEquals(EMPTY, PslUtils.nthArgf(null, null, null, null));

		// text is not null, selectColumns is null
		assertEquals(EMPTY, PslUtils.nthArgf(EMPTY, null, null, null));

		// text is not null, selectColumns is not null, separators is null
		assertEquals(EMPTY, PslUtils.nthArgf(EMPTY, EMPTY, null, null));

		// selectColumns is not null, separators is not null, text is empty
		assertEquals(EMPTY, PslUtils.nthArgf(EMPTY, EMPTY, EMPTY, null));

		// text is not null and not empty, separators is not null, selectColumns is empty
		String text = "|OK|1";
		assertEquals(EMPTY, PslUtils.nthArgf(text, EMPTY, EMPTY, null));

		// text is not null and not empty, selectColumns is not null and not empty, separators is empty
		String selectColumns = "3";
		assertEquals(EMPTY, PslUtils.nthArgf(text, selectColumns, EMPTY, null));

		// text is not null and not empty, separators is not null and not empty, resultSeparator is not null,
		// selectColumns starts with '-'
		String separators = "|";
		selectColumns = "-2";
		assertEquals(" OK", PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));

		// text is not null and not empty, separators is not null and not empty, resultSeparator is not null,
		// selectColumns ends with '-'
		selectColumns = "2-";
		assertEquals("OK 1", PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));

		// text is not null and not empty, separators is not null and not empty, resultSeparator is not null,
		// selectColumns is '1-3'
		selectColumns = "1-3";
		assertEquals(" OK 1", PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));

		// text is not null and not empty, separators is not null and not empty, resultSeparator is not null,
		// selectColumns is invalid
		selectColumns = "foo-bar";
		assertEquals(EMPTY, PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));
		selectColumns = "3-4";
		assertEquals("1", PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));
		selectColumns = "3-1";
		assertEquals(EMPTY, PslUtils.nthArgf(text, selectColumns, separators, WHITE_SPACE));

		// text contains "\n" in columns
		text = "OK|OK|\n|WARN|\nALARM";
		selectColumns = "1-";
		assertEquals("OK\nOK\n\n\nWARN\n\nALARM", PslUtils.nthArgf(text, selectColumns, separators, NEW_LINE));

		assertEquals("a,b,d", PslUtils.nthArgf("a,b,c,d,e,", "1,2,4", ",", ","));
		assertEquals("a,b,c,d,e,", PslUtils.nthArgf("a,b,c,d,e,", "1-", ",", ","));
		assertEquals("a,b,c", PslUtils.nthArgf("a,b,c,d,e,", "1-3", ",", ","));
		assertEquals("b,c,d", PslUtils.nthArgf("a,b,c,d,e,", "2-4", ",", ","));
		assertEquals("b", PslUtils.nthArgf("a,b,c,d,e,", "2", ",", ","));
		assertEquals("a,b,c,d,e,", PslUtils.nthArgf("a,b,c,d,e,", "1-50", ",", ","));
	}

	@Test
	void testNthArg() {

		// text contains "\n" in columns
		String text = "OK|OK|\n|WARN|\nALARM";
		String selectColumns = "1-";
		String separators = "|";
		assertEquals("OK\nOK\nWARN\nALARM", PslUtils.nthArg(text, selectColumns, separators, NEW_LINE));

		// selectColumns is "2-"
		selectColumns = "2-";
		assertEquals("OK\nWARN", PslUtils.nthArg(text, selectColumns, separators, NEW_LINE));

		assertEquals("OK\nWARN\nALARM", PslUtils.nthArg("OK|OK|\nOK|WARN|\nOK|ALARM", selectColumns, separators, NEW_LINE));

		assertEquals("a,b,c,d", PslUtils.nthArg("a,b,\nc,d,e,", "1-2", ",", ","));

		assertEquals("a,b,e,f", PslUtils.nthArg("a,b,c,d,\ne,f,g,h,", "-2", ",", ","));
		assertEquals("a,b,d", PslUtils.nthArg("a,b,c,d,e,", "1,2,4", ",", ","));
		assertEquals("a,b,c,d,e", PslUtils.nthArg("a,b,c,d,e,", "1-", ",", ","));
		assertEquals("a,b,c", PslUtils.nthArg("a,b,c,d,e,", "1-3", ",", ","));
		assertEquals("b,c,d", PslUtils.nthArg("a,b,c,d,e,", "2-4", ",", ","));
		assertEquals("b", PslUtils.nthArg("a,b,c,d,e,", "2", ",", ","));
		assertEquals("a,b,c,d,e", PslUtils.nthArg("a,b,c,d,e,", "1-50", ",", ","));
	}

	@Test
	void testFormatExtendedJSON() {
		assertThrows(IllegalArgumentException.class, () -> PslUtils.formatExtendedJSON(null, null));

		SourceTable sourceTable = SourceTable.builder().build();
		assertThrows(IllegalArgumentException.class, () -> PslUtils.formatExtendedJSON("", null));
		assertThrows(IllegalArgumentException.class, () -> PslUtils.formatExtendedJSON(null, sourceTable));
		assertThrows(IllegalArgumentException.class, () -> PslUtils.formatExtendedJSON("", sourceTable), "Empty row of values");

		String row = "val1,val2,val3";
		assertThrows(IllegalArgumentException.class, () -> PslUtils.formatExtendedJSON(row, sourceTable), "Empty SourceTable data: " + sourceTable);

		sourceTable.setRawData("source table raw data");
		assertEquals("{\n" +
				"\"Entry\":{\n" +
				"\"Full\":\"val1,val2,val3\",\n" +
				"\"Column(1)\":\"val1\",\n" +
				"\"Column(2)\":\"val2\",\n" +
				"\"Column(3)\":\"val3\",\n" +
				"\"Value\":source table raw data\n" +
				"}\n" +
				"}",
				PslUtils.formatExtendedJSON(row, sourceTable));
	}
	
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
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

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
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

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
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

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
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

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
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

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
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Current sensor.
		wmiNumericSensors.add(Arrays.asList("6", "20", description, "40", "first threshold", "4", "0", "30", "second threshold"));
		expectedResult.add(
				Arrays.asList(
						"Current",
						"sensorId",
						"sensorName",
						"deviceId",
						"20.0"));
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

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
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// EnergyUsage sensor.
		wmiNumericSensors.add(Arrays.asList("8", "3600000", description, "40", "first threshold", "1", "0", "7200000", "10800000"));
		expectedResult.add(
				Arrays.asList(
						"EnergyUsage",
						"sensorId",
						"sensorName",
						"deviceId",
						"1.0"));
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Discrete sensor.
		description = "sensorName(sensorId):description for deviceType deviceId";
		wmiDiscreteSensors.add(Arrays.asList("OEM State,Value=0001", description));
		expectedResult.add(
				Arrays.asList(
						"deviceType",
						"deviceId",
						"deviceType deviceId",
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						"sensorName=0x0100"));
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		description = "sensorName(sensorId):description for deviceType deviceId";
		wmiDiscreteSensors.add(Arrays.asList("OEM State.,Value=State Asserted=State Deasserted=Deasserted", description));
		expectedResult.set(expectedResult.size() - 1, 
				Arrays.asList(
						"deviceType",
						"deviceId",
						"deviceType deviceId",
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						"sensorName=0x0100|sensorName=OEM State|sensorName=Value=1=0=0"));
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

		// Absent device.
		wmiDiscreteSensors.add(Arrays.asList("OEM State,Valu=Device Removed/Device Absent", description));
		expectedResult.remove(expectedResult.size() - 1);
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

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
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(new ArrayList<>(), wmiNumericSensors, new ArrayList<>()));

		// Discrete sensor alone.
		description = "sensorName(sensorId):description for deviceType deviceId";
		wmiDiscreteSensors = Collections.singletonList(Arrays.asList("OEM State,Value=0001", description));
		expectedResult = Collections.singletonList(
				Arrays.asList(
						"deviceType",
						"deviceId",
						"deviceType deviceId",
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						"sensorName=0x0100"));
		assertEquals(expectedResult, PslUtils.ipmiTranslateFromWmi(new ArrayList<>(), new ArrayList<>(), wmiDiscreteSensors));
	}

	@Test
	void testIsNumeric() {
		String numericInt = "34";
		assertTrue(PslUtils.isNumeric(numericInt));

		String numericDouble = "34.0";
		assertTrue(PslUtils.isNumeric(numericDouble));

		String notNumeric = "a string";
		assertFalse(PslUtils.isNumeric(notNumeric));
	}

	@Test
	void testConvertFromFahrenheitToCelsius() {
		double fahrenheit = 50.0;
		assertEquals(10, PslUtils.convertFromFahrenheitToCelsius(fahrenheit));
	}

	@Test
	void testConvertFromKelvinToCelsius() {
		double kelvin = 283.15;
		assertEquals(10, PslUtils.convertFromKelvinToCelsius(kelvin));
	}
}