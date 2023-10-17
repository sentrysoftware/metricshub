package com.sentrysoftware.metricshub.engine.matsya.http;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.EMPTY;

import com.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbeddedFileBody implements Body {

	private static final long serialVersionUID = -8300191804094179578L;

	private EmbeddedFile body;

	@Override
	public String getContent(String username, char[] password, String authenticationToken, @NonNull String hostname) {
		if (body == null) {
			return EMPTY;
		}

		return HttpMacrosUpdater.update(body.getContent(), username, password, authenticationToken, hostname);
	}

	@Override
	public Body copy() {
		return EmbeddedFileBody.builder().body(body.copy()).build();
	}

	@Override
	public String description() {
		return body != null ? body.description() : null;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		if (body != null) {
			body.update(updater);
		}
	}
}
