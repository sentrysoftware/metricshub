package com.sentrysoftware.metricshub.engine.client.http;

import com.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;
import com.sentrysoftware.metricshub.engine.connector.model.common.ResultContent;
import com.sentrysoftware.metricshub.engine.strategy.utils.EmbeddedFileHelper;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

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

	private String authenticationToken;

	public static class HttpRequestBuilder {

		/**
		 * Set the {@link Header} object
		 *
		 * @param value       string value that can reference an embedded file
		 * @param connectorId the identifier of the connector
		 * @param hostname    the hostname of the host we currently monitor
		 * @return this builder
		 * @throws IOException
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
		 * Set the {@link Body} object
		 *
		 * @param value       string value that can reference an embedded file
		 * @param connectorId the identifier of the connector
		 * @param hostname    the hostname of the host we currently monitor
		 * @return this builder
		 * @throws IOException
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
		 * Get the HTTP embedded file
		 *
		 * @param value         value from which we want to extract the embedded file
		 * @param context       operation context (header or body) used for logging
		 * @param connectorId   the identifier of the connector used for logging
		 * @param hostname      the hostname of the host we currently monitor
		 * @return {@link Optional} instance that may contain the
		 * {@link EmbeddedFile} instance
		 * @throws IOException
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
