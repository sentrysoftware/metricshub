package com.sentrysoftware.matrix.strategy.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class JobInfo {
	private String connectorId;
	private String hostname;
	private String monitorType;
	private String jobName;

}
