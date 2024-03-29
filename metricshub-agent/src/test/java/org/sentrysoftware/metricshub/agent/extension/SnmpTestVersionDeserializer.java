package org.sentrysoftware.metricshub.agent.extension;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

/**
 * Custom deserializer for converting SNMP version values from JSON during deserialization.
 * It maps the JSON value to the corresponding {@link SnmpVersion} enum.
 */
public class SnmpTestVersionDeserializer extends JsonDeserializer<SnmpTestConfiguration.SnmpVersion> {

	/**
	 * Deserializes the SNMP version value from JSON and maps it to the {@link SnmpVersion} enum.
	 *
	 * @param parser JSON parser
	 * @param ctxt   Deserialization context
	 * @return The deserialized {@link SnmpVersion} value
	 * @throws IOException If an I/O error occurs during deserialization
	 */
	@Override
	public SnmpTestConfiguration.SnmpVersion deserialize(JsonParser parser, DeserializationContext ctxt)
		throws IOException {
		if (parser == null) {
			return null;
		}

		try {
			return SnmpTestConfiguration.SnmpVersion.interpretValueOf(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}
}
