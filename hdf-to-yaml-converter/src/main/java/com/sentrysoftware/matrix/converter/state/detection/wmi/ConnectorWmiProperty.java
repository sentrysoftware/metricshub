package com.sentrysoftware.matrix.converter.state.detection.wmi;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.WbemNameSpaceProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.WbemQueryProcessor;

public class ConnectorWmiProperty {

	private static final String WMI_TYPE_VALUE = "WMI";

	private ConnectorWmiProperty() {}

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream
				.of(
					new TypeProcessor(WMI_TYPE_VALUE),
					new ForceSerializationProcessor(WMI_TYPE_VALUE),
					new ExpectedResultProcessor(WMI_TYPE_VALUE),
					new WbemNameSpaceProcessor(WMI_TYPE_VALUE),
					new WbemQueryProcessor(WMI_TYPE_VALUE),
					new ErrorMessageProcessor(WMI_TYPE_VALUE))
				.collect(Collectors.toSet());
	}
}
