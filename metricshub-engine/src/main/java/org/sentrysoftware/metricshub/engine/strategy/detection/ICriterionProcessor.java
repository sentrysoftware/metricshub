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

/**
 * Interface for processing different types of criteria for detection.
 */
public interface ICriterionProcessor {
	/**
	 * Process the WMI criterion.
	 *
	 * @param wmiCriterion The WMI criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(WmiCriterion wmiCriterion);

	/**
	 * Process the WBEM criterion.
	 *
	 * @param wbemCriterion The WBEM criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(WbemCriterion wbemCriterion);

	/**
	 * Process the SNMP Get Next criterion.
	 *
	 * @param snmpGetNextCriterion The SNMP Get Next criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(SnmpGetNextCriterion snmpGetNextCriterion);

	/**
	 * Process the SNMP Get criterion.
	 *
	 * @param snmpGetCriterion The SNMP Get criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(SnmpGetCriterion snmpGetCriterion);

	/**
	 * Process the service criterion.
	 *
	 * @param serviceCriterion The service criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(ServiceCriterion serviceCriterion);

	/**
	 * Process the product requirements criterion.
	 *
	 * @param productRequirementsCriterion The product requirements criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(ProductRequirementsCriterion productRequirementsCriterion);

	/**
	 * Process the process criterion.
	 *
	 * @param processCriterion The process criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(ProcessCriterion processCriterion);

	/**
	 * Process the OS command criterion.
	 *
	 * @param osCommandCriterion The OS command criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(OsCommandCriterion osCommandCriterion);

	/**
	 * Process the IPMI criterion.
	 *
	 * @param ipmiCriterion The IPMI criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(IpmiCriterion ipmiCriterion);

	/**
	 * Process the HTTP criterion.
	 *
	 * @param httpCriterion The HTTP criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(HttpCriterion httpCriterion);

	/**
	 * Process the device type criterion.
	 *
	 * @param deviceTypeCriterion The device type criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(DeviceTypeCriterion deviceTypeCriterion);
}
