package com.sentrysoftware.matrix.connector.parser.state.compute.extractpropertyfromwbempath;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExtractPropertyFromWbemPath;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

public class ConnectorExtractPropertyFromWbemPathProperty {

	private ConnectorExtractPropertyFromWbemPathProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {
		return Stream
				.of(
						new TypeProcessor(ExtractPropertyFromWbemPath.class, ExtractPropertyFromWbemPathProcessor.EXTRACT_PROPERTY_FROM_WBEM_PATH_TYPE_VALUE),
						new ColumnProcessor(ExtractPropertyFromWbemPath.class, ExtractPropertyFromWbemPathProcessor.EXTRACT_PROPERTY_FROM_WBEM_PATH_TYPE_VALUE),
						new PropertyNameProcessor())
				.collect(Collectors.toSet());
	}

}
