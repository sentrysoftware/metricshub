package com.sentrysoftware.matrix.engine.strategy.detection;

import com.sentrysoftware.matrix.connector.model.detection.criteria.http.Http;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi.Ipmi;
import com.sentrysoftware.matrix.connector.model.detection.criteria.kmversion.KmVersion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.Os;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OsCommand;
import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SnmpGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SnmpGetNext;
import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ucs.Ucs;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.Wbem;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.Wmi;

public interface ICriterionVisitor {

	public CriterionTestResult visit(final Http http);

	public CriterionTestResult visit(final Ipmi ipmi);

	public CriterionTestResult visit(final KmVersion kmVersion);

	public CriterionTestResult visit(final Os os);

	public CriterionTestResult visit(final OsCommand osCommand);

	public CriterionTestResult visit(final Process process);

	public CriterionTestResult visit(final Service service);

	public CriterionTestResult visit(final SnmpGet snmpGet);

	public CriterionTestResult visit(final SnmpGetNext snmpGetNext);

	public CriterionTestResult visit(final SshInteractive telnetInteractive);

	public CriterionTestResult visit(final Ucs ucs);

	public CriterionTestResult visit(final Wbem wbem);

	public CriterionTestResult visit(final Wmi wmi);

}
