package com.sentrysoftware.matrix.connector.parser.state.source.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isNull;
import static org.springframework.util.Assert.isTrue;

@AllArgsConstructor
public class TypeProcessor extends AbstractStateParser {

	private final Class<? extends Source> type;
	private final String typeValue;

	private static final Pattern TYPE_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.type\\s*$",
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

		MonitorJob monitorJob = getMonitorJob(matcher, connector); // Never null

		String sourceKey = getSourceKey(matcher);
		isNull(getSource(matcher, connector, false), () -> sourceKey + " has already been defined.");

		try {

			Source source = type.getConstructor().newInstance();
			source.setIndex(getSourceIndex(matcher));
			source.setKey(sourceKey);

			monitorJob.getSources().add(source);

			connector.getSourceProtocols().add(source.getProtocol());

		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {

			throw new IllegalStateException(
				"TypeProcessor parse: Could not instantiate "
					+ type.getSimpleName()
					+ " Source ("
					+ sourceKey
					+ "): "
					+ e.getMessage());
		}
	}
}
