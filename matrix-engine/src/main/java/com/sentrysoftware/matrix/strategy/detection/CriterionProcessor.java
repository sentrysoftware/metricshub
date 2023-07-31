package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.connector.model.identity.criterion.DeviceTypeCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.HttpCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.IpmiCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.OsCommandCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProcessCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProductRequirementsCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ServiceCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpGetCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpGetNextCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WbemCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WmiCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WqlCriterion;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriterionProcessor {

	private MatsyaClientsExecutor matsyaClientsExecutor;

	private TelemetryManager telemetryManager;

	private String connectorName;

	/**
	 * Process the given {@link DeviceTypeCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(DeviceTypeCriterion deviceTypeCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link HttpCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(HttpCriterion httpCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link IpmiCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(IpmiCriterion ipmiCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link OsCommandCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(OsCommandCriterion osCommandCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link ProcessCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(ProcessCriterion processCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link ProductRequirementsCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(ProductRequirementsCriterion productRequirementsCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link ServiceCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(ServiceCriterion serviceCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link SnmpCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(SnmpCriterion snmpCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link SnmpGetCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(SnmpGetCriterion snmpGetCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link SnmpGetNextCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(SnmpGetNextCriterion snmpGetNextCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link WmiCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(WmiCriterion wmiCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link WbemCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(WbemCriterion wbemCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link WqlCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param deviceTypeCriterion
	 * @return
	 */
	CriterionTestResult process(WqlCriterion wqlCriterion) {
		// TODO
		return null;
	}
}
