package com.sentrysoftware.hardware.agent.controller;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.sentrysoftware.hardware.agent.dto.ErrorResponseDTO;
import com.sentrysoftware.hardware.agent.exception.BusinessException;
import com.sentrysoftware.hardware.agent.exception.IBusinessException;

/**
 * Middleware handling all the exceptions that can be thrown by the REST Controllers.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler({BusinessException.class})
	protected <T extends IBusinessException> ResponseEntity<Object> handleBusinessException(final T exception) {

		// Get the code and generate the appropriate HttpStatus error
		return new ResponseEntity<>(ErrorResponseDTO.builder().code(exception.getErrorCode())
				.message(exception.getMessage()).date(exception.getDate()).build(),
				exception.getErrorCode().getHttpStatus());

	}
}