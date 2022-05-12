package com.sentrysoftware.matrix.engine.strategy.source.compute;

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
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Json2Csv;
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
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Xml2Csv;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

public interface IComputeVisitor {

	void setSourceTable(final SourceTable sourceTable);

	SourceTable getSourceTable();

	void visit(final ArrayTranslate arrayTranslate);

	void visit(final And and);

	void visit(final Add add);

	void visit(final Awk awk);

	void visit(final Convert convert);

	void visit(final Divide divide);

	void visit(final DuplicateColumn duplicateColumn);

	void visit(final ExcludeMatchingLines excludeMatchingLines);

	void visit(final Extract extract);

	void visit(final ExtractPropertyFromWbemPath extractPropertyFromWbemPath);

	void visit(final Json2Csv json2csv);

	void visit(final KeepColumns keepColumns);

	void visit(final KeepOnlyMatchingLines keepOnlyMatchingLines);

	void visit(final LeftConcat leftConcat);

	void visit(final Multiply multiply);

	void visit(final PerBitTranslation perBitTranslation);

	void visit(final Replace replace);

	void visit(final RightConcat rightConcat);

	void visit(final Substract substract);

	void visit(final Substring substring);

	void visit(final Translate translate);

	void visit(final Xml2Csv xml2csv);

}
