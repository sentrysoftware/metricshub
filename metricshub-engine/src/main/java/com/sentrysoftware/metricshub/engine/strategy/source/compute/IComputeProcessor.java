package com.sentrysoftware.metricshub.engine.strategy.source.compute;

import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Add;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.And;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.ArrayTranslate;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Awk;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Convert;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Divide;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.DuplicateColumn;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Extract;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.ExtractPropertyFromWbemPath;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Json2Csv;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.KeepColumns;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.LeftConcat;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Multiply;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.PerBitTranslation;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Replace;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.RightConcat;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Substring;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Subtract;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Translate;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Xml2Csv;

public interface IComputeProcessor {
	void process(Add add);

	void process(And and);

	void process(ArrayTranslate arrayTranslate);

	void process(Awk awk);

	void process(Convert convert);

	void process(Divide divide);

	void process(DuplicateColumn duplicateColumn);

	void process(ExcludeMatchingLines excludeMatchingLines);

	void process(Extract extract);

	void process(ExtractPropertyFromWbemPath extractPropertyFromWbemPath);

	void process(Json2Csv json2Csv);

	void process(KeepColumns keepColumns);

	void process(KeepOnlyMatchingLines keepOnlyMatchingLines);

	void process(LeftConcat leftConcat);

	void process(Multiply multiply);

	void process(PerBitTranslation perBitTranslation);

	void process(Replace replace);

	void process(RightConcat rightConcat);

	void process(Substring substring);

	void process(Subtract subtract);

	void process(Translate translate);

	void process(Xml2Csv xml2Csv);
}
