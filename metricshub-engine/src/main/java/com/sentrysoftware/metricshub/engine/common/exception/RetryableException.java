package com.sentrysoftware.metricshub.engine.common.exception;

import com.sentrysoftware.metricshub.engine.strategy.utils.RetryOperation;

/**
 * Exception which indicates that a retry operation is required.<br>
 * See {@link RetryOperation} implementation.
 */
public class RetryableException extends RuntimeException {

	private static final long serialVersionUID = 1L;
}
