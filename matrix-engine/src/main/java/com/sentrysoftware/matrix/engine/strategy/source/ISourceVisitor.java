package com.sentrysoftware.matrix.engine.strategy.source;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.Ipmi;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OsCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs.UcsSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WbemSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WmiSource;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

public interface ISourceVisitor {

	SourceTable visit(final HttpSource httpSource);

	SourceTable visit(final Ipmi ipmi);

	SourceTable visit(final OsCommandSource osCommandSource);

	SourceTable visit(final ReferenceSource referenceSource);

	SourceTable visit(final StaticSource staticSource);

	SourceTable visit(final SnmpGetSource snmpGetSource);

	SourceTable visit(final SnmpGetTableSource snmpGetTableSource);

	SourceTable visit(final TableJoinSource tableJoinSource);

	SourceTable visit(final TableUnionSource tableUnionSource);

	SourceTable visit(final SshInteractiveSource sshInteractiveSource);

	SourceTable visit(final UcsSource ucsSource);

	SourceTable visit(final WbemSource wbemSource);

	SourceTable visit(final WmiSource wmiSource);

}
