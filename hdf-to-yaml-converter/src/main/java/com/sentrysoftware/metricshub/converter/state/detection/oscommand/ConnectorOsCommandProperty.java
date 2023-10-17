package com.sentrysoftware.metricshub.converter.state.detection.oscommand;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.TimeoutProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.CriterionTypeProcessor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorOsCommandProperty {

	private static final String OSCOMMAND_HDF_TYPE_VALUE = "OSCommand";
	private static final String OSCOMMAND_YAML_TYPE_VALUE = "osCommand";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(
				new CriterionTypeProcessor(OSCOMMAND_HDF_TYPE_VALUE, OSCOMMAND_YAML_TYPE_VALUE),
				new ForceSerializationProcessor(),
				new ExpectedResultProcessor(),
				new ErrorMessageProcessor(),
				new CommandLineProcessor(),
				new ExecuteLocallyProcessor(),
				new TimeoutProcessor()
			)
			.collect(Collectors.toSet());
	}
}
