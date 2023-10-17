package com.sentrysoftware.metricshub.converter.state.detection.ipmi;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.metricshub.converter.state.detection.common.CriterionTypeProcessor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorIpmiProperty {

	public static final String IPMI_HDF_TYPE_VALUE = "IPMI";
	public static final String IPMI_YAML_TYPE_VALUE = "ipmi";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(new CriterionTypeProcessor(IPMI_HDF_TYPE_VALUE, IPMI_YAML_TYPE_VALUE), new ForceSerializationProcessor())
			.collect(Collectors.toSet());
	}
}
