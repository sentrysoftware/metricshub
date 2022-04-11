package com.sentrysoftware.matrix.model.monitoring;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DISK_CONTROLLER_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ID_SEPARATOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.common.helpers.StreamUtils;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.OperationStatus;
import com.sentrysoftware.matrix.engine.configuration.ApplicationBeans;
import com.sentrysoftware.matrix.engine.strategy.Context;
import com.sentrysoftware.matrix.engine.strategy.IStrategy;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.discovery.MonitorAlertRulesVisitor;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.IParameter;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@Slf4j
public class HostMonitoring implements IHostMonitoring {

	private static final String MONITOR_ID_CANNOT_BE_NULL = "monitor id cannot be null.";
	private static final String PARENT_ID_CANNOT_BE_NULL = "Parent Id cannot be null.";
	private static final String TARGET_ID_CANNOT_BE_NULL = "Target Id cannot be null.";
	private static final String MONITOR_TYPE_CANNOT_BE_NULL = "monitor type cannot be null.";

	private static final String STRATEGY_TIME = "strategyTime";
	private static final String STRATEGY_BEAN_NAME = "strategy";
	private static final String STRATEGY_CONFIG_BEAN_NAME = "strategyConfig";

	private static final List<MonitorType> DISK_CONTROLLER_CHILDREN = List.of(MonitorType.BATTERY,
		MonitorType.LOGICAL_DISK, MonitorType.PHYSICAL_DISK, MonitorType.TAPE_DRIVE);

	public static final HostMonitoring HOST_MONITORING = new HostMonitoring();

	private Map<MonitorType, Map<String, Monitor>> monitors = new LinkedHashMap<>();

	private boolean isLocalhost;

	private String ipmitoolCommand;

	private int ipmiExecutionCount;

	private final Set<String> possibleWmiNamespaces = new TreeSet<>();

	private final Set<String> possibleWbemNamespaces = new TreeSet<>();

	private EngineConfiguration engineConfiguration;

	@Getter(value = AccessLevel.PRIVATE)
	private Map<String, ConnectorNamespace> connectorNamespaces = new HashMap<>();

	private PowerMeter powerMeter;

	@Override
	public void clear() {
		monitors.clear();
	}

	@Override
	public Monitor addMonitor(@NonNull Monitor monitor) {
		final String id = monitor.getId();
		Assert.notNull(id, MONITOR_ID_CANNOT_BE_NULL);

		final MonitorType monitorType = monitor.getMonitorType();
		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

		Assert.isTrue(MonitorType.TARGET.equals(monitorType) || Objects.nonNull(monitor.getParentId()),
			PARENT_ID_CANNOT_BE_NULL);

		Assert.notNull(monitor.getTargetId(), TARGET_ID_CANNOT_BE_NULL);

		Monitor created = monitor;

		if (monitors.containsKey(monitorType)) {

			Map<String, Monitor> map = monitors.get(monitorType);

			final Monitor previousMonitor = map.get(id);

			if (previousMonitor != null) {
				// Next discovery
				created = copyInformationFromCurrent(previousMonitor, monitor);
			} else {
				// first discovery
				map.put(id, monitor);
			}

		} else {
			// Very first monitor with this type
			monitors.put(monitorType, createLinkedHashMap(id, monitor));
		}

		// The monitor is created then it is present
		created.setAsPresent();

		// Generate alert rules
		created.getMonitorType().getMetaMonitor().accept(new MonitorAlertRulesVisitor(created));

		return created;
	}

	/**
	 * Copy the current monitor's information in the monitor previously discovered
	 * 
	 * @param previous Previously discovered Monitor instance
	 * @param current  Monitor from the current discovery
	 * @return merged monitor instance
	 */
	private Monitor copyInformationFromCurrent(@NonNull final Monitor previous, @NonNull final Monitor current) {

		previous.setName(current.getName());
		previous.setParentId(current.getParentId());
		previous.setTargetId(current.getTargetId());
		previous.setExtendedType(current.getExtendedType());
		previous.setDiscoveryTime(current.getDiscoveryTime());
		previous.setMetadata(current.getMetadata());

		return previous;
	}

