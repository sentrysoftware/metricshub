package com.sentrysoftware.matrix.converter.state.detection.process;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorProcessProperty {

	private static final String PROCESS_HDF_TYPE_VALUE = "Process";
	private static final String PROCESS_YAML_TYPE_VALUE = "process";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(
				new TypeProcessor(PROCESS_HDF_TYPE_VALUE, PROCESS_YAML_TYPE_VALUE),
				new ForceSerializationProcessor(),
				new ProcessCommandLineProcessor()
			)
			.collect(Collectors.toSet());
	}
}
