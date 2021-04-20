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

	public CriterionTestResult visit(HTTP criterion);

	public CriterionTestResult visit(IPMI ipmi);

	public CriterionTestResult visit(KMVersion kmVersion);

	public CriterionTestResult visit(OS os);

	public CriterionTestResult visit(OSCommand osCommand);

	public CriterionTestResult visit(Process process);

	public CriterionTestResult visit(Service service);

	public CriterionTestResult visit(SNMPGet snmpGet);

	public CriterionTestResult visit(SNMPGetNext snmpGetNext);

	public CriterionTestResult visit(TelnetInteractive telnetInteractive);

	public CriterionTestResult visit(UCS ucs);

	public CriterionTestResult visit(WBEM wbem);

	public CriterionTestResult visit(WMI wmi);

}
