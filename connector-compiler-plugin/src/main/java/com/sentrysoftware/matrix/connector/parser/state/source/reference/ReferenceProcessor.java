package com.sentrysoftware.matrix.connector.parser.state.source.reference;

import static org.springframework.util.Assert.isNull;
import static org.springframework.util.Assert.isTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public class ReferenceProcessor extends AbstractStateParser {
	private static final Pattern REFERENCE_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return REFERENCE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> "Invalid key: " + key + ConnectorParserConstants.DOT);

		MonitorJob monitorJob = getMonitorJob(matcher, connector); // Never null

		String sourceKey = getSourceKey(matcher);
		isNull(getSource(matcher, connector, false), () -> sourceKey + " has already been defined.");

		Source source;
		// If value is like "%...%", then it's a reference source, else it's a static source.
		if (value.startsWith(ConnectorParserConstants.PERCENT) && value.endsWith(ConnectorParserConstants.PERCENT)) {
			source = ReferenceSource.builder()
					.reference(value.replaceAll(ConnectorParserConstants.SOURCE_REFERENCE_REGEX_REPLACEMENT, "$1").toLowerCase())
					.build();
		} else {
			source = StaticSource.builder()
					.staticValue(value)
					.build();
		}

		source.setIndex(getSourceIndex(matcher));
		source.setKey(sourceKey);

		monitorJob.getSources().add(source);
	}

	@Override
	public Class<SNMPGetSource> getType() {
		return SNMPGetSource.class;
	}

	@Override
	public String getTypeValue() {
		return "Reference";
	}
}
