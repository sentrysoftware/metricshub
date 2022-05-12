package com.sentrysoftware.hardware.agent.dto.exporter;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExporterConfigDTO {

	@Default
	@JsonSetter(nulls = SKIP)
	private OtlpConfigDTO otlp = OtlpConfigDTO.builder().build();
}
