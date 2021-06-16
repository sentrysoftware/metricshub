package com.sentrysoftware.matrix.engine.strategy.detection;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DetectionOperation extends AbstractStrategy {

	@Override
	public Boolean call() throws Exception {

		// The configuration is wrapped in the strategyConfig bean
		final Set<String> selectedConnectors = strategyConfig.getEngineConfiguration().getSelectedConnectors();

		// Localhost check 
		final boolean isLocalhost = NetworkHelper.isLocalhost(strategyConfig.getEngineConfiguration().getTarget().getHostname());

		final List<TestedConnector> testedConnectorList;
		// No selectedConnectors then perform auto detection
		if (selectedConnectors.isEmpty()) {
			testedConnectorList = performAutoDetection(isLocalhost);
		} else {
			testedConnectorList = processSelectedConnectors(selectedConnectors);
		}

		// Create the target
		log.debug("Create the Target");
		final Monitor target = createTarget(isLocalhost);

		// Create the connector instances
		createConnectors(target, testedConnectorList);

		return true;
	}

	/**
	 * Process the user's selected connectors
	 * @param selectedConnectorKeys
	 * @return list of {@link TestedConnector}, successful or not
	 */
	List<TestedConnector> processSelectedConnectors(final Set<String> selectedConnectorKeys) {
		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();
		log.debug("Process selected connectors for system {}: {}",
				hostname, selectedConnectorKeys);

		// Get the selected connectors from the store singleton bean
		final Stream<Connector> connectorStream = store.getConnectors().entrySet().stream()
				.filter(entry -> selectedConnectorKeys.contains(entry.getKey())).map(Entry::getValue);

		return detectConnectors(connectorStream, hostname).collect(Collectors.toList());
	}

	/**
	 * Perform auto detection
	 * 
	 * @param isLocalhost whether the monitored system is local host or not
	 * @return the list of successful {@link TestedConnector}
	 * @throws LocalhostCheckException could be thrown by
	 *                                 filterConnectorsByLocalAndRemoteSupport if
	 *                                 {@link NetworkHelper#isLocalhost(String)}
	 *                                 fails
	 */
	List<TestedConnector> performAutoDetection(final boolean isLocalhost) throws LocalhostCheckException {
		String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();
		log.debug("Start DETECTION for system {}", hostname);

		// Filter Connectors by the TargetType (target type: NT, LINUX, ESX, ...etc)
		Stream<Connector> connectorStream = filterConnectorsByTargetType(store.getConnectors().values().stream(),
				strategyConfig.getEngineConfiguration().getTarget().getType());

		// Now based on the target location (Local or Remote) filter connectors by
		// localSupport or remoteSupport
		connectorStream = filterConnectorsByLocalAndRemoteSupport(connectorStream, isLocalhost);

		// Now detect the connectors, try to run the detection criteria for each
		// connector and select only the connectors
		// matching the succeeded criteria
		Stream<TestedConnector> testedConnectors = detectConnectors(connectorStream, hostname);

		// Only successful connectors for the auto detection
		final List<TestedConnector> testedConnectorList = keepOnlySuccessConnectors(testedConnectors, hostname)
				.collect(Collectors.toList());

		// Supersedes handling
		handleSupersedes(testedConnectorList);

		// We have detected connectors, now we need to handle Supersedes
		log.debug(
				"DETECTION: CONCLUSION: The following connectors match {}'s system and will be used to monitor its hardware: {}",
				hostname, testedConnectorList.stream()
						.map(c -> c.getConnector().getCompiledFilename()).collect(Collectors.toList()));

		return testedConnectorList;
	}

	/**
	 * Create connector instances
	 * @param target
	 * @param testedConnectorList
	 */
	void createConnectors(final Monitor target, final List<TestedConnector> testedConnectorList) {
		// Loop over the testedConnecotrs and create them in the HostMonitoring instance
		testedConnectorList.forEach(testedConnector -> createConnector(target, testedConnector));
	}

	/**
	 * Create the given tested connector attached to the passed {@link Monitor} target
	 * @param target
	 * @param testedConnector
	 */
	void createConnector(final Monitor target, final TestedConnector testedConnector) {

		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		final Connector connector = testedConnector.getConnector();

		final Monitor monitor = Monitor.builder().id(target.getId() + "@" + connector.getCompiledFilename())
				.name(connector.getCompiledFilename())
				.targetId(target.getId())
				.parentId(target.getId())
				.monitorType(MonitorType.CONNECTOR).build();

		final TextParam testReport = buildTestReportParameter(target.getName(), testedConnector);
		final StatusParam statusParam = buildStatusParamForConnector(testedConnector);

		monitor.addParameter(testReport);
		monitor.addParameter(statusParam);

		hostMonitoring.addMonitor(monitor);
	}

	/**
	 * Create the Target
	 * @param isLocalhost
	 */
	Monitor createTarget(final boolean isLocalhost) {

		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		final HardwareTarget target = strategyConfig.getEngineConfiguration().getTarget();

		// Create the target
		final Monitor targetMonitor = Monitor.builder()
				.id(target.getId())
				.targetId(target.getId())
				.name(target.getHostname())
				.monitorType(MonitorType.TARGET).build();

		// Create the location metadata
		targetMonitor.addMetadata(HardwareConstants.LOCATION,
				isLocalhost ? HardwareConstants.LOCALHOST: HardwareConstants.REMOTE);

		// Create the operating system type metadata
		targetMonitor.addMetadata(HardwareConstants.OPERATING_SYSTEM_TYPE, target.getType().name());

		hostMonitoring.addMonitor(targetMonitor);

		log.debug("Created Target: {} ID: {} ", target.getHostname(), target.getId());

		return targetMonitor;
	}

	/**
	 * Filter the given stream {@link TestedConnector} instances and keep only
	 * connectors with successful criteria
	 * 
	 * @param testedConnectors
	 * @param hostname
	 * @return Updated {@link Stream}
	 */
	Stream<TestedConnector> keepOnlySuccessConnectors(final Stream<TestedConnector> testedConnectors, final String hostname) {
		return testedConnectors.filter(tc -> isSuccessCriterion(tc, hostname));
	}

	/**
	 * Check if the given {@link TestedConnector} has been successfully tested.
	 * 
	 * @param testedConnector
	 * @param hostname
	 * @return <code>true</code> if all the {@link Criterion} have been tested
	 *         successfully.
	 */
	boolean isSuccessCriterion(final TestedConnector testedConnector, final String hostname) {

		boolean success = false;
		final List<CriterionTestResult> criterionTestResults = testedConnector.getCriterionTestResults();
		if (!criterionTestResults.isEmpty()) {
			success = criterionTestResults.stream().allMatch(CriterionTestResult::isSuccess);
		}

		if (!success) {
			log.debug("The connector {} matches {}'s platform.", testedConnector.getConnector().getCompiledFilename(),
					hostname);
		}

		return success;
	}

	/**
	 * If a connector is defined in the supersedes list of another detected
	 * connector then it will be removed
	 * 
	 * @param testedConnectorList
	 */
	void handleSupersedes(final List<TestedConnector> testedConnectorList) {
		final Set<String> supersedes = new HashSet<>();
		testedConnectorList.forEach(testedConnector -> updateSupersedes(supersedes, testedConnector));

		testedConnectorList.removeIf(c -> supersedes.contains(c.getConnector().getCompiledFilename().toLowerCase()));
	}

	/**
	 * Update the given {@link Set} of supersedes connectors
	 * 
	 * @param supersedes
	 * @param testedConnector
	 */
	void updateSupersedes(final Set<String> supersedes, final TestedConnector testedConnector) {
		if (testedConnector.getConnector().getSupersedes() == null
				|| testedConnector.getConnector().getSupersedes().isEmpty()) {
			return;
		}
		supersedes.addAll(testedConnector.getConnector().getSupersedes().stream()
				.map(fileName -> fileName.replace(".hdf", ".connector").toLowerCase()).collect(Collectors.toSet()));
	}

	/**
	 * Build the stream of {@link TestedConnector}. For performance reasons, a parallel stream is ran
	 * for connector detections, thus we wait for only one timeout instead of waiting all the 
	 * timeouts in serial mode.
	 * 
	 * @param stream
	 * @param hostname
	 * @return {@link Stream} of {@link Connector} instances
	 */
	Stream<TestedConnector> detectConnectors(final Stream<Connector> stream, final String hostname) {
		return stream.parallel().map(connector -> { 
			log.debug("Start Detection for Connector {}", connector.getCompiledFilename());
			final TestedConnector testedConnector = testConnector(connector, hostname);
			log.debug("End of Detection for Connector {}. Detection Status: {}", connector.getCompiledFilename(), getTestedConnectorStatus(testedConnector));
			return testedConnector;
		});
	}

	/**
	 * Filter connectors if local host and hdf.LocalSupport is false Filter
	 * connectors if not local host and hdf.RemoteSupport is not true
	 * 
	 * @param connectorStream
	 * @param isLocalhost
	 * @return {@link Stream} of {@link Connector} instances
	 * @throws LocalhostCheckException when {@link NetworkHelper#isLocalhost(String)} fails
	 */
	Stream<Connector> filterConnectorsByLocalAndRemoteSupport(final Stream<Connector> connectorStream, final boolean isLocalhost) throws LocalhostCheckException  {
		if (isLocalhost) {
			return connectorStream.filter(connector -> !Boolean.FALSE.equals(connector.getLocalSupport()));
		} else {
			return connectorStream.filter(connector -> Boolean.TRUE.equals(connector.getRemoteSupport()));
		}
	}

	/**
	 * Filter the connectors by the {@link TargetType}
	 * 
	 * @param connectorStream
	 * @param targetType
	 * 
	 * @return {@link Stream} of {@link Connector} instances
	 */
	Stream<Connector> filterConnectorsByTargetType(final Stream<Connector> connectorStream, final TargetType targetType) {

		return connectorStream.filter(connector -> Objects.nonNull(connector.getAppliesToOS())
				&& connector.getAppliesToOS().contains(targetType.getOsType()));

	}

	@Override
	public void post() {
		// Not implemented yet
	}

	@Override
	public void release() {
		// Not implemented yet
	}

	@Override
	public void prepare() {
		// Not implemented yet
	}
}
