package com.sentrysoftware.hardware.prometheus.exception;

import java.time.LocalDateTime;

import com.sentrysoftware.hardware.prometheus.dto.ErrorCode;

public interface IBusinessException {

	public ErrorCode getErrorCode();
	public LocalDateTime getDate();
	public String getMessage();
}