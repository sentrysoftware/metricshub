package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step;

import static org.springframework.util.Assert.isTrue;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class TypeProcessor extends AbstractStateParser {

	private static final Pattern TYPE_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.Step\\(([1-9]\\d*)\\)\\.Type\\s*$",
			Pattern.CASE_INSENSITIVE);

	private final Class<? extends Step> type;
	private final String typeValue;

	@Override
	protected Matcher getMatcher(
			@NonNull
			String key) {
		return TYPE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		isTrue(getTypeValue().equalsIgnoreCase(value), String.format("Invalid Step type: %s", value));

		final Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), String.format("Invalid key: %s.", key));

		final SshInteractiveSource sshInteractiveSource = getSshInteractiveSource(matcher, connector);

		try {

			final int stepIndex = getSourceStepIndex(matcher);

			final Step step = type.getConstructor().newInstance();
			step.setIndex(stepIndex);

			if (sshInteractiveSource.getSteps() == null) {
				sshInteractiveSource.setSteps(new ArrayList<>());
			}
			sshInteractiveSource.getSteps().add(step);

		} catch (final Exception e) {
			throw new IllegalStateException(String.format("TypeProcessor parse: Could not instantiate %s Step: %s",
					type.getSimpleName(),
					e.getMessage()));
		}
	}
}
