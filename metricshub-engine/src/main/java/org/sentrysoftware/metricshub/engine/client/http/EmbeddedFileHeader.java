package org.sentrysoftware.metricshub.engine.client.http;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmbeddedFileHeader implements Header {

	private static final long serialVersionUID = 7171137961999511622L;

	private EmbeddedFile header;

	@Override
	public Map<String, String> getContent(
		String username,
		char[] password,
		String authenticationToken,
		@NonNull String hostname
	) {
		if (header == null) {
			return new HashMap<>();
		}

		return Header.resolveAndParseHeader(header.getContent(), username, password, authenticationToken, hostname);
	}

	@Override
	public Header copy() {
		return EmbeddedFileHeader.builder().header(header.copy()).build();
	}

	@Override
	public String description() {
		return header != null ? header.description() : null;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		if (header != null) {
			header.update(updater);
		}
	}
}
