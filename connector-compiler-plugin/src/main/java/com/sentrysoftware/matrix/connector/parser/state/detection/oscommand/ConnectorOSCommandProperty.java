package com.sentrysoftware.matrix.connector.parser.state.detection.oscommand;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OSCommand;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TimeoutProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;

public class ConnectorOSCommandProperty {

	private ConnectorOSCommandProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
					new TypeProcessor(OSCommand.class, OSCommandProcessor.OSCOMMAND_TYPE_VALUE),
					new ForceSerializationProcessor(OSCommand.class, OSCommandProcessor.OSCOMMAND_TYPE_VALUE),
					new ExpectedResultProcessor(OSCommand.class, OSCommandProcessor.OSCOMMAND_TYPE_VALUE),
					new ErrorMessageProcessor(OSCommand.class, OSCommandProcessor.OSCOMMAND_TYPE_VALUE),
					new CommandLineProcessor(),
					new ExecuteLocallyProcessor(),
					new TimeoutProcessor(OSCommand.class, OSCommandProcessor.OSCOMMAND_TYPE_VALUE))
				.collect(Collectors.toSet());
	}

}
