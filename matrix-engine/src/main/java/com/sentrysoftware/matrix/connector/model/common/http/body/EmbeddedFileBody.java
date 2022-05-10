package com.sentrysoftware.matrix.connector.model.common.http.body;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AUTHENTICATION_TOKEN_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BASIC_AUTH_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USERNAME_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOSTNAME_MACRO;

import java.util.Base64;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;

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
	public String getContent(@NonNull String username, @NonNull char[] password, String authenticationToken, String hostname) {

		if (body == null) {
			return null;
		}

		String passwordAsString = String.valueOf(password);

		return body
			.getContent()
			.replace(USERNAME_MACRO, username)
			.replace(HOSTNAME_MACRO, hostname)
			.replace(AUTHENTICATION_TOKEN_MACRO, authenticationToken == null ? "" : authenticationToken)
			.replace(PASSWORD_MACRO, passwordAsString)
			.replace(PASSWORD_BASE64_MACRO, Base64.getEncoder().encodeToString(passwordAsString.getBytes()))
			.replace(BASIC_AUTH_BASE64_MACRO, Base64.getEncoder().encodeToString((username + ":" + passwordAsString).getBytes()));
	}

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
