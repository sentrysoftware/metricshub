package com.sentrysoftware.matrix.connector.model.common.http.header;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AUTHENTICATION_TOKEN_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BASIC_AUTH_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USERNAME_MACRO;
import static org.springframework.util.Assert.isTrue;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmbeddedFileHeader implements Header {

	private static final long serialVersionUID = 7171137961999511622L;

	private EmbeddedFile header;

	@Override
	public Map<String, String> getContent(@NonNull String username, @NonNull char[] password, String authenticationToken) {

		if (header == null) {
			return null;
		}

		String passwordAsString = String.valueOf(password);

		String content = header.getContent();

		String resolvedContent = content
			.replace(USERNAME_MACRO, username)
			.replace(AUTHENTICATION_TOKEN_MACRO, authenticationToken == null ? "" : authenticationToken)
			.replace(PASSWORD_MACRO, passwordAsString)
			.replace(PASSWORD_BASE64_MACRO, Base64.getEncoder().encodeToString(passwordAsString.getBytes()))
			.replace(BASIC_AUTH_BASE64_MACRO, Base64.getEncoder().encodeToString((username + ":" + passwordAsString).getBytes()));

		Map<String, String> result = new HashMap<>();
		for (String line : resolvedContent.split(NEW_LINE)) {

			if (line != null && !line.trim().isEmpty()) {

				String[] tuple = line.split(":");
				isTrue(tuple.length == 2, "Invalid header entry: " + line);

				result.put(tuple[0].trim(), tuple[1].trim());
			}
		}

		return result;
	}

	
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