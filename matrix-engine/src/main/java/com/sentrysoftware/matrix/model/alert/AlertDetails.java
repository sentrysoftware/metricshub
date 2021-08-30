package com.sentrysoftware.matrix.model.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlertDetails {

	private String problem;
	private String consequence;
	private String recommendedAction;
}
