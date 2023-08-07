package com.sentrysoftware.matrix.connector.model.common.http.body;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.engine.strategy.matsya.HttpMacrosUpdater;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.function.UnaryOperator;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

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

		return HttpMacrosUpdater.update(
			body.getContent(),
			username,
			password,
			authenticationToken,
			hostname
		);
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
