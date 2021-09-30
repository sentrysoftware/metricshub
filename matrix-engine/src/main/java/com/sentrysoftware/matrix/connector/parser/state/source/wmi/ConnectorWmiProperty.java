package com.sentrysoftware.matrix.connector.parser.state.source.wmi;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemNamespaceProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemQueryProcessor;

public class ConnectorWmiProperty {

	private static final String WMI_TYPE_VALUE = "WMI";

	private ConnectorWmiProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(WMISource.class, WMI_TYPE_VALUE),
				new ForceSerializationProcessor(WMISource.class, WMI_TYPE_VALUE),
				new WbemQueryProcessor(WMISource.class, WMI_TYPE_VALUE),
				new WbemNamespaceProcessor(WMISource.class, WMI_TYPE_VALUE))
			.collect(Collectors.toSet());
	}
}
