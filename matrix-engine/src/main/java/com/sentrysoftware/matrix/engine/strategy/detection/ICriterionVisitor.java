package com.sentrysoftware.matrix.engine.strategy.detection;

import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.detection.criteria.kmversion.KMVersion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.OS;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OSCommand;
import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.detection.criteria.telnet.TelnetInteractive;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ucs.UCS;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.WMI;

public interface ICriterionVisitor {

	public CriterionTestResult visit(final HTTP http);

	public CriterionTestResult visit(final IPMI ipmi);

	public CriterionTestResult visit(final KMVersion kmVersion);

	public CriterionTestResult visit(final OS os);

	public CriterionTestResult visit(final OSCommand osCommand);

	public CriterionTestResult visit(final Process process);

	public CriterionTestResult visit(final Service service);

	public CriterionTestResult visit(final SNMPGet snmpGet);

	public CriterionTestResult visit(final SNMPGetNext snmpGetNext);

	public CriterionTestResult visit(final TelnetInteractive telnetInteractive);

	public CriterionTestResult visit(final UCS ucs);

	public CriterionTestResult visit(final WBEM wbem);

	public CriterionTestResult visit(final WMI wmi);

}
