package com.sentrysoftware.metricshub.agent.config;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AlertingSystemConfig represents the configuration for the alerting system in the MetricsHub agent.
 * It includes options such as whether the alerting system is disabled, and the template for problem alerts.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AlertingSystemConfig {

	private static final String DEFAULT_PROBLEM_TEMPLATE =
		"Problem on ${FQDN} with ${MONITOR_NAME}.${NEWLINE}${NEWLINE}${ALERT_DETAILS}${NEWLINE}${NEWLINE}${FULLREPORT}";

	@Default
	private Boolean disable = false;

	@Default
	@JsonSetter(nulls = SKIP)
	private String problemTemplate = DEFAULT_PROBLEM_TEMPLATE;
}
