package com.sentrysoftware.matrix.connector.parser.state.detection.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.CLOSING_PARENTHESIS;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.OPENING_PARENTHESIS;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.SET_EXPECTED_RESULT;

@AllArgsConstructor
public class ExpectedResultProcessor extends AbstractStateParser {

	private final Class<? extends Criterion> type;
	private final String typeValue;

	private static final Pattern EXPECTED_RESULT_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.expectedresult\\s*$",
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
		return EXPECTED_RESULT_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {

			Criterion criterion = getCriterion(key, connector);
			criterion
				.getClass()
				.getMethod(SET_EXPECTED_RESULT, String.class)
				.invoke(criterion, value);

		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

			throw new IllegalStateException(
				"ExpectedResultProcessor parse: Cannot invoke "
					+ SET_EXPECTED_RESULT
					+ OPENING_PARENTHESIS
					+ value
					+ CLOSING_PARENTHESIS
					+ " on "
					+ type.getSimpleName()
					+ " Criterion: "
					+ e.getMessage());
		}
	}
}
