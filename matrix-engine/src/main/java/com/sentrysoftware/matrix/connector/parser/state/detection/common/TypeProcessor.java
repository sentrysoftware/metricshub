package com.sentrysoftware.matrix.connector.parser.state.detection.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;

@AllArgsConstructor
public class TypeProcessor extends AbstractStateParser {

	private final Class<? extends Criterion> type;
	private final String typeValue;

	private static final Pattern TYPE_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.type\\s*$",
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
		return TYPE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		isTrue(getTypeValue().equalsIgnoreCase(value), () -> "Invalid Criterion type: " + value);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> "Invalid key: " + key + ConnectorParserConstants.DOT);

		Detection detection = connector.getDetection();
		if (detection == null) {

			detection = new Detection();
			connector.setDetection(detection);
		}

		try {

			Criterion criterion = type
				.getConstructor()
				.newInstance();

			criterion.setIndex(getCriterionIndex(matcher));

			detection
				.getCriteria()
				.add(criterion);

		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {

			throw new IllegalStateException(
					String.format(
							"TypeProcessor parse: Could not instantiate %s Detection: %s",
							type.getSimpleName(),
							e.getMessage())
					, e);
		}
	}
}
