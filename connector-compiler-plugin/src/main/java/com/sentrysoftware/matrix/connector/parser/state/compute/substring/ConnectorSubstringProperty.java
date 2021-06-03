package com.sentrysoftware.matrix.connector.parser.state.compute.substring;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substring;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;


public class ConnectorSubstringProperty {

	private ConnectorSubstringProperty () {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(Substring.class, SubstringProcessor.SUBSTRING_TYPE_VALUE),
				new ColumnProcessor(Substring.class, SubstringProcessor.SUBSTRING_TYPE_VALUE),
				new StartProcessor(), 
				new LengthProcessor())
			.collect(Collectors.toSet());
	}
}
