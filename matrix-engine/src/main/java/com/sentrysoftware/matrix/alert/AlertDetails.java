package com.sentrysoftware.matrix.alert;

import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.StringJoiner;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

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
		final StringJoiner stringJoiner = new StringJoiner(MatrixConstants.NEW_LINE);

		addNonNull(stringJoiner, "Problem           : ", problem);
		addNonNull(stringJoiner, "Consequence       : ", consequence);
		addNonNull(stringJoiner, "Recommended Action: ", recommendedAction);

		return stringJoiner.toString();
	}

}
