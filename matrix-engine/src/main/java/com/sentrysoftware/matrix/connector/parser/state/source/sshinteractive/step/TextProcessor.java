package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.SET_TEXT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class TextProcessor extends StepProcessor {

	private static final Pattern TEXT_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.Step\\(([1-9]\\d*)\\)\\.Text\\s*$",
			Pattern.CASE_INSENSITIVE);

	private final Class<? extends Step> type;
	private final String typeValue;

	@Override
	protected Matcher getMatcher(
			@NonNull
			String key) {
		return TEXT_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		final Step step = getSourceStep(key, connector);

		try {
			step.getClass()
			.getMethod(SET_TEXT, String.class)
			.invoke(step, value);

		} catch (final Exception e) {
			throw new IllegalStateException(
					String.format("TextProcessor parse: cannot invoke %s (%s) on Step: %s",
							SET_TEXT,
							value, 
							e.getMessage()), 
					e);
		}
	}
}
