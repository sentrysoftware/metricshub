package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.telnet;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.common.telnet.step.Step;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TelnetInteractiveSource extends Source {

	private static final long serialVersionUID = 7662516386312299806L;

	private Integer port;
	private String excludeRegExp;
	private String keepOnlyRegExp;
	private Integer removeHeader;
	private Integer removeFooter;
	private String separators;
	private List<String> selectColumns = new ArrayList<>();
	private List<Step> steps = new ArrayList<>();

	@Builder
	public TelnetInteractiveSource(List<Compute> computes, boolean forceSerialization, Integer port,
			String excludeRegExp, String keepOnlyRegExp, Integer removeHeader, Integer removeFooter,
			String separators, List<String> selectColumns, List<Step> steps) {

		super(computes, forceSerialization);
		this.port = port;
		this.excludeRegExp = excludeRegExp;
		this.keepOnlyRegExp = keepOnlyRegExp;
		this.removeHeader = removeHeader;
		this.removeFooter = removeFooter;
		this.separators = separators;
		this.selectColumns = selectColumns;
		this.steps = steps;
	}

	
}
