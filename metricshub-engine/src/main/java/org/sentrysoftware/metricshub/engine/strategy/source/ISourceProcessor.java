package org.sentrysoftware.metricshub.engine.strategy.source;

import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.CopySource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.OsCommandSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.StaticSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.TableJoinSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.TableUnionSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WmiSource;

public interface ISourceProcessor {
	SourceTable process(CopySource copySource);

	SourceTable process(HttpSource httpSource);

	SourceTable process(IpmiSource ipmiSource);

	SourceTable process(OsCommandSource osCommandSource);

	SourceTable process(SnmpGetSource snmpSource);

	SourceTable process(SnmpTableSource snmpTableSource);

	SourceTable process(StaticSource staticSource);

	SourceTable process(TableJoinSource tableJoinSource);

	SourceTable process(TableUnionSource tableUnionSource);

	SourceTable process(WbemSource wbemSource);

	SourceTable process(WmiSource wmiSource);
}
