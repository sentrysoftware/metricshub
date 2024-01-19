package com.sentrysoftware.metricshub.cli.service.converter;

import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import picocli.CommandLine;

/**
 * A converter class for converting a given privacy string to {@link SnmpConfiguration.Privacy}.
 */
public class SnmpPrivacyConverter implements CommandLine.ITypeConverter<SnmpConfiguration.Privacy> {

	/**
	 * Converts a given privacy string to {@link SnmpConfiguration.Privacy}
	 *
	 * @param privacy a given privacy string
	 * @return value of type {@link SnmpConfiguration.Privacy}
	 * @throws CommandLine.TypeConversionException if an error occurs during the conversion
	 */
	@Override
	public SnmpConfiguration.Privacy convert(final String privacy) {
		try {
			return SnmpConfiguration.Privacy.interpretValueOf(privacy);
		} catch (Exception e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
