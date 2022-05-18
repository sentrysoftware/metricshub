package com.sentrysoftware.matrix.connector.parser.state.source.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.body.EmbeddedFileBody;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HttpSource;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

public class BodyProcessor  extends HttpProcessor {

	private static final Pattern BODY_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.body\\s*$",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern EMBEDDED_FILE_PATTERN = Pattern.compile(
			ConnectorParserConstants.EMBEDDED_FILE_REGEX,
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return BODY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher embeddedFileMatcher = EMBEDDED_FILE_PATTERN.matcher(value);

		Body body;

		if (embeddedFileMatcher.matches()) {
			int embeddedFileIndex;

			try {
				embeddedFileIndex = Integer.parseInt(embeddedFileMatcher.group(1));
			} catch(NumberFormatException e) {
				throw new IllegalStateException(
						"BodyProcessor parse: Could not instantiate EmbeddedFile from Source ("
								+ value
								+ "): "
								+ e.getMessage());
			}

			EmbeddedFile embeddedFile = connector.getEmbeddedFiles().get(embeddedFileIndex);

			Assert.state(embeddedFile != null, () -> "BodyProcessor parse: Could not find EmbeddedFile in Source (" + value + ")");

			body = new EmbeddedFileBody(embeddedFile);
		} else {
			body = new StringBody(value);
		}

		((HttpSource) getSource(key, connector)).setBody(body);
	}
}
