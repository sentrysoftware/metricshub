package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.host.HostType;
import lombok.NonNull;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class HostTypeConverter implements ITypeConverter<HostType> {

	@Override
	public HostType convert(@NonNull final String type) throws Exception {
		try {
			return HostType.interpretValueOf(type);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}
}
