package com.sentrysoftware.hardware.prometheus.exception;

import java.time.LocalDateTime;

import org.springframework.util.Assert;

import com.sentrysoftware.hardware.prometheus.dto.ErrorCode;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Business Exception that needs to be thrown by the internal services.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessException extends Exception implements IBusinessException {

	private static final long serialVersionUID = 4895909027035078169L;

	private final ErrorCode errorCode;
	private final LocalDateTime date;

	public BusinessException(final ErrorCode errorCode, final String message, final Throwable cause) {

		super(message, cause);

		Assert.notNull(errorCode, "errorCode cannot be null.");

		this.errorCode = errorCode;
		this.date = LocalDateTime.now();

	}

	public BusinessException(final ErrorCode errorCode, final String message) {

		super(message);

		Assert.notNull(errorCode, "errorCode cannot be null.");

		this.errorCode = errorCode;
		this.date = LocalDateTime.now();
	}

}