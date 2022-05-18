package com.sentrysoftware.matrix.connector.parser.state.detection.wmi;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.Wmi;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.WbemNameSpaceProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.WbemQueryProcessor;

public class ConnectorWmiProperty {

	private static final String WMI_TYPE_VALUE = "WMI";

	private ConnectorWmiProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
					new TypeProcessor(Wmi.class, WMI_TYPE_VALUE),
					new ForceSerializationProcessor(Wmi.class, WMI_TYPE_VALUE),
					new ExpectedResultProcessor(Wmi.class, WMI_TYPE_VALUE),
					new WbemNameSpaceProcessor(Wmi.class, WMI_TYPE_VALUE),
					new WbemQueryProcessor(Wmi.class, WMI_TYPE_VALUE),
					new ErrorMessageProcessor(Wmi.class, WMI_TYPE_VALUE))
				.collect(Collectors.toSet());
	}
}
