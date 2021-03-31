package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Substring implements Compute {

	private static final long serialVersionUID = 1959269892827970861L;

	private Integer column;
	private Integer start;
	private Integer end;
}
