package com.sentrysoftware.matrix.engine.strategy.detection;

import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.engine.strategy.discovery.MonitorBuildingInfo;
import com.sentrysoftware.matrix.engine.strategy.discovery.MonitorDiscoveryVisitor;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;
import lombok.extern.slf4j.Slf4j;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DESCRIPTION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DISPLAY_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FILE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCALHOST;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.OPERATING_SYSTEM_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.REMOTE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TARGET_FQDN;

@Slf4j
public class DetectionOperation extends AbstractStrategy {

	@Override
	public Boolean call() throws Exception {

		// The configuration is wrapped in the strategyConfig bean
		final Set<String> selectedConnectors = strategyConfig.getEngineConfiguration().getSelectedConnectors();

		// Localhost check 
		final boolean isLocalhost = NetworkHelper.isLocalhost(strategyConfig.getEngineConfiguration().getTarget().getHostname());

		// Create the target
		log.debug("Create the Target");
		final Monitor target = createTarget(isLocalhost);

		// No selectedConnectors then perform auto detection
		final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);
		final List<TestedConnector> testedConnectorList;
		if (selectedConnectors.isEmpty()) {
			testedConnectorList = performAutoDetection(isLocalhost, threadsPool);
		} else {
			testedConnectorList = processSelectedConnectors(selectedConnectors, threadsPool);
		}

		// Order the shutdown
		threadsPool.shutdown();

		try {
			// Blocks until all tasks have completed execution after a shutdown request
			threadsPool.awaitTermination(THREAD_TIMEOUT, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error("Waiting for threads termination aborted with an error", e);
		}

		// Create the connector instances
		createConnectors(target, testedConnectorList);

		return true;
	}

	/**
	 * Process the user's selected connectors.
	 *
	 * @param selectedConnectorKeys	The keys of the user's selected connectors.
	 * @param threadsPool			The threads pool that will be used to execute the detections.
	 *
	 * @return						A {@link List} of {@link TestedConnector}, successful or not.
	 */
	List<TestedConnector> processSelectedConnectors(final Set<String> selectedConnectorKeys,
													ExecutorService threadsPool) {

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();
		log.debug("Process selected connectors for system {}: {}",
				hostname, selectedConnectorKeys);

		// Get the selected connectors from the store singleton bean
		final Stream<Connector> connectorStream = store.getConnectors().entrySet().stream()
				.filter(entry -> selectedConnectorKeys.contains(entry.getKey())).map(Entry::getValue);

		return detectConnectors(connectorStream, hostname, threadsPool).collect(Collectors.toList());
	}

