package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.http.header.EmbeddedFileHeader;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.Http;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderProcessor extends HttpProcessor {

	private static final Pattern HEADER_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.header\\s*$",
		Pattern.CASE_INSENSITIVE);

	private static final Pattern EMBEDDED_FILE_PATTERN = Pattern.compile(
		ConnectorParserConstants.EMBEDDED_FILE_REGEX,
		Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return HEADER_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((Http) getCriterion(key, connector)).setHeader(getHeader(value, connector));
	}

	/**
	 * @param value		Either an inline HTTP header, or a reference to an embedded file.
	 * @param connector	The {@link Connector}, in case the header being searched for is in an embedded file.
	 *
	 * @return			A {@link StringHeader} or an {@link EmbeddedFileHeader},
	 * 					depending on whether the given HTTP header is an inline header
	 * 					or a reference to an embedded file,
	 * 					respectively.
	 */
	private Header getHeader(String value, Connector connector) {

		Matcher matcher = EMBEDDED_FILE_PATTERN.matcher(value);
		if (matcher.matches()) {

			EmbeddedFile embeddedFile = connector
				.getEmbeddedFiles()
				.get(Integer.parseInt(matcher.group(1)));

			return new EmbeddedFileHeader(embeddedFile);
		}

		return new StringHeader(value);
	}
}
