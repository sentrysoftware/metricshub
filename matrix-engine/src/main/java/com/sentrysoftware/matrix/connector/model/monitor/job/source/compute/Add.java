package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Add implements Compute {

	private static final long serialVersionUID = 8539063845713915259L;

	private Integer column;
	// Number value or Column(n), hence the String type 
	private String add;
}
