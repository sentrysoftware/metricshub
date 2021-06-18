package com.sentrysoftware.matrix.connector.model.common.http.body;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AUTHENTICATION_TOKEN_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BASIC_AUTH_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLON;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USERNAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USERNAME_MACRO;
import static org.springframework.util.Assert.notNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StringBody implements Body {

	private static final long serialVersionUID = 7408610469247885489L;

	private String body;

	@Override
	public String getContent(String username, char[] password, String authenticationToken) {

		if (body == null) {
			return null;
		}

		notNull(username, "username cannot be null");
		notNull(password, "password cannot be null");

		String passwordAsString = String.valueOf(password);

		return body
			.replace(USERNAME_MACRO, username)
			.replace(AUTHENTICATION_TOKEN_MACRO, authenticationToken == null ? EMPTY : authenticationToken)
			.replace(PASSWORD_MACRO, passwordAsString)
			.replace(PASSWORD_BASE64_MACRO, Base64.getEncoder().encodeToString(passwordAsString.getBytes()))
			.replace(BASIC_AUTH_BASE64_MACRO, Base64.getEncoder().encodeToString((USERNAME + COLON + passwordAsString).getBytes()));
	}
}
