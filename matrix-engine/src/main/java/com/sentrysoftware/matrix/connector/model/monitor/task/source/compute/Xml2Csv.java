package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Xml2Csv extends Compute {

	private static final long serialVersionUID = 1L;

	private String recordTag;
	private String properties;

	@Builder
	public Xml2Csv(String type, String recordTag, String properties) {
		super(type);
		this.recordTag = recordTag;
		this.properties = properties;
	}


	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- recordTag=", recordTag);
		addNonNull(stringJoiner, "- properties=", properties);

		return stringJoiner.toString();
	}

	@Override
	public Xml2Csv copy() {
		return Xml2Csv
			.builder()
			.type(type)
			.recordTag(recordTag)
			.properties(properties)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		recordTag = updater.apply(recordTag);
		properties = updater.apply(properties);
	}
}
