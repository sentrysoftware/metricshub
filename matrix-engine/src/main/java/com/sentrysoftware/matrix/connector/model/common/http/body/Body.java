package com.sentrysoftware.matrix.connector.model.common.http.body;

import java.io.Serializable;
import java.util.function.UnaryOperator;

public interface Body extends Serializable {

	String getContent(String username, char[] password, String authenticationToken, String hostname);

	Body copy();

	void update(UnaryOperator<String> updater);

	String description();
}
