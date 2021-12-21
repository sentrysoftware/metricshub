package com.sentrysoftware.hardware.agent.exception;

import java.time.LocalDateTime;

import com.sentrysoftware.hardware.agent.dto.ErrorCode;

public interface IBusinessException {

	public ErrorCode getErrorCode();
	public LocalDateTime getDate();
	public String getMessage();
}