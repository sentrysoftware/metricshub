package com.sentrysoftware.hardware.agent.dto.exporter;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExporterConfigDto {

	@Default
	@JsonSetter(nulls = SKIP)
	private OtlpConfigDto otlp = OtlpConfigDto.builder().build();
}