	@Override
	public Monitor addMonitor(@NonNull final Monitor monitor, final String id,
			@NonNull final String connectorName, @NonNull final MonitorType monitorType,
			final String attachedToDeviceId, final String attachedToDeviceType) {

		Assert.notNull(monitor.getTargetId(), TARGET_ID_CANNOT_BE_NULL);

		monitor.setMonitorType(monitorType);

		if (monitor.getId() == null) {
			monitor.setId(buildMonitorId(connectorName, monitorType, monitor.getTargetId(), id));
		}

		if (monitor.getParentId() == null) {

			if (DISK_CONTROLLER_CHILDREN.contains(monitorType)) {

				String diskControllerId = lookupDiskControllerId(connectorName,
					monitor.getMetadata(DISK_CONTROLLER_NUMBER));

				if (diskControllerId != null) {
					monitor.setParentId(diskControllerId);
				}

			}

			if (monitor.getParentId() == null) {

				monitor.setParentId(buildParentId(monitor.getTargetId(), connectorName, attachedToDeviceId,
					attachedToDeviceType));
			}
		}

		return addMonitor(monitor);
	}

	/**
	 * Build the parent id based on the given arguments
	 * <ol>
	 * 	<li>If the <code>attachedToDeviceId</code> is present then for sure the parent is going to be the enclosure or another
	 *  monitor identified with <code>attachedToDeviceType</code>. In that case we deduce the parent key without reading instances from the memory.</li>
	 *  <li>If the <code>attachedToDeviceId </code> is not present then we will try to get the latest Enclosure monitor with the extended type 'Computer'</li>
	 *  <li>If the previous conditions fail then the monitor will be attached to the target (main monitor)</li>
	 * </ol>
	 * @param targetId             The identifier of the main monitor. Called target in the matrix-engine library
	 * @param connectorName        The connector compiled file name.
	 * @param attachedToDeviceId   The identifier of the monitor we wish to deduce its key
	 * @param attachedToDeviceType The type of the monitor we wish to deduce its key
	 * @return {@link String} value containing the key of the parent monitor
	 */
	String buildParentId(@NonNull final String targetId, @NonNull final String connectorName, 
			final String attachedToDeviceId, final String attachedToDeviceType) {
		// We have a parent defined by the connector
		if (attachedToDeviceId != null) {

			// Get the monitorType parent
			// By default, get the Enclosure
			final MonitorType monitorType = MonitorType.getByNameInConnectorOptional(attachedToDeviceType)
					.orElse(MonitorType.ENCLOSURE);
			return buildMonitorId(connectorName, monitorType, targetId, attachedToDeviceId);

		} else {

			// The parent is the enclosure monitor with the extended type 'Computer'
			final Map<String, Monitor> enclosures = this.selectFromType(MonitorType.ENCLOSURE);

			// No enclosures ?
			if (enclosures != null) {

				// Since the Stream interface does not provide a findLast() method,
				// we are reversing the Stream so that we can use findFirst()
				Optional<Monitor> computerEnclosure = StreamUtils
						.reverse(enclosures.values().stream())
						.filter(monitor -> COMPUTER.equalsIgnoreCase(monitor.getExtendedType())).findFirst();

				// We've found the enclosure then
				if (computerEnclosure.isPresent()) {
					return computerEnclosure.get().getId();
				}

				// If no 'Computer'-type enclosure found,
				// check if there is only one enclosure
				if (enclosures.size() == 1) {

					return enclosures
						.values()
						.stream()
						.findFirst()
						.orElseThrow()
						.getId();
				}
			}
		}

		// If we have no parent, attach object to the main device (target)
		return targetId;
	}

	/**
	 * Build the monitor unique identifier [connectorName]_[monitorType]_[targetId]_[id]
	 * @param connectorName  The connector compiled file name
	 * @param monitorType    The type of the monitor. See {@link MonitorType}
	 * @param targetId       The unique identifier of the main monitor called target
	 * @param id             The id of the monitor we wish to build its identifier
	 * @return {@link String} value containing the key of the monitor
	 */
	public static String buildMonitorId(@NonNull final String connectorName, @NonNull final MonitorType monitorType, 
			@NonNull final String targetId, @NonNull final String id) {
		return new StringBuilder()
				.append(connectorName)
				.append(ID_SEPARATOR)
				.append(monitorType.getKey())
				.append(ID_SEPARATOR)
				.append(targetId)
				.append(ID_SEPARATOR)
				.append(id.replaceAll("\\s*", ""))
				.toString();
	}

