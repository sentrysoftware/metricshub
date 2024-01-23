package org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.TABLE_SEP;
import static org.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.strategy.source.compute.IComputeProcessor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Json2Csv extends Compute {

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = SKIP)
	private String entryKey = "/";

	private String properties;
	private String separator;

	@Builder
	@JsonCreator
	public Json2Csv(
		@JsonProperty("type") String type,
		@JsonProperty("entryKey") String entryKey,
		@JsonProperty("properties") String properties,
		@JsonProperty("separator") String separator
	) {
		super(type);
		this.entryKey = entryKey == null ? "/" : entryKey;
		this.properties = properties;
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
		return Json2Csv.builder().type(type).entryKey(entryKey).properties(properties).separator(separator).build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		entryKey = updater.apply(entryKey);
		separator = updater.apply(separator);
		properties = updater.apply(properties);
	}

	@Override
	public void accept(IComputeProcessor computeProcessor) {
		computeProcessor.process(this);
	}
}
