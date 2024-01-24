package org.sentrysoftware.metricshub.engine.client.http;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;
import org.sentrysoftware.metricshub.engine.connector.model.common.ResultContent;
import org.sentrysoftware.metricshub.engine.strategy.utils.EmbeddedFileHelper;

/**
 * Represents an HTTP request.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class HttpRequest {

	@NonNull
	private HttpConfiguration httpConfiguration;

	@NonNull
	private String hostname;

	private String method;

	private String url;

	private Header header;

	private Body body;

	@Builder.Default
	@NonNull
	private ResultContent resultContent = ResultContent.BODY;

	/**
	 * The authentication token for the request.
	 */
	private String authenticationToken;

	/**
	 * Builder for creating instances of {@code HttpRequest}.
	 */
	public static class HttpRequestBuilder {

		/**
		 * Set the {@link Header} object.
		 *
		 * @param value       String value that can reference an embedded file.
		 * @param connectorId The identifier of the connector.
		 * @param hostname    The hostname of the host being monitored.
		 * @return This builder.
		 * @throws IOException If an I/O error occurs while processing the embedded file.
		 */
		public HttpRequestBuilder header(final String value, final String connectorId, final String hostname)
			throws IOException {
			if (value != null) {
				final Optional<EmbeddedFile> maybeEmbeddedFile = getHttpEmbeddedFile(value, "header", connectorId, hostname);
				if (maybeEmbeddedFile.isPresent()) {
					this.header = new EmbeddedFileHeader(maybeEmbeddedFile.get());
				} else {
					this.header = new StringHeader(value);
				}
			}
			return this;
		}

		/**
		 * Set the {@link Body} object.
		 *
		 * @param value       String value that can reference an embedded file.
		 * @param connectorId The identifier of the connector.
		 * @param hostname    The hostname of the host being monitored.
		 * @return This builder.
		 * @throws IOException If an I/O error occurs while processing the embedded file.
		 */
		public HttpRequestBuilder body(final String value, final String connectorId, final String hostname)
			throws IOException {
			if (value != null) {
				final Optional<EmbeddedFile> maybeEmbeddedFile = getHttpEmbeddedFile(value, "body", connectorId, hostname);
				if (maybeEmbeddedFile.isPresent()) {
					this.body = new EmbeddedFileBody(maybeEmbeddedFile.get());
				} else {
					this.body = new StringBody(value);
				}
			}

			return this;
		}

		/**
		 * Get the HTTP embedded file.
		 *
		 * @param value       Value from which to extract the embedded file.
		 * @param context     Operation context (header or body) used for logging.
		 * @param connectorId The identifier of the connector used for logging.
		 * @param hostname    The hostname of the host being monitored.
		 * @return {@link Optional} instance that may contain the {@link EmbeddedFile} instance.
		 * @throws IOException If an I/O error occurs while processing the embedded file.
		 */
		public static Optional<EmbeddedFile> getHttpEmbeddedFile(
			final String value,
			final String context,
			final String connectorId,
			final String hostname
		) throws IOException {
			final Map<String, EmbeddedFile> embeddedFiles = EmbeddedFileHelper.findEmbeddedFiles(value);

			if (embeddedFiles.size() > 1) {
				final String message = String.format(
					"Hostname %s - Many embedded files are referenced in HTTP %s '%s'. Connector: %s.",
					hostname,
					context,
					value,
					connectorId
				);
				log.error(message);
				throw new IllegalStateException(hostname);
			}

			return embeddedFiles.values().stream().findAny();
		}
	}
}
