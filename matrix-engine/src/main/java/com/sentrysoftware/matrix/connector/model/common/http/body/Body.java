package com.sentrysoftware.matrix.connector.model.common.http.body;

import java.io.Serializable;

public interface Body extends Serializable {

	String getContent(String username, char[] password, String authenticationToken);

	public Body copy();

	String description();
}
