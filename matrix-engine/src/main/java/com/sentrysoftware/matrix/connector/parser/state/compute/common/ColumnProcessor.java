package com.sentrysoftware.matrix.connector.parser.state.compute.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.CLOSING_PARENTHESIS;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.INTEGER_REGEX;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.OPENING_PARENTHESIS;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.SET_COLUMN;
import static org.springframework.util.Assert.isTrue;

@AllArgsConstructor
public class ColumnProcessor extends AbstractStateParser {

	private final Class<? extends Compute> type;
	private final String typeValue;

	private static final Pattern COLUMN_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.column\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	protected Class<?> getType() {
		return type;
	}

	@Override
	protected String getTypeValue() {
		return typeValue;
	}

	@Override
	public Matcher getMatcher(String key) {
		return COLUMN_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		isTrue(value.matches(INTEGER_REGEX), () -> "Column number is invalid: " + value);

		try {

			Compute compute = getCompute(key, connector);
			compute
				.getClass()
				.getMethod(SET_COLUMN, Integer.class)
				.invoke(compute, Integer.parseInt(value));

		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

			throw new IllegalStateException(
				"ColumnProcessor parse: Cannot invoke "
					+ SET_COLUMN
					+ OPENING_PARENTHESIS
					+ value
					+ CLOSING_PARENTHESIS
					+ " on "
					+ type.getSimpleName()
					+ " Compute: "
					+ e.getMessage());
		}
	}
}
