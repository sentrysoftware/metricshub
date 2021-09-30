package com.sentrysoftware.matrix.connector.parser.state.compute.convert;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Convert;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

public class ConnectorConvertProperty {

	private ConnectorConvertProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(Convert.class, ConvertProcessor.CONVERT_TYPE_VALUE),
				new ColumnProcessor(Convert.class, ConvertProcessor.CONVERT_TYPE_VALUE),
				new ConversionTypeProcessor())
			.collect(Collectors.toSet());
	}
}
