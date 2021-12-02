package com.sentrysoftware.hardware.agent.exception;

import java.time.LocalDateTime;

import com.sentrysoftware.hardware.agent.dto.ErrorCode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Business Exception that needs to be thrown by the internal services.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessException extends Exception implements IBusinessException {

	private static final long serialVersionUID = 4895909027035078169L;

	private final ErrorCode errorCode;
	private final LocalDateTime date;

	public BusinessException(@NonNull final ErrorCode errorCode, final String message, final Throwable cause) {

		super(message, cause);

		this.errorCode = errorCode;
		this.date = LocalDateTime.now();

	}

	public BusinessException(@NonNull final ErrorCode errorCode, final String message) {

		super(message);

		this.errorCode = errorCode;
		this.date = LocalDateTime.now();
	}

}