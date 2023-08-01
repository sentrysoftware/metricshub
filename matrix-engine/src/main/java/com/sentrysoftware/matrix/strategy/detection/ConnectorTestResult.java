package com.sentrysoftware.matrix.strategy.detection;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.Connector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConnectorTestResult {

	private Connector connector;

	@Default
	private List<CriterionTestResult> criterionTestResult = new ArrayList<>();

}
