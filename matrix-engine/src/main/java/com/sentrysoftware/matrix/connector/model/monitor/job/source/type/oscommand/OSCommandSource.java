package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE_TAB;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.source.ISourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OSCommandSource extends Source {

	private static final long serialVersionUID = -5755243135604830670L;

	private String commandLine;
	private Long timeout;
	private boolean executeLocally;
	private String excludeRegExp;
	private String keepOnlyRegExp;
	private Integer removeHeader;
	private Integer removeFooter;
	private String separators = WHITE_SPACE_TAB;
	private List<String> selectColumns = new ArrayList<>();

	@Builder
	public OSCommandSource(List<Compute> computes, boolean forceSerialization, String commandLine,
			Long timeout, boolean executeLocally,
			String excludeRegExp, String keepOnlyRegExp, Integer removeHeader, Integer removeFooter,
			String separators, List<String> selectColumns, int index, String key) {

		super(computes, forceSerialization, index, key);
		this.commandLine = commandLine;
		this.timeout = timeout;
		this.executeLocally = executeLocally;
		this.excludeRegExp = excludeRegExp;
		this.keepOnlyRegExp = keepOnlyRegExp;
		this.removeHeader = removeHeader;
		this.removeFooter = removeFooter;
		this.separators = separators == null ? WHITE_SPACE_TAB : separators;
		this.selectColumns = selectColumns;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	/**
	 * Copy the current instance
	 * 
	 * @return new {@link OSCommandSource} instance
	 */
	public OSCommandSource copy() {
		return OSCommandSource.builder()
				.commandLine(commandLine)
				.computes(getComputes() != null ? getComputes().stream().collect(Collectors.toList()) : null)
				.executeLocally(executeLocally)
				.excludeRegExp(excludeRegExp)
				.forceSerialization(isForceSerialization())
				.index(getIndex() != null ? getIndex() : 0)
				.keepOnlyRegExp(keepOnlyRegExp)
				.key(getKey())
				.removeFooter(removeFooter)
				.removeHeader(removeHeader)
				.selectColumns(selectColumns)
				.separators(separators)
				.timeout(timeout)
				.build();
	}

}
