package org.sentrysoftware.metricshub.agent.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration.SnmpVersion;

/**
 * Custom deserializer for converting SNMP version values from JSON during deserialization.
 * It maps the JSON value to the corresponding {@link SnmpVersion} enum.
 */
public class SnmpVersionDeserializer extends JsonDeserializer<SnmpVersion> {

	/**
	 * Deserializes the SNMP version value from JSON and maps it to the {@link SnmpVersion} enum.
	 *
	 * @param parser JSON parser
	 * @param ctxt   Deserialization context
	 * @return The deserialized {@link SnmpVersion} value
	 * @throws IOException If an I/O error occurs during deserialization
	 */
	@Override
	public SnmpVersion deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return null;
		}

		try {
			return SnmpVersion.interpretValueOf(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}
}
