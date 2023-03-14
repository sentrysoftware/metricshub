package com.sentrysoftware.matrix.converter.state.detection.sshinteractive.step;

import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TypeProcessor extends AbstractStateConverter {

	private static final Pattern TYPE_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.Step\\(([1-9]\\d*)\\)\\.Type\\s*$",
			Pattern.CASE_INSENSITIVE);

	private final Class<? extends Step> type;

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
