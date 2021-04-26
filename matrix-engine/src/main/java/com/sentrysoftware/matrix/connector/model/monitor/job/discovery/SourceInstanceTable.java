package com.sentrysoftware.matrix.connector.model.monitor.job.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SourceInstanceTable implements InstanceTable {

	private static final long serialVersionUID = 5807018761726958231L;

	private String sourceKey;
}
