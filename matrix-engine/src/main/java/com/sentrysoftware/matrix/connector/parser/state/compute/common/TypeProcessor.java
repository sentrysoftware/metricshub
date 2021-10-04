package com.sentrysoftware.matrix.connector.parser.state.compute.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;

@AllArgsConstructor
public class TypeProcessor extends AbstractStateParser {

	private final Class<? extends Compute> type;
	private final String typeValue;

	private static final Pattern TYPE_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.type\\s*$",
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

		isTrue(getTypeValue().equalsIgnoreCase(value), () -> "Invalid Source type: " + value);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> "Invalid key: " + key + ConnectorParserConstants.DOT);

		Source source = getSource(matcher, connector, true);

		// TODO: When all source types have been implemented,
		// TODO: remove this if statement and uncomment the notNull check
		if (source == null) {
			return;
		}
		//notNull(source, "Could not find any source for the following key: " + key + ConnectorParserConstants.DOT);

		if (source.getComputes() == null) {
			source.setComputes(new ArrayList<>());
		}

		try {

			Compute compute = type.getConstructor().newInstance();
			compute.setIndex(getComputeIndex(matcher));

			source.getComputes().add(compute);

		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {

			throw new IllegalStateException(
				"TypeProcessor parse: Could not instantiate "
					+ type.getSimpleName()
					+ " Compute for Source "
					+ source.getKey()
					+ ConnectorParserConstants.COLON_SPACE
					+ e.getMessage());
		}
	}
}
