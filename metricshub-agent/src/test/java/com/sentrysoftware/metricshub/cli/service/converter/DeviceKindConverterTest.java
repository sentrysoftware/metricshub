package com.sentrysoftware.metricshub.cli.service.converter;

import static org.junit.jupiter.api.Assertions.*;

import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.TypeConversionException;

class DeviceKindConverterTest {

	@Test
	void testConvert() throws Exception {
		final DeviceKindConverter deviceKindConverter = new DeviceKindConverter();
		assertEquals(DeviceKind.WINDOWS, new DeviceKindConverter().convert("win"));
		assertThrows(TypeConversionException.class, () -> deviceKindConverter.convert("unknown"));
	}
}
