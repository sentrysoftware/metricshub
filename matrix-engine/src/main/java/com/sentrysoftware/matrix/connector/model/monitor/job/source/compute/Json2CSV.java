package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Json2CSV implements Compute {

	private static final long serialVersionUID = -4481018666787412274L;

	private String entryKey;
	@Default
	private List<String> properties = new ArrayList<>();
	private String separator;
}
