package com.sentrysoftware.matrix.converter.state.detection.productrequirements;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorProductRequirementsProperty {

	private static final String KM_VERSION_HDF_TYPE_VALUE = "KMVersion";
	private static final String PRODUCT_REQUIREMENTS_YAML_TYPE_VALUE = "productRequirements";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(
				new TypeProcessor(KM_VERSION_HDF_TYPE_VALUE, PRODUCT_REQUIREMENTS_YAML_TYPE_VALUE),
				new ForceSerializationProcessor(),
				new KmVersionProcessor()
			)
			.collect(Collectors.toSet());
	}
}
