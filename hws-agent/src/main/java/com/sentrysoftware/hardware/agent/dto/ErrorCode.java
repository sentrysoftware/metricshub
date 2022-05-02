package com.sentrysoftware.hardware.agent.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.Getter;

/**
 * All the application error codes are defined here. The {@link HttpStatus} is also defined for each error code to allow the
 * {@link RestExceptionHandler} catching the exception and triggering the right HTTP {@link ResponseEntity}.
 */
public enum ErrorCode {

	GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR), 
	PROMETHEUS_IO_ERROR(HttpStatus.FAILED_DEPENDENCY),
	BAD_CONNECTOR_STORE(HttpStatus.FAILED_DEPENDENCY),
	CANNOT_READ_CONFIGURATION(HttpStatus.FAILED_DEPENDENCY),
	BAD_CONNECTOR_CONFIGURATION(HttpStatus.FORBIDDEN),
	NO_TARGET_TYPE(HttpStatus.FORBIDDEN),
	INVALID_HOSTNAME(HttpStatus.FORBIDDEN),
	INVALID_PORT(HttpStatus.FORBIDDEN),
	INVALID_TIMEOUT(HttpStatus.FORBIDDEN),
	NO_USERNAME(HttpStatus.FORBIDDEN),
	NO_PASSWORD(HttpStatus.FORBIDDEN),
	NO_COMMUNITY_STRING(HttpStatus.FORBIDDEN),
	NO_BMC_KEY(HttpStatus.FORBIDDEN),
	NO_NAMESPACE(HttpStatus.FORBIDDEN),
	NO_SUDO_COMMAND(HttpStatus.FORBIDDEN),
	TARGET_NOT_FOUND(HttpStatus.NOT_FOUND),
	UNSUPPORTED_PROTOCOL(HttpStatus.FORBIDDEN);

	@Getter
	private HttpStatus httpStatus;

	ErrorCode(final HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
}