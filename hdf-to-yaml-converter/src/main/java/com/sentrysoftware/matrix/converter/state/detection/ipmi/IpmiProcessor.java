package com.sentrysoftware.matrix.converter.state.detection.ipmi;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public abstract class IpmiProcessor extends AbstractStateConverter {

	public static final String IPMI_TYPE_VALUE = "IPMI";
	
	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		// TODO Auto-generated method stub
	}
}
