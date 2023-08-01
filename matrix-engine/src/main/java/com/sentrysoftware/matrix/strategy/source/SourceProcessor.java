package com.sentrysoftware.matrix.strategy.source;

import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.IpmiSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.OsCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WbemSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WmiSource;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SourceProcessor implements ISourceProcessor {

	private TelemetryManager telemetryManager;
	private String connectorName;
	private MatsyaClientsExecutor matsyaClientsExecutor;

	public SourceTable process(final HttpSource httpSource) {
		// TODO Auto-generated method stub
		return null;
	}

	public SourceTable process(final IpmiSource ipmiSource) {
		// TODO Auto-generated method stub
		return null;
	}

	public SourceTable process(final OsCommandSource osCommandSource) {
		// TODO Auto-generated method stub
		return null;
	}

	public SourceTable process(final SnmpSource snmpSource) {
		// TODO Auto-generated method stub
		return null;
	}

	public SourceTable process(final TableJoinSource tableJoinSource) {
		// TODO Auto-generated method stub
		return null;
	}

	public SourceTable process(final TableUnionSource tableUnionSource) {
		// TODO Auto-generated method stub
		return null;
	}

	public SourceTable process(final WbemSource wbemSource) {
		// TODO Auto-generated method stub
		return null;
	}

	public SourceTable process(final WmiSource wmiSource) {
		// TODO Auto-generated method stub
		return null;
	}
}
