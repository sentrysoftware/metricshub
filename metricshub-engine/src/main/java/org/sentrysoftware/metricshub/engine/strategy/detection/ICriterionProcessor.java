package org.sentrysoftware.metricshub.engine.strategy.detection;

import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.DeviceTypeCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.HttpCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.IpmiCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.OsCommandCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ProcessCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ProductRequirementsCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ServiceCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetNextCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WbemCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WmiCriterion;

public interface ICriterionProcessor {
	CriterionTestResult process(WmiCriterion wmiCriterion);

	CriterionTestResult process(WbemCriterion wbemCriterion);

	CriterionTestResult process(SnmpGetNextCriterion snmpGetNextCriterion);

	CriterionTestResult process(SnmpGetCriterion snmpGetCriterion);

	CriterionTestResult process(ServiceCriterion serviceCriterion);

	CriterionTestResult process(ProductRequirementsCriterion productRequirementsCriterion);

	CriterionTestResult process(ProcessCriterion processCriterion);

	CriterionTestResult process(OsCommandCriterion osCommandCriterion);

	CriterionTestResult process(IpmiCriterion ipmiCriterion);

	CriterionTestResult process(HttpCriterion httpCriterion);

	CriterionTestResult process(DeviceTypeCriterion deviceTypeCriterion);
}
