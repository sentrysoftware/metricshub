package com.sentrysoftware.matrix.model.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AlertDetails {

	private String problem;
	private String consequence;
	private String recommendedAction;
}
