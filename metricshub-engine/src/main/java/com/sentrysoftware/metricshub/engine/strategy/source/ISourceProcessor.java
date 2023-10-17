package com.sentrysoftware.metricshub.engine.strategy.source;

import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.CopySource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.OsCommandSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.StaticSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.TableUnionSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WmiSource;

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
