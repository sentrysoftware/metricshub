package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RightConcat implements Compute {

	private static final long serialVersionUID = -9081852556216941894L;

	private Integer column;
	private String string;
}
