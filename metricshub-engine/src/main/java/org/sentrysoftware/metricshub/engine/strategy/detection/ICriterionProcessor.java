package org.sentrysoftware.metricshub.engine.strategy.detection;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.CommandLineCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.DeviceTypeCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.HttpCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.IpmiCriterion;
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
	 * Process the Command Line criterion.
	 *
	 * @param commandLineCriterion The OS command criterion to process.
	 * @return The result of the criterion test.
	 */
	CriterionTestResult process(CommandLineCriterion commandLineCriterion);

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
