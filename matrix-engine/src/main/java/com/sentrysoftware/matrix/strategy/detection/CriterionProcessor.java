package com.sentrysoftware.matrix.strategy.detection;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EXPECTED_VALUE_RETURNED_VALUE;

import java.util.regex.Pattern;

import org.bouncycastle.util.test.TestResult;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
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
import com.sentrysoftware.matrix.matsya.HttpRequest;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.utils.PslUtils;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
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
	 * @param httpCriterion
	 * @return
	 */
	CriterionTestResult process(HttpCriterion httpCriterion) {

		if (httpCriterion == null) {
			return CriterionTestResult.empty();
		}

		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();

		if (hostConfiguration == null) {
			log.debug("There is no host configuration. Cannot process HTTP detection {}.", httpCriterion);
			return CriterionTestResult.empty();
		}

		final String hostname = hostConfiguration.getHostname();

		final HttpConfiguration httpConfiguration = (HttpConfiguration) hostConfiguration
				.getConfigurations()
				.get(HttpConfiguration.class);

		if (httpConfiguration == null) {
			log.debug("Hostname {} - The HTTP credentials are not configured for this host. Cannot process HTTP detection {}.",
					hostname,
					httpCriterion);
			return CriterionTestResult.empty();
		}

		final String result = matsyaClientsExecutor.executeHttp(
				HttpRequest
				.builder()
				.hostname(hostname)
				.method(httpCriterion.getMethod().toString())
				.url(httpCriterion.getUrl())
				.header(new StringHeader(httpCriterion.getHeader()))
				.body(new StringBody(httpCriterion.getBody()))
				.httpConfiguration(httpConfiguration)
				.resultContent(httpCriterion.getResultContent())
				.authenticationToken(httpCriterion.getAuthenticationToken())
				.build(),
				false
				);

		return checkHttpResult(hostname, result, httpCriterion.getExpectedResult());
	}

	/**
	 * Process the given {@link IpmiCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param ipmiCriterion
	 * @return
	 */
	CriterionTestResult process(IpmiCriterion ipmiCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link OsCommandCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param osCommandCriterion
	 * @return
	 */
	CriterionTestResult process(OsCommandCriterion osCommandCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link ProcessCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param processCriterion
	 * @return
	 */
	CriterionTestResult process(ProcessCriterion processCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link ProductRequirementsCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param productRequirementsCriterion
	 * @return
	 */
	CriterionTestResult process(ProductRequirementsCriterion productRequirementsCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link ServiceCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param serviceCriterion
	 * @return
	 */
	CriterionTestResult process(ServiceCriterion serviceCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link SnmpCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param snmpCriterion
	 * @return
	 */
	CriterionTestResult process(SnmpCriterion snmpCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link SnmpGetCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param snmpGetCriterion
	 * @return
	 */
	CriterionTestResult process(SnmpGetCriterion snmpGetCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link SnmpGetNextCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param snmpGetNextCriterion
	 * @return
	 */
	CriterionTestResult process(SnmpGetNextCriterion snmpGetNextCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link WmiCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param wmiCriterion
	 * @return
	 */
	CriterionTestResult process(WmiCriterion wmiCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link WbemCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param wbemCriterion
	 * @return
	 */
	CriterionTestResult process(WbemCriterion wbemCriterion) {
		// TODO
		return null;
	}

	/**
	 * Process the given {@link WqlCriterion} through Matsya and return the {@link CriterionTestResult}
	 * @param wqlCriterion
	 * @return
	 */
	CriterionTestResult process(WqlCriterion wqlCriterion) {
		// TODO
		return null;
	}

	/**
	 * @param hostname			The hostname against which the HTTP test has been carried out.
	 * @param result			The actual result of the HTTP test.
	 *
	 * @param expectedResult	The expected result of the HTTP test.
	 * @return					A {@link TestResult} summarizing the outcome of the HTTP test.
	 */
	private CriterionTestResult checkHttpResult(final String hostname, final String result, final String expectedResult) {

		String message;
		boolean success = false;

		if (expectedResult == null) {
			if (result == null || result.isEmpty()) {
				message = String.format("Hostname %s - HTTP test failed - The HTTP test did not return any result.", hostname);
			} else {
				message = String.format("Hostname %s - HTTP test succeeded. Returned result: %s.", hostname, result);
				success = true;
			}

		} else {
			// We convert the PSL regex from the expected result into a Java regex to be able to compile and test it
			final Pattern pattern = Pattern.compile(PslUtils.psl2JavaRegex(expectedResult), Pattern.CASE_INSENSITIVE);
			if (result != null && pattern.matcher(result).find()) {
				message = String.format("Hostname %s - HTTP test succeeded. Returned result: %s.", hostname, result);
				success = true;
			} else {
				message = String
						.format("Hostname %s - HTTP test failed - "
								+ "The result (%s) returned by the HTTP test did not match the expected result (%s).",
								hostname, result, expectedResult);
				message += String.format(EXPECTED_VALUE_RETURNED_VALUE, expectedResult, result);
			}
		}

		log.debug(message);

		return CriterionTestResult
				.builder()
				.result(result)
				.message(message)
				.success(success)
				.build();
	}
}
