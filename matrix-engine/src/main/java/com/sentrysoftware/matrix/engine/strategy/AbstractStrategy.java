package com.sentrysoftware.matrix.engine.strategy;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.ICriterionVisitor;
import com.sentrysoftware.matrix.engine.strategy.detection.TestedConnector;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.source.SourceUpdaterVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.compute.ComputeVisitor;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractStrategy implements IStrategy {

	@Autowired
	protected ConnectorStore store;

	@Autowired
	protected StrategyConfig strategyConfig;

	@Autowired
	protected SourceVisitor sourceVisitor;

	@Autowired
	@Setter
	protected Long strategyTime;

	@Autowired
	protected ICriterionVisitor criterionVisitor;

	@Override
	public void prepare() {

	}

	/**
	 * Execute each source in the given list of sources then for each source table apply all the attached computes.
	 * When the {@link SourceTable} is ready it is added to {@link HostMonitoring}
	 * 
	 * @param sources        The {@link List} of {@link Source} instances we wish to execute
	 * @param hostMonitoring The {@link SourceTable} and {@link Monitor} container (Namespace)
	 * @param connector      The connector we currently process
	 * @param monitorType    The type of the monitor {@link MonitorType} only used for logging
	 * @param hostname       The hostname of the target only used for logging
	 */
	public void processSourcesAndComputes(final List<Source> sources, final IHostMonitoring hostMonitoring,
			final Connector connector, final MonitorType monitorType,
			final String hostname) {

		processSourcesAndComputes(sources, hostMonitoring, connector, monitorType, hostname, null);
	}

	/**
	 * Execute each source in the given list of sources then for each source table apply all the attached computes.
	 * When the {@link SourceTable} is ready it is added to {@link HostMonitoring}
	 * 
	 * @param sources        The {@link List} of {@link Source} instances we wish to execute
	 * @param hostMonitoring The {@link SourceTable} and {@link Monitor} container (Namespace)
	 * @param connector      The connector we currently process
	 * @param monitorType    The type of the monitor {@link MonitorType} only used for logging
	 * @param hostname       The hostname of the target only used for logging
	 * @param monitor        The monitor used in the mono instance processing
	 */
	public void processSourcesAndComputes(final List<Source> sources, final IHostMonitoring hostMonitoring,
			final Connector connector, final MonitorType monitorType,
			final String hostname, final Monitor monitor) {

		if (sources == null || sources.isEmpty()) {
			log.debug("No source found from connector {} with monitor {}. System {}", connector.getCompiledFilename(), monitorType, hostname);
			return;
		}

		// Loop over all the sources and accept the SourceVisitor which is going to
		// visit and process the source
		for (final Source source : sources) {

			final SourceTable sourceTable = source.accept(new SourceUpdaterVisitor(sourceVisitor, connector, monitor));

			if (sourceTable == null) {
				log.warn("Received null source table for source key {}. Connector {}. Monitor {}. System {}",
						source.getKey(),
						connector.getCompiledFilename(),
						monitorType,
						hostname);
				continue;
			}

			hostMonitoring.addSourceTable(source.getKey(), sourceTable);

			final List<Compute> computes = source.getComputes();

			if (computes != null) {

				final ComputeVisitor computeVisitor = new ComputeVisitor(sourceTable, connector);

				for (final Compute compute : computes) {
					compute.accept(computeVisitor);
				}

				hostMonitoring.addSourceTable(source.getKey(), computeVisitor.getSourceTable());
			}
		}
	}

	/**
	 * Return <code>true</code> if the {@link List} of the {@link HardwareMonitor} instances is not null and not empty in the given
	 * {@link Connector}
	 * 
	 * @param connector The connector we wish to check
	 * @param hostname  The system hostname used for debug purpose
	 * @return boolean value
	 */
	public boolean validateHardwareMonitors(final Connector connector, final String hostname, final String logMessageTemplate) {
		if (connector.getHardwareMonitors() == null || connector.getHardwareMonitors().isEmpty()) {
			log.warn(logMessageTemplate, hostname, connector.getCompiledFilename());
			return false;
		}
	
		return true;
	}

	/**
	 * Run the given connector detection criteria and return true if all the
	 * criterion are successfully executed.
	 * 
	 * @param connector
	 * @param hostname
	 * @return <code>true</code> if the connector matches the platform
	 */
	public TestedConnector testConnector(final Connector connector, final String hostname) {

		final Detection detection = connector.getDetection();

		final TestedConnector testedConnector = TestedConnector.builder().connector(connector).build();

		if (detection == null) {
			log.warn("The connector {} DOES NOT match {}'s platform as it has no detection to test.",
					connector.getCompiledFilename(), hostname);
			return testedConnector;
		}

		final List<Criterion> criteria = detection.getCriteria();

		if (criteria == null || criteria.isEmpty()) {
			log.warn("The connector {} DOES NOT match {}'s platform as it has no criteria to test.",
					connector.getCompiledFilename(), hostname);
			return testedConnector;
		}

		for (Criterion criterion : criteria) {
			final CriterionTestResult critetionTestResult = processCriterion(criterion);
			if (!critetionTestResult.isSuccess()) {
				log.debug("Detected failed criterion for connector {} on platform: {}. Message: {}.",
						connector.getCompiledFilename(),
						hostname,
						critetionTestResult.getMessage());
			}

			testedConnector.getCriterionTestResults().add(critetionTestResult);
		}

		return testedConnector;
	}

	/**
	 * Accept the criterion visitor which implement the logic that needs to be
	 * executed for each criterion implementation
	 * 
	 * @param criterion
	 * @return <code>true</code> if the criterion execution succeeded
	 */
	CriterionTestResult processCriterion(final Criterion criterion) {

		return criterion.accept(criterionVisitor);
	}

	/**
	 * 
	 * @param testedConnector The {@link TestedConnector} instance we wish to check it is succeeded or not
	 * @return SUCCEEDED if the TestedConnector instance shows success = <code>true</code> otherwise FAILED
	 */
	protected static String getTestedConnectorStatus(final TestedConnector testedConnector) {
		return testedConnector.isSuccess() ? "SUCCEEDED" : "FAILED";
	}

	/**
	 * Build status parameter for the given {@link TestedConnector} 
	 * @param testedConnector
	 * @return {@link StatusParam} instance
	 */
	protected StatusParam buildStatusParamForConnector(final TestedConnector testedConnector) {
		boolean success = testedConnector.isSuccess();
		return StatusParam
				.builder()
				.collectTime(strategyTime)
				.name(HardwareConstants.STATUS_PARAMETER)
				.state(success ? ParameterState.OK : ParameterState.ALARM)
				.statusInformation(success ? "Connector test succeeded" : "Connector test failed")
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.build();
	}

	/**
	 * Build test report parameter for the given {@link TestedConnector}
	 * @param targetName
	 * @param testedConnector
	 * @return {@link TextParam} instance
	 */
	protected TextParam buildTestReportParameter(final String targetName, final TestedConnector testedConnector) {
		final TextParam testReport = TextParam
				.builder()
				.collectTime(strategyTime)
				.name(HardwareConstants.TEST_REPORT_PARAMETER)
				.parameterState(ParameterState.OK)
				.build();

		final StringBuilder value = new StringBuilder();

		final String builtTestResult = testedConnector.getCriterionTestResults().stream()
						.map(criterionResult -> String.format("Received Result: %s. %s", criterionResult.getResult(),
								criterionResult.getMessage()))
						.collect(Collectors.joining("\n"));
		value.append(builtTestResult)
				.append("\nConclusion: ")
				.append("TEST on ")
				.append(targetName)
				.append(" ")
				.append(getTestedConnectorStatus(testedConnector));

		testReport.setValue(value.toString());

		return testReport;
	}
}
