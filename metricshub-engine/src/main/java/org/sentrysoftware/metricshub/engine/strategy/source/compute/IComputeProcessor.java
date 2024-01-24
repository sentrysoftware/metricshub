package org.sentrysoftware.metricshub.engine.strategy.source.compute;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Add;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.And;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.ArrayTranslate;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Awk;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Convert;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Divide;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.DuplicateColumn;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.ExcludeMatchingLines;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Extract;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.ExtractPropertyFromWbemPath;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Json2Csv;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.KeepColumns;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.KeepOnlyMatchingLines;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.LeftConcat;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Multiply;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.PerBitTranslation;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Replace;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.RightConcat;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Substring;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Subtract;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Translate;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Xml2Csv;

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
