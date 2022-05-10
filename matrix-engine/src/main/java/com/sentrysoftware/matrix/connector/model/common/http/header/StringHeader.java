package com.sentrysoftware.matrix.connector.model.common.http.header;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AUTHENTICATION_TOKEN_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BASIC_AUTH_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USERNAME_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOSTNAME_MACRO;
import static org.springframework.util.Assert.isTrue;

import java.util.Base64;
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
	public Map<String, String> getContent(@NonNull String username, @NonNull char[] password, String authenticationToken, String hostname) {

		if (header == null) {
			return null;
		}

		String passwordAsString = String.valueOf(password);

		String resolvedHeader = header
			.replace(USERNAME_MACRO, username)
			.replace(HOSTNAME_MACRO, hostname)
			.replace(AUTHENTICATION_TOKEN_MACRO, authenticationToken == null ? "" : authenticationToken)
			.replace(PASSWORD_MACRO, passwordAsString)
			.replace(PASSWORD_BASE64_MACRO, Base64.getEncoder().encodeToString(passwordAsString.getBytes()))
			.replace(BASIC_AUTH_BASE64_MACRO, Base64.getEncoder().encodeToString((username + ":" + passwordAsString).getBytes()));

		String[] splitHeader = resolvedHeader.split(":");
		isTrue(splitHeader.length == 2, "Invalid header: " + header);

		return Map.of(splitHeader[0].trim(), splitHeader[1].trim());
	}

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
