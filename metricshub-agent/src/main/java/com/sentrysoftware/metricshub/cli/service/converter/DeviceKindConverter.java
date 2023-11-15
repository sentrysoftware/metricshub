package com.sentrysoftware.metricshub.cli.service.converter;

import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import lombok.NonNull;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class DeviceKindConverter implements ITypeConverter<DeviceKind> {

	@Override
	public DeviceKind convert(@NonNull final String type) throws Exception {
		try {
			return DeviceKind.detect(type);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}
}
