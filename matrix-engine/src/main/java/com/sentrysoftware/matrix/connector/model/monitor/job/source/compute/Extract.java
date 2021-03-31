package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Extract implements Compute {

	private static final long serialVersionUID = 5738773914074029228L;

	private Integer column;
	private Integer subColumn;
	private String subSeparators;
}
