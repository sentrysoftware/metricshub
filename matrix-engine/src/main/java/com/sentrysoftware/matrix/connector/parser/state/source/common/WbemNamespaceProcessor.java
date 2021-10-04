package com.sentrysoftware.matrix.connector.parser.state.source.common;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.CLOSING_PARENTHESIS;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.OPENING_PARENTHESIS;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.SET_WBEM_NAMESPACE;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WbemNamespaceProcessor extends AbstractStateParser {

	private final Class<? extends Source> type;
	private final String typeValue;

	private static final Pattern WBEM_NAMESPACE_KEY_PATTERN = Pattern
			.compile("^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.wbemnamespace\\s*$", Pattern.CASE_INSENSITIVE);

	@Override
	protected Class<?> getType() {
		return type;
	}

	@Override
	protected String getTypeValue() {
		return typeValue;
	}

	@Override
	protected Matcher getMatcher(String key) {
		return WBEM_NAMESPACE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {

			final Source source = getSource(key, connector);
			source.getClass().getMethod(SET_WBEM_NAMESPACE, String.class).invoke(source, value);

		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

			throw new IllegalStateException(
					"WbemNamespaceProcessor parse: Cannot invoke "
						+ SET_WBEM_NAMESPACE
						+ OPENING_PARENTHESIS
						+ value
						+ CLOSING_PARENTHESIS
						+ " on "
						+ type.getSimpleName()
						+ " Source: "
						+ e.getMessage());
		}
	}
}