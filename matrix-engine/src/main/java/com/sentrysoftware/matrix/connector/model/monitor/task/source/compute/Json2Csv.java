package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.TABLE_SEP;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Json2Csv extends Compute {

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = SKIP)
	@Default
	private String entryKey = "/";

	private List<String> properties = new ArrayList<>();
	private String separator;

	@Builder
	@JsonCreator
	public Json2Csv(
		@JsonProperty("type") String type, 
		@JsonProperty("entryKey") String entryKey,
		@JsonProperty("properties") List<String> properties,
		@JsonProperty("separator") String separator
	) {

		super(type);
		this.entryKey = entryKey == null ? "/" : entryKey;
		this.properties = properties == null ? new ArrayList<>() : properties;
		this.separator = separator == null ? TABLE_SEP : separator;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- entryKey=", entryKey);
		addNonNull(stringJoiner, "- properties=", properties);
		addNonNull(stringJoiner, "- separator=", separator);

		return stringJoiner.toString();
	}
	@Override
	public Json2Csv copy() {
		return Json2Csv
			.builder()
			.type(type)
			.entryKey(entryKey)
			.properties(properties == null ? null : new ArrayList<>(properties))
			.separator(separator)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		entryKey = updater.apply(entryKey);
		separator = updater.apply(separator);
		if (properties != null && !properties.isEmpty()) {
			properties = properties
				.stream()
				.map(updater::apply)
				.collect(Collectors.toCollection(ArrayList::new));
		}
	}
}
