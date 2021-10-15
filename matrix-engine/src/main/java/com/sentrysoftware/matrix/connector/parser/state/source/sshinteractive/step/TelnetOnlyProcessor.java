package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class TelnetOnlyProcessor extends StepProcessor {

	private static final Pattern TELNET_ONLY_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.Step\\(([1-9]\\d*)\\)\\.TelnetOnly\\s*$",
			Pattern.CASE_INSENSITIVE);

	private final Class<? extends Step> type;
	private final String typeValue;

	@Override
	protected Matcher getMatcher(
			@NonNull
			String key) {
		return TELNET_ONLY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {
		super.parse(key, value, connector);

		getSourceStep(key, connector).setIgnored(isStringTrueValue(value));
	}
}
