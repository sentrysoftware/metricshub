package com.sentrysoftware.metricshub.cli.service.converter;

import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import lombok.NonNull;
import picocli.CommandLine;

public class SnmpVersionConverter implements CommandLine.ITypeConverter<SnmpConfiguration.SnmpVersion> {

	/**
	 * Converts a given version string to {@link SnmpConfiguration.SnmpVersion}
	 *
	 * @param version a given version
	 * @return value of type {@link SnmpConfiguration.SnmpVersion}
	 */
	@Override
	public SnmpConfiguration.SnmpVersion convert(@NonNull final String version) {
		try {
			return SnmpConfiguration.SnmpVersion.interpretValueOf(version);
		} catch (Exception e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}