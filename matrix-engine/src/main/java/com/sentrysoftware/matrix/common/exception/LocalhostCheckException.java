package com.sentrysoftware.matrix.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LocalhostCheckException extends Exception {

	private static final long serialVersionUID = 8768317006653204228L;

	public LocalhostCheckException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public LocalhostCheckException(final String message) {
		super(message);
	}
}
