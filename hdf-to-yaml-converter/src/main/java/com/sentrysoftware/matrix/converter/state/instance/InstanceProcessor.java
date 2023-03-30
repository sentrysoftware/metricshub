package com.sentrysoftware.matrix.converter.state.instance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

public class InstanceProcessor extends AbstractStateConverter {

    private static final Pattern PATTERN = Pattern.compile(
            "^\\s*([a-z]+)\\.discovery\\.instance\\.(parameteractivation\\.[a-z0-9]+|[a-z0-9]+)\\s*$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public boolean detect(String key, String value, JsonNode connector) {
        return value != null
                && key != null
                && PATTERN.matcher(key).matches();
    }

    @Override
    public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
        ObjectNode attributes = getOrCreateAttributes(key, connector);

        String attribute = getMappingAttribute(key);
        attributes.set(attribute, JsonNodeFactory.instance.textNode(ConversionHelper.performValueConversions(value)));
    }

    /**
	 * Extract the parameter name from the given key.<br><br>
	 *
	 * e.g. extract <b>DeviceID</b> from <b>Enclosure.Discovery.Instance.DeviceID</b>.<br>
	 * e.g. extract <b>ParameterActivation.Temperature</b> from <b>Enclosure.Discovery.Instance.ParameterActivation.Temperature</b>.
	 *
	 * @param key	The key from which the parameter name should be extracted.
	 *
	 * @return		The parameter name contained in the given key.
	 */
	String getParameter(final String key) {

		final Matcher matcher = PATTERN.matcher(key);

		//noinspection ResultOfMethodCallIgnored
		matcher.find();

		return matcher.group(2);
	}

    @Override
    protected Matcher getMatcher(String key) {
        return PATTERN.matcher(key);
    }
}