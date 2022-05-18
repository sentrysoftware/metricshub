package com.sentrysoftware.matrix.engine.strategy.detection;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPILED_FILE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DESCRIPTION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DISPLAY_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCALHOST;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.OPERATING_SYSTEM_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.REMOTE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOST_FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.APPLIES_TO_OS;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.engine.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.engine.strategy.discovery.MonitorBuildingInfo;
import com.sentrysoftware.matrix.engine.strategy.discovery.MonitorDiscoveryVisitor;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;

import com.sentrysoftware.matrix.connector.model.common.OsType;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DetectionOperation extends AbstractStrategy {

	@Override
	public Boolean call() throws Exception {

		// The configuration is wrapped in the strategyConfig bean
		final Set<String> selectedConnectors = strategyConfig.getEngineConfiguration().getSelectedConnectors();

		// Localhost check
		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		final boolean isLocalhost = NetworkHelper.isLocalhost(hostname);

		// Create the target
		log.debug("Hostname {} - Create the Target", hostname);
		final Monitor target = createTarget(isLocalhost);

		// No selectedConnectors then perform auto detection
		final List<TestedConnector> testedConnectorList;
		if (selectedConnectors.isEmpty()) {
			testedConnectorList = performAutoDetection(isLocalhost);
		} else {
			testedConnectorList = processSelectedConnectors(selectedConnectors);
		}

		// Create the connector instances
		createConnectors(target, testedConnectorList);

		return true;
	}

	/**
	 * Process the user's selected connectors.
	 *
	 * @param selectedConnectorKeys	The keys of the user's selected connectors.
	 *
	 * @return						A {@link List} of {@link TestedConnector}, successful or not.
	 */
	List<TestedConnector> processSelectedConnectors(final Set<String> selectedConnectorKeys) {

		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		log.debug("Hostname {} - Process selected connectors: {}",
				hostname, selectedConnectorKeys);

		// Get the selected connectors from the store singleton bean
		final Stream<Connector> connectorStream = store.getConnectors().entrySet().stream()
				.filter(entry -> selectedConnectorKeys.contains(entry.getKey())).map(Entry::getValue);

		return detectConnectors(connectorStream, hostname).collect(Collectors.toList());
	}

	/**
	 * Perform auto detection.
	 *
	 * @param isLocalhost				Whether the monitored system is local host or not.
	 *
	 * @return							The {@link List} of successful {@link TestedConnector}s.
	 */
	List<TestedConnector> performAutoDetection(final boolean isLocalhost) {

		String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		log.debug("Hostname {} - Start Detection", hostname);

		// Get the excluded connectors
		final Set<String> excludedConnectors = strategyConfig.getEngineConfiguration().getExcludedConnectors();

		// Skip excluded connectors
		Stream<Connector> connectorStream = filterExcludedConnectors(excludedConnectors, store
				.getConnectors()
				.values());

		final HostType hostType = strategyConfig.getEngineConfiguration().getHost().getType();

		// Skip connectors with a "hdf.NoAutoDetection" set to "true"
		connectorStream = filterNoAutoDetectionConnectors(connectorStream);

		// Filter Connectors by the HostType (target type: NT, LINUX, ESX, ...etc)
		connectorStream = filterConnectorsByTargetType(connectorStream, hostType);

		// Now based on the target location (Local or Remote) filter connectors by
		// localSupport or remoteSupport
		connectorStream = filterConnectorsByLocalAndRemoteSupport(connectorStream, isLocalhost);

		// Based on the user's configuration, determine the sources that we can actually accept
		Set<Class<? extends Source>> acceptedSources = strategyConfig.getEngineConfiguration()
				.determineAcceptedSources(isLocalhost);

		// Now we know what would be executed, filter the connectors based on the accepted protocols
		connectorStream = filterConnectorsByAcceptedSources(connectorStream, acceptedSources);

		// Now detect the connectors, try to run the detection criteria for each connector and select only the connectors
		// matching the succeeded criteria. Sort the connectors alphabetically
		Stream<TestedConnector> testedConnectors = detectConnectors(connectorStream, hostname)
				.sorted(Comparator.comparing(tc -> tc.getConnector().getCompiledFilename()));
		
		// Only successful connectors for the auto detection
		final List<TestedConnector> testedConnectorList = keepOnlySuccessConnectors(testedConnectors, hostname)
				.collect(Collectors.toList());

		// Supersedes handling
		handleSupersedes(testedConnectorList);
		
		// Filter out last resort connectors when appropriate
		filterLastResortConnectors(testedConnectorList, hostname);

		// We have detected connectors, now we need to handle Supersedes
		log.debug(
				"Hostname {} - Detection Conclusion: The following connectors match the system and will be used to monitor its hardware: {}",
				hostname, testedConnectorList.stream()
						.map(c -> c.getConnector().getCompiledFilename()).collect(Collectors.toList()));

		return testedConnectorList;
	}

	/**
	 * Removes detected connectors of type "last resort" if their specified "last resort" monitor type (enclosure, fan, etc.) is already 
	 * discovered by a "regular" connector.
	 * 
	 * @param matchingConnectorList The list of detected connectors, that match the host
	 * @param hostname      		The name of the host currently discovered
	 * 
	 */
	void filterLastResortConnectors(List<TestedConnector> matchingConnectorList, final String hostname) {

		// Extract the list of last resort connectors from the list of matching connectors
		final List<TestedConnector> lastResortConnectorList = matchingConnectorList
				.stream()
				.filter(tc -> tc.getConnector().getOnLastResort() != null)
				.collect(Collectors.toList());
		
		if (lastResortConnectorList.isEmpty()) {
			return;
		}
		
		// Extract the list of regular connectors connectors from the list of matching connectors
		final List<TestedConnector> regularConnectorList = matchingConnectorList
				.stream()
				.filter(tc -> tc.getConnector().getOnLastResort() == null)
				.collect(Collectors.toList());
		
		// Go through the list of last resort connectors and remove them if a regular connector discovers the same monitor type
		String[] connectorNameHolder = new String[1];
		lastResortConnectorList.forEach(lastResortTC -> {
			boolean hasLastResortMonitor = regularConnectorList.stream().anyMatch(tc -> { 
				List<HardwareMonitor> hardwareMonitors = tc.getConnector().getHardwareMonitors();
				
				// Remember connector's filename
				connectorNameHolder[0] = tc.getConnector().getCompiledFilename();
				
				if (hardwareMonitors == null || hardwareMonitors.isEmpty()) {
					log.warn(
							"Hostname {} - {} connector detection. On last resort filter: connector {} has no hardware monitors",
							hostname,
							strategyConfig.getEngineConfiguration().getHost().getHostname(),
							connectorNameHolder[0]
							);
					
					return false;
				}
	
				// The monitor's instance table must not be empty
				return hardwareMonitors.stream().anyMatch(hm -> lastResortTC.getConnector().getOnLastResort().equals(hm.getType())
						&& hm.getDiscovery() != null && hm.getDiscovery().getInstanceTable() != null);	
			});
		
			if (hasLastResortMonitor) {
				// The current connector discovers the same type has the defined last resort type. Discard last resort connector 
				matchingConnectorList.remove(lastResortTC);
				
				log.info(
						"Hostname {} - {} is a \"last resort\" connector and its components are already discovered thanks to connector {}. Connector is therefore discarded.",
						hostname,
						lastResortTC.getConnector().getCompiledFilename(),
						connectorNameHolder[0]
						);
				
			} else {
				// Add the last resort connector to the list of "regular" connectors so that it prevents other
				// last resort connectors of the same type from matching (but that should never happen, right connector developers?)
				regularConnectorList.add(lastResortTC);
			}
		});
	}


	/**
	 * Filter the given stream of {@link Connector} instances based on the source
	 * types we actually accept
	 *
	 * @param connectorStream Stream of connector instances from the singleton store
	 * @param acceptedSources Set of the sources we should accept in the current
	 *                        host monitoring detection
	 * @return new {@link Stream} of connectors
	 */
	Stream<Connector> filterConnectorsByAcceptedSources(final Stream<Connector> connectorStream,
			Set<Class<? extends Source>> acceptedSources) {

		return connectorStream.filter(connector -> acceptedSources
				.stream().anyMatch(sourceProtocol -> connector.getSourceTypes().contains(sourceProtocol)));
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
	 * @param host
	 * @param testedConnector
	 */
	void createConnector(final Monitor host, final TestedConnector testedConnector) {

		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		final Connector connector = testedConnector.getConnector();

		final Monitor monitor = Monitor.builder().id(host.getId() + "@" + connector.getCompiledFilename())
				.name(connector.getDisplayName())
				.hostId(host.getId())
				.parentId(host.getId())
				.monitorType(MonitorType.CONNECTOR).build();

		final TextParam testReport = buildTestReportParameter(host.getName(), testedConnector);
		final IParameter[] statusAndStatusInformation = buildConnectorStatusAndStatusInformation(testedConnector);

		monitor.collectParameter(testReport);
		monitor.collectParameter(statusAndStatusInformation[0]); // Status
		monitor.collectParameter(statusAndStatusInformation[1]); // Status Information

		monitor.addMetadata(HOST_FQDN, host.getFqdn());
		monitor.addMetadata(DISPLAY_NAME, connector.getDisplayName());
		monitor.addMetadata(COMPILED_FILE_NAME, connector.getCompiledFilename());
		monitor.addMetadata(DESCRIPTION, connector.getComments());
		monitor.addMetadata(CONNECTOR, connector.getCompiledFilename());
		monitor.addMetadata(APPLIES_TO_OS, connector.getAppliesToOS().stream().map(OsType::getDisplayName).collect(Collectors.joining(",")));

		monitor.getMonitorType().getMetaMonitor()
				.accept(new MonitorDiscoveryVisitor(MonitorBuildingInfo.builder()
						.connectorName(connector.getCompiledFilename())
						.hostMonitoring(hostMonitoring)
						.hostname(host.getName())
						.monitor(monitor)
						.monitorType(MonitorType.CONNECTOR)
						.targetMonitor(host)
						.hostType(strategyConfig.getEngineConfiguration().getHost().getType())
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

		final HardwareHost host = strategyConfig.getEngineConfiguration().getHost();

		hostMonitoring.setLocalhost(isLocalhost);

		String hostname = host.getHostname();

		// Create the target
		final Monitor targetMonitor = Monitor
			.builder()
			.id(host.getId())
			.hostId(host.getId())
			.name(hostname)
			.monitorType(MonitorType.HOST)
			.discoveryTime(strategyTime)
			.build();

		// Create the location metadata
		targetMonitor.addMetadata(LOCATION,
				isLocalhost ? LOCALHOST: REMOTE);

		// Create the operating system type metadata
		targetMonitor.addMetadata(OPERATING_SYSTEM_TYPE, host.getType().name());

		// Create the fqdn metadata
		targetMonitor.addMetadata(FQDN, NetworkHelper.getFqdn(hostname));

		// This will create the monitor then set the alert rules
		targetMonitor.getMonitorType().getMetaMonitor().accept(
				new MonitorDiscoveryVisitor(MonitorBuildingInfo.builder()
						.hostMonitoring(hostMonitoring)
						.hostname(hostname)
						.monitor(targetMonitor)
						.monitorType(MonitorType.HOST)
						.hostType(host.getType())
						.build()));

		log.debug("Hostname {} - Created Target ID: {} ", host.getHostname(), host.getId());

		return hostMonitoring.getTargetMonitor();
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
			log.debug("Hostname {} - The connector {} matches {}'s platform.", hostname,
					testedConnector.getConnector().getCompiledFilename(), hostname);
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
				.map(fileName -> ConnectorParser.normalizeConnectorName(fileName).toLowerCase()).collect(Collectors.toSet()));
	}

	/**
	 * Build the stream of {@link TestedConnector}s.
	 * For performance reasons, detections will be run in parallel.
	 * Thus we just have to wait for only one timeout instead of all the timeouts as we would have to in sequential mode.
	 *
	 * @param stream		The {@link Stream} of {@link Connector}s whose {@link Detection} will be tested.
	 * @param hostname		The name of the host against with the {@link Detection}s will be tested.
	 *
	 * @return				A {@link Stream} of {@link Connector} instances.
	 */
	Stream<TestedConnector> detectConnectors(final Stream<Connector> stream, final String hostname) {

		final List<TestedConnector> testedConnectors = new ArrayList<>();

		// The user may want to run queries sent to the target one by one instead of everything in parallel
		if (strategyConfig.getEngineConfiguration().isSequential()) {

			log.info("Hostname {} - Running detection in sequential mode", hostname);

			// Run detection in sequential mode
			stream.forEach(
					connector -> testedConnectors.add(runConnectorDetection(connector, hostname)));

		} else {

			log.info("Hostname {} - Running detection in parallel mode", hostname);

			// Default mode is parallel.
			// Make sure our list is thread-safe, in our case it is not required to manually synchronize this list as there is no traversal
			// using stream or iterator in the threads.
			final List<TestedConnector> testedConnectorsSynchronized = Collections.synchronizedList(testedConnectors);

			final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

			stream.forEach(connector -> threadsPool
					.execute(() -> testedConnectorsSynchronized.add(runConnectorDetection(connector, hostname))));

			// Order the shutdown
			threadsPool.shutdown();

			try {
				// Blocks until all tasks have completed execution after a shutdown request
				threadsPool.awaitTermination(THREAD_TIMEOUT, TimeUnit.SECONDS);
			} catch (Exception e) {
				if (e instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}
				log.debug("Hostname {} - Waiting for threads termination aborted with an error", hostname, e);
			}
		}

		return testedConnectors.stream();
	}

	/**
	 * Run the detection using the criteria defined in the given connector.
	 * 
	 * @param connector The connector we wish to test
	 * @param hostname  The hostname of the target device
	 * 
	 * @return {@link TestedConnector} instance which tells if the connector test succeeded or not.
	 */
	private TestedConnector runConnectorDetection(final Connector connector, final String hostname) {

		log.debug("Hostname {} - Start Detection for Connector {}", hostname, connector.getCompiledFilename());

		final TestedConnector testedConnector = testConnector(connector, hostname);

		log.debug("Hostname {} - End of Detection for Connector {}. Detection Status: {}", hostname, connector.getCompiledFilename(),
				getTestedConnectorStatus(testedConnector));

		return testedConnector;
	}

	/**
	 * Filter connectors if local host and hdf.LocalSupport is false Filter
	 * connectors if not local host and hdf.RemoteSupport is not true
	 *
	 * @param connectorStream
	 * @param isLocalhost
	 * @return {@link Stream} of {@link Connector} instances
	 */
	Stream<Connector> filterConnectorsByLocalAndRemoteSupport(final Stream<Connector> connectorStream, final boolean isLocalhost) {
		if (isLocalhost) {
			return connectorStream.filter(connector -> !Boolean.FALSE.equals(connector.getLocalSupport()));
		} else {
			return connectorStream.filter(connector -> Boolean.TRUE.equals(connector.getRemoteSupport()));
		}
	}

	/**
	 * Filter the connectors by the {@link HostType}
	 *
	 * @param connectorStream
	 * @param hostType
	 *
	 * @return {@link Stream} of {@link Connector} instances
	 */
	Stream<Connector> filterConnectorsByTargetType(final Stream<Connector> connectorStream, final HostType hostType) {

		return connectorStream.filter(connector -> Objects.nonNull(connector.getAppliesToOS())
				&& connector.getAppliesToOS().contains(hostType.getOsType()));

	}

	/**
	 * Filter connectors not having an <i>hdf.NoAutoDetection</i> set to <i>true</i>
	 * from the given stream of connectors.
	 *
	 * @param connectorStream	The connectors to filter.
	 * @return					{@link Stream} of {@link Connector} instances.
	 */
	static Stream<Connector> filterNoAutoDetectionConnectors(final Stream<Connector> connectorStream) {

		return connectorStream
			.filter(connector -> connector.getNoAutoDetection() == null || !connector.getNoAutoDetection());
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
