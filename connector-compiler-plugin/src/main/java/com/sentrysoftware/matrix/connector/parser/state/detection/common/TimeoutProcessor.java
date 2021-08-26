package com.sentrysoftware.matrix.connector.parser.state.detection.common;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.SET_TIMEOUT;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TimeoutProcessor extends AbstractStateParser {

	private final Class<? extends Criterion> type;
	private final String typeValue;

	private static final Pattern TIMEOUT_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.timeout\\s*$",
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
		return TIMEOUT_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {

			Criterion criterion = getCriterion(key, connector);
			criterion
				.getClass()
				.getMethod(SET_TIMEOUT, Long.class)
				.invoke(criterion, Long.parseLong(value));

		} catch (NumberFormatException e) {

			throw new IllegalStateException(String.format(
					"TimeoutProcessor parse: Invalid timeout value in %s (%s)",
					key,
					value
			));

		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

			throw new IllegalStateException(String.format(
					"TimeoutProcessor parse: Cannot invoke %s(%s) on %s Criterion: %s",
					SET_TIMEOUT,
					value,
					type.getSimpleName(),
					e.getMessage()
			));
		}
	}

}
