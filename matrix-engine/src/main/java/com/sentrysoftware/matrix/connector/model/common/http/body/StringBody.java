package com.sentrysoftware.matrix.connector.model.common.http.body;

import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.engine.strategy.matsya.HttpMacrosUpdater;

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
	public String getContent(String username, char[] password, String authenticationToken, @NonNull String hostname) {

		return HttpMacrosUpdater.update(
			body,
			username,
			password,
			authenticationToken,
			hostname
		);
	}

	@Override
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
