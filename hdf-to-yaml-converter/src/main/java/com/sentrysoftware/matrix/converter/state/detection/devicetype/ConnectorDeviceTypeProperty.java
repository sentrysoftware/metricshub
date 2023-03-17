package com.sentrysoftware.matrix.converter.state.detection.devicetype;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorDeviceTypeProperty {

	private static final String DEVICETYPE_HDF_TYPE_VALUE = "OS";
	private static final String DEVICETYPE_YAML_TYPE_VALUE = "deviceType";

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
				new TypeProcessor(DEVICETYPE_HDF_TYPE_VALUE, DEVICETYPE_YAML_TYPE_VALUE),
				new ForceSerializationProcessor(),
				new KeepOnlyProcessor(),
				new ExcludeProcessor()
			)
			.collect(Collectors.toSet());
	}
}