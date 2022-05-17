package com.sentrysoftware.matrix.model.alert;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;

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

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		addNonNull(stringJoiner, "Problem           : ", problem);
		addNonNull(stringJoiner, "Consequence       : ", consequence);
		addNonNull(stringJoiner, "Recommended Action: ", recommendedAction);

		return stringJoiner.toString();
	}

}
