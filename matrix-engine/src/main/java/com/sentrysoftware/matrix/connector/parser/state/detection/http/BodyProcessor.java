package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.body.EmbeddedFileBody;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BodyProcessor extends HttpProcessor {

	private static final Pattern BODY_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.body\\s*$",
		Pattern.CASE_INSENSITIVE);

	private static final Pattern EMBEDDED_FILE_PATTERN = Pattern.compile(
		ConnectorParserConstants.EMBEDDED_FILE_REGEX,
		Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return BODY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((HTTP) getCriterion(key, connector)).setBody(getBody(value, connector));
	}

	/**
	 * @param value		Either an inline HTTP body, or a reference to an embedded file.
	 * @param connector	The {@link Connector}, in case the body being searched for is in an embedded file.
	 *
	 * @return			A {@link StringBody} or an {@link EmbeddedFileBody},
	 * 					depending on whether the given HTTP body is an inline body or a reference to an embedded file,
	 * 					respectively.
	 */
	private Body getBody(String value, Connector connector) {

		Matcher matcher = EMBEDDED_FILE_PATTERN.matcher(value);
		if (matcher.matches()) {

			EmbeddedFile embeddedFile = connector
				.getEmbeddedFiles()
				.get(Integer.parseInt(matcher.group(1)));

			return new EmbeddedFileBody(embeddedFile);
		}

		return new StringBody(value);
	}
}
