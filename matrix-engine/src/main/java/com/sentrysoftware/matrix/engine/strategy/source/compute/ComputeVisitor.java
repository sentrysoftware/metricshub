package com.sentrysoftware.matrix.engine.strategy.source.compute;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Convert;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExtractPropertyFromWbemPath;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Json2CSV;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepColumns;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substring;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.XML2CSV;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.utils.PslUtils;

import lombok.Getter;
import lombok.Setter;

@Component
public class ComputeVisitor implements IComputeVisitor {

	@Setter
	@Getter
	private SourceTable sourceTable;

	@Override
	public void visit(final Add add) {
		// Not implemented yet
	}

	@Override
	public void visit(final ArrayTranslate arrayTranslate) {
		// Not implemented yet
	}

	@Override
	public void visit(final And and) {
		// Not implemented yet
	}

	@Override
	public void visit(final Awk awk) {
		// Not implemented yet
	}

	@Override
	public void visit(final Convert convert) {
		// Not implemented yet
	}

	@Override
	public void visit(final Divide divide) {
		// Not implemented yet
	}

	@Override
	public void visit(final DuplicateColumn duplicateColumn) {
		// Not implemented yet
	}

	@Override
	public void visit(final ExcludeMatchingLines excludeMatchingLines) {
		// Not implemented yet
	}

	@Override
	public void visit(final Extract extract) {
		// Not implemented yet
	}

	@Override
	public void visit(final ExtractPropertyFromWbemPath extractPropertyFromWbemPath) {
		// Not implemented yet
	}

	@Override
	public void visit(final Json2CSV json2csv) {
		// Not implemented yet
	}

	@Override
	public void visit(final KeepColumns keepColumns) {
		// Not implemented yet
	}

	@Override
	public void visit(final KeepOnlyMatchingLines keepOnlyMatchingLines) {
		if (keepOnlyMatchingLines != null && keepOnlyMatchingLines.getColumn() != null && keepOnlyMatchingLines.getColumn() > 0
				&& sourceTable != null && sourceTable.getTable() != null && !sourceTable.getTable().isEmpty()
				&& keepOnlyMatchingLines.getColumn() <= sourceTable.getTable().get(0).size()) {

			int columnIndex = keepOnlyMatchingLines.getColumn() - 1;
			List<List<String>> sourceTableTmp = new ArrayList<>();

			String regexpPsl = keepOnlyMatchingLines.getRegExp();
			List<String> valueList = keepOnlyMatchingLines.getValueList();

			List<List<String>> table = sourceTable.getTable();

			if (regexpPsl != null && !regexpPsl.isEmpty()) {
				Pattern regexpPattern = Pattern.compile(PslUtils.psl2JavaRegex(regexpPsl));
				for(List<String> line : table) {
					if (regexpPattern.matcher(line.get(columnIndex)).matches()) {
						sourceTableTmp.add(line);
					}
				}
				table = sourceTableTmp;

				// We clear the table in case there is a valueList to check as well. 
				sourceTableTmp.clear();
			}

			if (valueList != null && !valueList.isEmpty()) {
				for(List<String> line : table) {
					if (valueList.contains(line.get(columnIndex))) {
						sourceTableTmp.add(line);
					}
				}
				table = sourceTableTmp;
			}

			sourceTable.setTable(table);
		}
	}

	@Override
	public void visit(final LeftConcat leftConcat) {
		// Not implemented yet
	}

	@Override
	public void visit(final Multiply multiply) {
		// Not implemented yet
	}

	@Override
	public void visit(final PerBitTranslation perBitTranslation) {
		// Not implemented yet
	}

	@Override
	public void visit(final Replace replace) {
		// Not implemented yet
	}

	@Override
	public void visit(final RightConcat rightConcat) {
		// Not implemented yet
	}

	@Override
	public void visit(final Substract substract) {
		// Not implemented yet
	}

	@Override
	public void visit(final Substring substring) {
		// Not implemented yet
	}

	@Override
	public void visit(final Translate translate) {
		// Not implemented yet
	}

	@Override
	public void visit(final XML2CSV xml2csv) {
		// Not implemented yet
	}

}
