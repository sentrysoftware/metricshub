package com.sentrysoftware.hardware.agent.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.sentrysoftware.hardware.agent.dto.ErrorCode;
import com.sentrysoftware.hardware.agent.dto.ErrorResponseDto;
import com.sentrysoftware.hardware.agent.exception.BusinessException;

class RestExceptionHandlerTest {

	private static final String TEST_ERROR_MESSAGE = "test error message";

	@Test
	void testHandleBusinessException() {
		final BusinessException exception = new BusinessException(ErrorCode.GENERAL_ERROR, TEST_ERROR_MESSAGE);
		final ResponseEntity<Object> actual = new RestExceptionHandler().handleBusinessException(exception);
		ResponseEntity<Object> expected = new ResponseEntity<>(ErrorResponseDto.builder().code(exception.getErrorCode())
				.message(TEST_ERROR_MESSAGE).date(exception.getDate()).build(),
				exception.getErrorCode().getHttpStatus());

		assertEquals(expected, actual);
	}

}
