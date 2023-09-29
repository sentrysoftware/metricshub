package com.sentrysoftware.matrix.connector.model.monitor.task;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MultiInstanceCollect extends AbstractCollect {

	private static final Set<String> DEFAULT_KEYS = Set.of(MatrixConstants.MONITOR_ATTRIBUTE_ID);

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = SKIP)
	private Set<String> keys = DEFAULT_KEYS;

	@Builder
	public MultiInstanceCollect(
		final Map<String, Source> sources,
		final Mapping mapping,
		final Set<String> executionOrder,
		final Set<String> keys
	) {
		super(sources, mapping, executionOrder);
		this.keys = keys != null ? keys : DEFAULT_KEYS;
	}
}
