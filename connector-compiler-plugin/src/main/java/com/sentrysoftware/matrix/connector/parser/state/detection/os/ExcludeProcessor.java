package com.sentrysoftware.matrix.connector.parser.state.detection.os;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.OS;

public class ExcludeProcessor extends OsProcessor {

	private static final Pattern EXCLUDE_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.exclude\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return EXCLUDE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((OS) getCriterion(key, connector)).setExclude(getOsTypes(value));
	}
}
