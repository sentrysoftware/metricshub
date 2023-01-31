package com.sentrysoftware.matrix.connector.model.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HttpMethod {

	GET, POST, DELETE;

	public static final List<HttpMethod> HTTP_METHODS = List.of(HttpMethod.values());

}
