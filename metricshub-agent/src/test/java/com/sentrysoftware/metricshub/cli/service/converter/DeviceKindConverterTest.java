package com.sentrysoftware.metricshub.cli.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.TypeConversionException;

class DeviceKindConverterTest {

	@Test
	void testConvert() throws Exception {
		final DeviceKindConverter deviceKindConverter = new DeviceKindConverter();
		assertEquals(DeviceKind.WINDOWS, deviceKindConverter.convert("win"));
		assertThrows(TypeConversionException.class, () -> deviceKindConverter.convert("unknown"));
	}
}