	/**
	 * Perform auto detection.
	 * 
	 * @param isLocalhost				Whether the monitored system is local host or not.
	 * @param threadsPool				The threads pool that will be used to execute the detections.
	 *
	 * @return							The {@link List} of successful {@link TestedConnector}s.
	 *
	 * @throws LocalhostCheckException	Could be thrown by filterConnectorsByLocalAndRemoteSupport
	 * 									if {@link NetworkHelper#isLocalhost(String)} fails.
	 */
	List<TestedConnector> performAutoDetection(final boolean isLocalhost, ExecutorService threadsPool)
		throws LocalhostCheckException {

		String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();
		log.debug("Start DETECTION for system {}", hostname);

		// Get the excluded connectors
		final Set<String> excludedConnectors = strategyConfig.getEngineConfiguration().getExcludedConnectors();

		// Skip excluded connectors
		Stream<Connector> connectorStream = filterExcludedConnectors(excludedConnectors, store
				.getConnectors()
				.values());

		// Filter Connectors by the TargetType (target type: NT, LINUX, ESX, ...etc)
		connectorStream = filterConnectorsByTargetType(connectorStream, 
				strategyConfig.getEngineConfiguration().getTarget().getType());

		// Now based on the target location (Local or Remote) filter connectors by
		// localSupport or remoteSupport
		connectorStream = filterConnectorsByLocalAndRemoteSupport(connectorStream, isLocalhost);

		// Now detect the connectors, try to run the detection criteria for each
		// connector and select only the connectors
		// matching the succeeded criteria
		Stream<TestedConnector> testedConnectors = detectConnectors(connectorStream, hostname, threadsPool);

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
	 * Filter excluded connectors from the given collection of connectors.
	 * 
	 * @param excludedConnectors The user's excluded connectors we want to skip
	 * @param connectors         The connectors to filter
	 * @return {@link Stream} of {@link Connector} instances
	 */
	static Stream<Connector> filterExcludedConnectors(final Set<String> excludedConnectors, final Collection<Connector> connectors) {
		return connectors
				.stream()
				.filter(connector -> !excludedConnectors.contains(connector.getCompiledFilename()));
	}

	/**
	 * Create connector instances
	 * @param target
	 * @param testedConnectorList
	 */
	void createConnectors(final Monitor target, final List<TestedConnector> testedConnectorList) {

		// Loop over the testedConnectors and create them in the HostMonitoring instance
		for (TestedConnector testedConnector : testedConnectorList) {
			createConnector(target, testedConnector);
		}
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

		monitor.collectParameter(testReport);
		monitor.collectParameter(statusParam);

		monitor.addMetadata(TARGET_FQDN, target.getFqdn());
		monitor.addMetadata(DISPLAY_NAME, connector.getDisplayName());
		monitor.addMetadata(FILE_NAME, connector.getCompiledFilename());
		monitor.addMetadata(DESCRIPTION, connector.getComments());

		monitor.getMonitorType().getMetaMonitor()
				.accept(new MonitorDiscoveryVisitor(MonitorBuildingInfo.builder()
						.connectorName(connector.getCompiledFilename())
						.hostMonitoring(hostMonitoring)
						.hostname(target.getName())
						.monitor(monitor)
						.monitorType(MonitorType.CONNECTOR)
						.targetMonitor(target)
						.targetType(strategyConfig.getEngineConfiguration().getTarget().getType())
						.build()));
	}

	/**
	 * Creates the Target.
	 *
	 * @param isLocalhost				Whether the target should be localhost or not.
	 *
	 * @throws UnknownHostException		If the target's hostname could not be resolved.
	 */
	Monitor createTarget(final boolean isLocalhost) throws UnknownHostException {

		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		final HardwareTarget target = strategyConfig.getEngineConfiguration().getTarget();

		hostMonitoring.setLocalhost(isLocalhost);

		String hostname = target.getHostname();

		// Create the target
		final Monitor targetMonitor = Monitor
			.builder()
			.id(target.getId())
			.targetId(target.getId())
			.name(hostname)
			.monitorType(MonitorType.TARGET)
			.build();

		// Create the location metadata
		targetMonitor.addMetadata(LOCATION,
				isLocalhost ? LOCALHOST: REMOTE);

		// Create the operating system type metadata
		targetMonitor.addMetadata(OPERATING_SYSTEM_TYPE, target.getType().name());

		// Create the fqdn metadata
		targetMonitor.addMetadata(FQDN, NetworkHelper.getFqdn(hostname));

		// This will create the monitor then set the alert rules
		targetMonitor.getMonitorType().getMetaMonitor().accept(
				new MonitorDiscoveryVisitor(MonitorBuildingInfo.builder()
						.hostMonitoring(hostMonitoring)
						.hostname(hostname)
						.monitor(targetMonitor)
						.monitorType(MonitorType.TARGET)
						.targetType(target.getType())
						.build()));

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
	 * Build the stream of {@link TestedConnector}s.
	 * For performance reasons, detections will be run in parallel.
	 * Thus we just have to wait for only one timeout instead of all the timeouts as we would have to in sequential mode.
	 * 
	 * @param stream		The {@link Stream} of {@link Connector}s whose {@link Detection} will be tested.
	 * @param hostname		The name of the host against with the {@link Detection}s will be tested.
	 * @param threadsPool	The threads pool that will be used to execute the detections.
	 *
	 * @return				A {@link Stream} of {@link Connector} instances.
	 */
	Stream<TestedConnector> detectConnectors(final Stream<Connector> stream, final String hostname, ExecutorService threadsPool) {

		return stream
			.map(connector -> {

				log.debug("Start Detection for Connector {}", connector.getCompiledFilename());

				TestedConnector testedConnector;
				try {

					testedConnector = threadsPool.submit(() -> testConnector(connector, hostname)).get();

				} catch (InterruptedException e) {

					log.error("Interrupted error", e);

					Thread.currentThread().interrupt();

					return TestedConnector
						.builder()
						.connector(connector)
						.build();

				} catch (ExecutionException e) {

					log.error("Execution error", e);

					return TestedConnector
						.builder()
						.connector(connector)
						.build();
				}

				log.debug("End of Detection for Connector {}. Detection Status: {}", connector.getCompiledFilename(),
					getTestedConnectorStatus(testedConnector));

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
