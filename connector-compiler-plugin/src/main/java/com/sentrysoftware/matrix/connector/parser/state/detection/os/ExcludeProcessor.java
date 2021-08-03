package com.sentrysoftware.matrix.connector.parser.state.detection.os;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COMMA;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OSType;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.OS;

public class ExcludeProcessor extends OsProcessor {

	private static final Pattern KEEP_ONLY_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.exclude\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return KEEP_ONLY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Set<OSType> excludeList = new HashSet<>();

		try {
			Arrays.stream(value.split(COMMA))
			.forEach(excludeOs -> excludeList.add(OSType.valueOf(excludeOs.trim().toUpperCase())));

		} catch (Exception e) {
			throw new IllegalStateException(
					"ExcludeProcessor parse: invalid OS type in "
							+ value
							+ ": "
							+ e.getMessage());
		}

		((OS) getCriterion(key, connector)).setExclude(excludeList);
	}

}
