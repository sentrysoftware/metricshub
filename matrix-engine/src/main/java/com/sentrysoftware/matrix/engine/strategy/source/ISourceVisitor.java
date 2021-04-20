package com.sentrysoftware.matrix.engine.strategy.source;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.telnet.TelnetInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs.UCSSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;

public interface ISourceVisitor {

	SourceTable visit(HTTPSource httpSource);

	SourceTable visit(IPMI ipmi);

	SourceTable visit(OSCommandSource osCommandSource);

	SourceTable visit(ReferenceSource referenceSource);

	SourceTable visit(SNMPGetSource snmpGetSource);

	SourceTable visit(SNMPGetTableSource snmpGetTableSource);

	SourceTable visit(TableJoinSource tableJoinSource);

	SourceTable visit(TableUnionSource tableUnionSource);

	SourceTable visit(TelnetInteractiveSource telnetInteractiveSource);

	SourceTable visit(UCSSource ucsSource);

	SourceTable visit(WBEMSource wbemSource);

	SourceTable visit(WMISource wmiSource);

}
