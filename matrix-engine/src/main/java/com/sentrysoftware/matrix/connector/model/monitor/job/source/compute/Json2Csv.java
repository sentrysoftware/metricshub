package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TABLE_SEP;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Json2Csv extends Compute {

	private static final long serialVersionUID = -4481018666787412274L;

	private String entryKey;
	private List<String> properties = new ArrayList<>();
	private String separator = TABLE_SEP;

	@Builder
	public Json2Csv(Integer index, String entryKey, List<String> properties, String separator) {
		super(index);
		this.entryKey = entryKey;
		this.properties = properties == null ? new ArrayList<>() : properties;
		this.separator = separator == null ? TABLE_SEP : separator;
	}
	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

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
			.index(index)
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
