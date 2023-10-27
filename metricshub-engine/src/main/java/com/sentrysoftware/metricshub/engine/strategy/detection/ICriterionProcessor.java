package com.sentrysoftware.metricshub.engine.strategy.detection;

import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.DeviceTypeCriterion;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.HttpCriterion;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.IpmiCriterion;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.OsCommandCriterion;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ProcessCriterion;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ProductRequirementsCriterion;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ServiceCriterion;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetCriterion;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetNextCriterion;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WbemCriterion;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WmiCriterion;

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