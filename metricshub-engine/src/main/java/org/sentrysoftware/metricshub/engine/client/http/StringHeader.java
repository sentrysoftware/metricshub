package org.sentrysoftware.metricshub.engine.client.http;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StringHeader implements Header {

	private static final long serialVersionUID = 7838818669996389750L;

	private String header;

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

		return Header.resolveAndParseHeader(header, username, password, authenticationToken, hostname);
	}

	@Override
	public Header copy() {
		return StringHeader.builder().header(header).build();
	}

	@Override
	public String description() {
		return header;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		header = updater.apply(header);
	}
}
