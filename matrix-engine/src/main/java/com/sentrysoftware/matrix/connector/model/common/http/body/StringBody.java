package com.sentrysoftware.matrix.connector.model.common.http.body;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AUTHENTICATION_TOKEN_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BASIC_AUTH_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USERNAME_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOSTNAME_MACRO;

import java.util.Base64;
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
public class StringBody implements Body {

	private static final long serialVersionUID = 7408610469247885489L;

	private String body;

	@Override
	public String getContent(@NonNull String username, @NonNull char[] password, String authenticationToken, @NonNull String hostname) {

		if (body == null) {
			return null;
		}

		String passwordAsString = String.valueOf(password);

		return body
			.replace(USERNAME_MACRO, username)
			.replace(HOSTNAME_MACRO, hostname)
			.replace(AUTHENTICATION_TOKEN_MACRO, authenticationToken == null ? "" : authenticationToken)
			.replace(PASSWORD_MACRO, passwordAsString)
			.replace(PASSWORD_BASE64_MACRO, Base64.getEncoder().encodeToString(passwordAsString.getBytes()))
			.replace(BASIC_AUTH_BASE64_MACRO, Base64.getEncoder().encodeToString((username + ":" + passwordAsString).getBytes()));
	}

	public Body copy() {
		return StringBody.builder().body(body).build();
	}

	@Override
	public String description() {
		return body;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		body = updater.apply(body);
	}
}