	private <K, V> Map<K, V> createLinkedHashMap(K key, V value) {
		Map<K, V> map = new LinkedHashMap<>();
		map.put(key, value);
		return map;
	}

	@Override
	public void removeMonitor(Monitor monitor) {
		if (monitor == null) {
			return;
		}

		final String id = monitor.getId();
		Assert.notNull(id, MONITOR_ID_CANNOT_BE_NULL);

		final MonitorType monitorType = monitor.getMonitorType();
		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

		removeRelatedChildren(id);

		if (monitors.containsKey(monitorType)) {
			Map<String, Monitor> instances = monitors.get(monitorType);

			if (null != instances) {
				instances.remove(id);
			}
		}

	}

	/**
	 * Remove the children of the monitor identified by the given
	 * <code>monitorId</code> recursively
	 *
	 * @param monitorId	The monitor's identifier.
	 */
	private void removeRelatedChildren(String monitorId) {
		monitors.values().stream().filter(Objects::nonNull).forEach(instances -> instances.entrySet().removeIf(entry ->
			{
				final Monitor monitor = entry.getValue();
				boolean remove = monitorId.equals(monitor.getParentId());
				if (remove) {
					removeRelatedChildren(monitor.getId());
				}
				return remove;
			}));
	}

	@Override
	public Map<String, Monitor> selectFromType(MonitorType monitorType) {
		return monitors.get(monitorType);
	}

