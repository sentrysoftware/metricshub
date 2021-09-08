package com.sentrysoftware.matrix.engine.strategy.detection;

import com.sentrysoftware.matrix.connector.model.Connector;
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

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CriterionUpdaterVisitor implements ICriterionVisitor {

	private ICriterionVisitor criterionVisitor;
	private Connector connector;

	@Override
	public CriterionTestResult visit(HTTP http) {
		return http.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(IPMI ipmi) {
		return ipmi.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(KMVersion kmVersion) {
		return kmVersion.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(OS os) {
		return os.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(OSCommand osCommand) {
		osCommand.setEmbeddedFiles(connector.getEmbeddedFiles());
		return osCommand.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(Process process) {
		return process.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(Service service) {
		return service.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(SNMPGet snmpGet) {
		return snmpGet.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(SNMPGetNext snmpGetNext) {
		return snmpGetNext.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(TelnetInteractive telnetInteractive) {
		return telnetInteractive.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(UCS ucs) {
		return ucs.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(WBEM wbem) {
		return wbem.accept(criterionVisitor);
	}

	@Override
	public CriterionTestResult visit(WMI wmi) {
		return wmi.accept(criterionVisitor);
	}

}
