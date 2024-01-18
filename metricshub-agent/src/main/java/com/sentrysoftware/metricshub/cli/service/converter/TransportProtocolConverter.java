package com.sentrysoftware.metricshub.cli.service.converter;

import com.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

/**
 * Converter for {@link TransportProtocols} used in command-line argument parsing.
 * Implements {@link ITypeConverter}.
 */
public class TransportProtocolConverter implements ITypeConverter<TransportProtocols> {

	/**
	 * Converts the input String to a {@link TransportProtocols} enum value.
	 *
	 * @param transportProtocol The input String representing a transport protocol.
	 * @return The corresponding {@link TransportProtocols} enum value.
	 * @throws TypeConversionException If the conversion fails, typically due to an invalid input.
	 */
	@Override
	public TransportProtocols convert(final String transportProtocol) throws Exception {
		try {
			return TransportProtocols.interpretValueOf(transportProtocol);
		} catch (Exception e) {
			throw new TypeConversionException(e.getMessage());
		}
	}
}