	@Override
	public Monitor findById(@NonNull String monitorIdentifier) {
		return monitors
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor -> monitorIdentifier.equals(monitor.getId()))
			.findFirst()
			.orElse(null);
	}

	@Override
	public Set<Monitor> findChildren(final String parentId) {

		return monitors
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor ->
					(parentId == null && monitor.getParentId() == null)
					|| (parentId != null && parentId.equals(monitor.getParentId()))
			)
			.collect(Collectors.toSet());

	}

	@Override
	public String toJson() {

		final HostMonitoringVO hostMonitoringVO = new HostMonitoringVO();

		final List<MonitorType> monitorTypes = new ArrayList<>(monitors.keySet());

		monitorTypes.stream()
			.sorted(new MonitorTypeComparator())
			.filter(monitorType -> monitors.get(monitorType) != null && monitors.get(monitorType).values() != null)
			.forEach(monitorType ->
				{
					final List<Monitor> monitorList = new ArrayList<>(monitors.get(monitorType).values());
					Collections.sort(monitorList, new MonitorComparator());
					hostMonitoringVO.addAll(monitorList);
				});

		return JsonHelper.serialize(hostMonitoringVO);
	}

	@Override
	public Map<MonitorType, Map<String, Monitor>> selectFromTypes(MonitorType... monitorTypes) {

		return Collections.emptyMap();
	}

	@Override
	public void saveParameters() {
		monitors.values()
			.forEach(
				sameTypeMonitors -> sameTypeMonitors
					.values()
					.forEach(
						mo -> mo.getParameters()
							.values()
							.forEach(IParameter::save)
					)
			);
	}

	@Override
	public void addMissingMonitor(@NonNull Monitor monitor) {
		final String id = monitor.getId();
		Assert.notNull(id, MONITOR_ID_CANNOT_BE_NULL);

		final MonitorType monitorType = monitor.getMonitorType();
		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

		if (!monitorType.getMetaMonitor().hasPresentParameter()) {
			return;
		}

		// The monitor is created as missing
		monitor.setAsMissing();

		if (monitors.containsKey(monitorType)) {
			monitors.get(monitorType).put(id, monitor);
		} else {
			monitors.put(monitorType, createLinkedHashMap(id, monitor));
		}
	}

	/**
	 * Executes the given {@link IStrategy} instances and returns the result of the last execution.
	 *
	 * @param strategies	The {@link IStrategy} instances that should be executed.
	 *
	 * @return				The {@link EngineResult} resulting from the execution of the last given {@link IStrategy}.
	 */
	@Override
	public synchronized EngineResult run(final IStrategy... strategies) {

		final String hostname = engineConfiguration.getTarget().getHostname();

		log.trace("Hostname {} - Engine called for thread {}", hostname, Thread.currentThread().getName());

		checkEngineConfiguration();


		EngineResult lastEngineResult = null;

		for (IStrategy strategy : strategies) {

			log.trace("Hostname {} - Calling strategy {}", hostname, strategy.getClass().getSimpleName());
			lastEngineResult = run(strategy);
			log.info("Hostname {} - {} status {}", 
					hostname, strategy.getClass().getSimpleName(), lastEngineResult.getOperationStatus());

			if (log.isDebugEnabled()) {
				log.debug("Hostname {} - >>> {} >>>\n{}", hostname, strategy.getClass().getSimpleName(), toJson());
			}
		}

		return lastEngineResult;
	}

	/**
	 * Executes the given {@link IStrategy} and returns the result of the execution.
	 *
	 * @param strategy	The {@link IStrategy} that should be executed.
	 *
	 * @return			The {@link EngineResult} resulting from the execution of the given {@link IStrategy}.
	 */
	private EngineResult run(@NonNull final IStrategy strategy) {
		final ApplicationContext applicationContext = createApplicationContext(strategy);
		final String hostname = engineConfiguration.getTarget().getHostname();

		try {

			final boolean result = applicationContext.getBean(Context.class).executeStrategy();

			return EngineResult
				.builder()
				.hostMonitoring(this)
				.operationStatus(result ? OperationStatus.SUCCESS : OperationStatus.ERROR)
				.build();

		} catch (ExecutionException e) {

			Throwable cause = e.getCause();
			if (cause != null) {
				log.error(
						"Hostname {} - {} operation failed: {}: {}",
						hostname,
						strategy.getClass().getSimpleName(),
						cause.getClass().getSimpleName(),
						cause.getMessage()
				);
				log.debug("Hostname {} - Operation failed with ExecutionException", hostname, cause);
			} else {
				log.error("Hostname {} - {} operation failed: {}: {}", hostname, strategy.getClass().getSimpleName(),
						e.getClass().getSimpleName(), e.getMessage());
				log.debug("Hostname {} - Operation failed with ExecutionException", hostname, e);
			}

			return EngineResult
				.builder()
				.hostMonitoring(this)
				.operationStatus(OperationStatus.EXECUTION_EXCEPTION)
				.build();

		} catch (TimeoutException e) {

			log.error("Hostname {} - {} operation timeout!", hostname, strategy.getClass().getSimpleName());
			log.debug("Hostname {} - Operation failed with TimeoutException", hostname, e);

			return EngineResult
				.builder()
				.hostMonitoring(this)
				.operationStatus(OperationStatus.TIMEOUT_EXCEPTION)
				.build();

		} catch (InterruptedException e) {

			log.error("Hostname {} - {} operation interrupted", hostname, strategy.getClass().getSimpleName());
			log.debug("Hostname {} - Operation failed with InterruptedException", hostname, e);

			Thread.currentThread().interrupt();

			return EngineResult
				.builder()
				.hostMonitoring(this)
				.operationStatus(OperationStatus.INTERRUPTED_EXCEPTION)
				.build();

		} catch (Exception e) {

			log.error("Hostname {} - {} operation failed with {}", hostname, strategy.getClass().getSimpleName(), e.getClass().getSimpleName());
			log.debug("Hostname {} - Operation failed with unknown exception", hostname, e);

			return EngineResult
				.builder()
				.hostMonitoring(this)
				.operationStatus(OperationStatus.GENERAL_ERROR)
				.build();
		}
	}

	/**
	 * Checks the engine configuration
	 */
	private void checkEngineConfiguration() {

		Assert.notNull(engineConfiguration, "engineConfiguration cannot be null.");
		Assert.notNull(engineConfiguration.getProtocolConfigurations(), "protocolConfigurations cannot be null.");
		Assert.isTrue(!engineConfiguration.getProtocolConfigurations().isEmpty(), "protocolConfigurations cannot be empty.");
		Assert.notNull(engineConfiguration.getSelectedConnectors(), "selectedConnectors cannot be null.");
		Assert.notNull(engineConfiguration.getTarget(), "target cannot be null.");
		Assert.notNull(engineConfiguration.getTarget().getHostname(), "target hostname cannot be null.");
		Assert.notNull(engineConfiguration.getTarget().getType(), "target type cannot be null.");
		Assert.notNull(engineConfiguration.getTarget().getId(), "target id cannot be null.");
	}

	/**
	 * Creates a Spring {@link ApplicationContext} which provides a bean factory for accessing application components.
	 *
	 * @param strategy	The {@link IStrategy} for which the {@link ApplicationContext} should be created.
	 *
	 * @return {@link ApplicationContext}	The created {@link ApplicationContext}.
	 */
	private ApplicationContext createApplicationContext(final IStrategy strategy) {

		log.debug("Hostname {} - Creating spring context", engineConfiguration.getTarget().getHostname());

		final StrategyConfig strategyConfig = StrategyConfig
			.builder()
			.engineConfiguration(engineConfiguration)
			.hostMonitoring(this)
			.build();

		final AnnotationConfigApplicationContext configContext = new AnnotationConfigApplicationContext();
		configContext.getBeanFactory().destroySingletons();
		configContext.getBeanFactory().registerSingleton(STRATEGY_CONFIG_BEAN_NAME, strategyConfig);
		configContext.getBeanFactory().registerSingleton(STRATEGY_BEAN_NAME, strategy);
		configContext.getBeanFactory().registerSingleton(STRATEGY_TIME, new Date().getTime());

		// Register the configuration and components scan after singleton registrations
		// so that we can avoid the UnsatisfiedDependencyException
		configContext.register(ApplicationBeans.class);
		configContext.refresh();
		configContext.getBeanFactory().autowireBean(strategy);
		configContext.getBeanFactory().autowireBean(strategyConfig);

		return configContext;
	}

	/**
	 * @param connectorName			The name of the {@link Connector} containing the {@link DiskController}
	 *                              being searched for.
	 * @param diskControllerNumber	The number of the {@link DiskController} being searched for.
	 *
	 * @return						The ID of the {@link DiskController}
	 * 								discovered by the given {@link Connector}, and having the given number.
	 */
	private String lookupDiskControllerId(String connectorName, String diskControllerNumber) {

		Monitor result = null;

		Map<String, Monitor> diskControllers = selectFromType(MonitorType.DISK_CONTROLLER);
		if (diskControllers != null) {

			result = diskControllers
				.values()
				.stream()
				.filter(monitor -> monitor != null &&
											monitor.getMetadata(CONNECTOR) != null && monitor.getMetadata(CONNECTOR).equalsIgnoreCase(connectorName) &&
											monitor.getMetadata(DISK_CONTROLLER_NUMBER) != null && monitor.getMetadata(DISK_CONTROLLER_NUMBER).equals(diskControllerNumber))
				.findFirst()
				.orElse(null);
		}

		return result == null ? null : result.getId();
	}

	/**
	 * Extract the target monitor from the given {@link HostMonitoring}
	 * 
	 * @return {@link Monitor} instance ready to use
	 */
	public Monitor getTargetMonitor() {
		final Map<String, Monitor> targetMonitors = selectFromType(MonitorType.TARGET);
		if (targetMonitors == null || targetMonitors.isEmpty()) {
			return null;
		}

		return targetMonitors.values().stream().findFirst().orElse(null);
	}

	/**
	 *  These two classes are used to sort monitors and monitorTypes before adding them to a HostMonitoringVO
	 *  so that the parsing always returns the same value, and not with monitor types and monitors displayed randomly
	 */
	class MonitorComparator implements Comparator<Monitor> {
		@Override
		public int compare(Monitor a, Monitor b) {
			return a.getId().compareTo(b.getId());
		}
	}

	class MonitorTypeComparator implements Comparator<MonitorType> {
		@Override
		public int compare(MonitorType a, MonitorType b) {
			return a.name().compareTo(b.name());
		}
	}

	@Override
	public ConnectorNamespace getConnectorNamespace(final String connectorName) {
		synchronized (connectorNamespaces) {
			return connectorNamespaces.computeIfAbsent(connectorName, cn -> ConnectorNamespace.builder().build());
		}
	}

	@Override
	public ConnectorNamespace getConnectorNamespace(Connector connector) {
		return getConnectorNamespace(connector.getCompiledFilename());
	}

	public enum PowerMeter {
		COLLECTED, ESTIMATED;
	}
}
