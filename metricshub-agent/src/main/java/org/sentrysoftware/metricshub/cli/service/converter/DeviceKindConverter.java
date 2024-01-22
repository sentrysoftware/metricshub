package org.sentrysoftware.metricshub.cli.service.converter;

import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

/**
 * Converter for converting a string to a {@link DeviceKind}.
 * It uses the {@link DeviceKind#detect(String)} method for conversion.
 */
public class DeviceKindConverter implements ITypeConverter<DeviceKind> {

	/**
	 * Converts the given string to a {@link DeviceKind} using the {@link DeviceKind#detect(String)} method.
	 *
	 * @param type the string representation of the device kind
	 * @return the corresponding {@link DeviceKind} instance
	 * @throws TypeConversionException if an error occurs during conversion
	 */
	@Override
	public DeviceKind convert(@NonNull final String type) throws Exception {
		try {
			return DeviceKind.detect(type);
		} catch (Exception e) {
			throw new TypeConversionException(e.getMessage());
		}
	}
}
