package com.sentrysoftware.matrix.connector.model.common.http.header;

import java.io.Serializable;
import java.util.Map;

public interface Header extends Serializable {

	Map<String, String> getContent(String username, char[] password, String authenticationToken);
}
