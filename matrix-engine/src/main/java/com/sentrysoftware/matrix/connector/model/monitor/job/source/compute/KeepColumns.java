package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeepColumns implements Compute {

	private static final long serialVersionUID = 8346789196215087296L;

	@Default
	private List<Integer> columnNumbers = new ArrayList<>();
}
