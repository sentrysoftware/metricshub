package com.sentrysoftware.matrix.agent.config;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AlertingSystemConfig {

	private static final String DEFAULT_PROBLEM_TEMPLATE =
		"Problem on ${FQDN} with ${MONITOR_NAME}.${NEWLINE}${NEWLINE}${ALERT_DETAILS}${NEWLINE}${NEWLINE}${FULLREPORT}";

	@Default
	private boolean disable = false;

	@Default
	@JsonSetter(nulls = SKIP)
	private String problemTemplate = DEFAULT_PROBLEM_TEMPLATE;
}
