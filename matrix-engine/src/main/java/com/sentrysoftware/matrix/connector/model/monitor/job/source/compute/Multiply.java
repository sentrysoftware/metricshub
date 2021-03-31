package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Multiply implements Compute {

	private static final long serialVersionUID = -2587218670274401808L;

	private Integer column;
	// Number value or Column(n), hence the String type 
	private String multiplyBy;
}
