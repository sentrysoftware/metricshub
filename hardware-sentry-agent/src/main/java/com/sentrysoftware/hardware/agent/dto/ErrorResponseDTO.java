package com.sentrysoftware.hardware.agent.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to handle REST error responses.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {

	private ErrorCode code;
	private String message;
	private LocalDateTime date;
}