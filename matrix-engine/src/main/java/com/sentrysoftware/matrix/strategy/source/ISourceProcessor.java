package com.sentrysoftware.matrix.strategy.source;

import com.sentrysoftware.matrix.connector.model.monitor.task.source.CopySource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.IpmiSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.OsCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WbemSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WmiSource;

public interface ISourceProcessor {
	SourceTable process(CopySource copySource);

	SourceTable process(HttpSource httpSource);
	
	SourceTable process(IpmiSource ipmiSource);

	SourceTable process(OsCommandSource osCommandSource);

	SourceTable process(SnmpSource snmpSource);

	SourceTable process(StaticSource staticSource);

	SourceTable process(TableJoinSource tableJoinSource);

	SourceTable process(TableUnionSource tableUnionSource);

	SourceTable process(WbemSource wbemSource);

	SourceTable process(WmiSource wmiSource);
}
