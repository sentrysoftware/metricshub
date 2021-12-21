package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.target.TargetType;

import lombok.NonNull;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class TargetTypeConverter implements ITypeConverter<TargetType> {

	@Override
	public TargetType convert(@NonNull final String type) throws Exception {
		try {
			return TargetType.interpretValueOf(type);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}
}
