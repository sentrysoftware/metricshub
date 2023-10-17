package com.sentrysoftware.metricshub.converter.state.detection.wmi;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.TypeProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.WbemNameSpaceProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.WbemQueryProcessor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorWmiProperty {

	private static final String WMI_HDF_TYPE_VALUE = "WMI";
	private static final String WMI_YAML_TYPE_VALUE = "wmi";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(
				new TypeProcessor(WMI_HDF_TYPE_VALUE, WMI_YAML_TYPE_VALUE),
				new ForceSerializationProcessor(),
				new ExpectedResultProcessor(),
				new WbemNameSpaceProcessor(),
				new WbemQueryProcessor(),
				new ErrorMessageProcessor()
			)
			.collect(Collectors.toSet());
	}
}
