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

	SourceTable visit(final HTTPSource httpSource);

	SourceTable visit(final IPMI ipmi);

	SourceTable visit(final OSCommandSource osCommandSource);

	SourceTable visit(final ReferenceSource referenceSource);

	SourceTable visit(final SNMPGetSource snmpGetSource);

	SourceTable visit(final SNMPGetTableSource snmpGetTableSource);

	SourceTable visit(final TableJoinSource tableJoinSource);

	SourceTable visit(final TableUnionSource tableUnionSource);

	SourceTable visit(final TelnetInteractiveSource telnetInteractiveSource);

	SourceTable visit(final UCSSource ucsSource);

	SourceTable visit(final WBEMSource wbemSource);

	SourceTable visit(final WMISource wmiSource);

}
