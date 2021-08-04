package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;

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
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						"sensorName=0x0100"));
		assertEquals(expectedResult, IpmiHelper.ipmiTranslateFromWmi(wmiComputerSystem, wmiNumericSensors, wmiDiscreteSensors));

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
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
						HardwareConstants.EMPTY,
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
}
