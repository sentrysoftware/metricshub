package com.sentrysoftware.metricshub.converter.state.detection.wbem;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.CriterionTypeProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.WbemNameSpaceProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.WbemQueryProcessor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorWbemProperty {

	private static final String WBEM_HDF_TYPE_VALUE = "WBEM";
	private static final String WBEM_YAML_TYPE_VALUE = "wbem";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(
				new CriterionTypeProcessor(WBEM_HDF_TYPE_VALUE, WBEM_YAML_TYPE_VALUE),
				new ForceSerializationProcessor(),
				new ExpectedResultProcessor(),
				new WbemNameSpaceProcessor(),
				new WbemQueryProcessor(),
				new ErrorMessageProcessor()
			)
			.collect(Collectors.toSet());
	}
}
