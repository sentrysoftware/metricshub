package com.sentrysoftware.matrix.converter.state.detection.oscommand;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TimeoutProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;

public class ConnectorOSCommandProperty {

	private ConnectorOSCommandProperty() {}

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream
				.of(
					new TypeProcessor(OSCommandProcessor.OSCOMMAND_TYPE_VALUE),
					new ForceSerializationProcessor(OSCommandProcessor.OSCOMMAND_TYPE_VALUE),
					new ExpectedResultProcessor(OSCommandProcessor.OSCOMMAND_TYPE_VALUE),
					new ErrorMessageProcessor(OSCommandProcessor.OSCOMMAND_TYPE_VALUE),
					new CommandLineProcessor(),
					new ExecuteLocallyProcessor(),
					new TimeoutProcessor(OSCommandProcessor.OSCOMMAND_TYPE_VALUE))
				.collect(Collectors.toSet());
	}

}
