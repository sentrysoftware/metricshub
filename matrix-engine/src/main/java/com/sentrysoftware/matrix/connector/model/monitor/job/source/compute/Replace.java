package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Replace implements Compute {

	private static final long serialVersionUID = -1177932638215228955L;

	private Integer column;
	private String replace;
	private String replaceBy;
}
