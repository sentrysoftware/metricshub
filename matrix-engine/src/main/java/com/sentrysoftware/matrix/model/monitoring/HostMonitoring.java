package com.sentrysoftware.matrix.model.monitoring;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.common.helpers.StreamUtils;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.OperationStatus;
import com.sentrysoftware.matrix.engine.configuration.ApplicationBeans;
import com.sentrysoftware.matrix.engine.strategy.Context;
import com.sentrysoftware.matrix.engine.strategy.IStrategy;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;

@Data
@NoArgsConstructor
@Slf4j
public class HostMonitoring implements IHostMonitoring {

	private static final String MONITOR_ID_CANNOT_BE_NULL = "monitor id cannot be null.";
	private static final String PARENT_ID_CANNOT_BE_NULL = "Parent Id cannot be null.";
	private static final String TARGET_ID_CANNOT_BE_NULL = "Target Id cannot be null.";
	private static final String MONITOR_TYPE_CANNOT_BE_NULL = "monitor type cannot be null.";
	private static final String CONNECTOR_NAME_CANNOT_BE_NULL = "connectorName cannot be null.";
	private static final String MONITOR_CANNOT_BE_NULL = "monitor cannot be null.";

	private static final String STRATEGY_TIME = "strategyTime";
	private static final String STRATEGY_BEAN_NAME = "strategy";
	private static final String STRATEGY_CONFIG_BEAN_NAME = "strategyConfig";

	public static final HostMonitoring HOST_MONITORING = new HostMonitoring();

	private Map<MonitorType, Map<String, Monitor>> monitors = new LinkedHashMap<>();
	private Map<MonitorType, Map<String, Monitor>> previousMonitors = new LinkedHashMap<>();
	private Map<String, SourceTable> sourceTables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);


	private boolean isLocalhost;

	private String ipmitoolCommand;
	private int ipmiExecutionCount;

	private String automaticWmiNamespace;
	private final Set<String> possibleWmiNamespaces = new TreeSet<>();

	private String automaticWbemNamespace;
	private final Set<String> possibleWbemNamespaces = new TreeSet<>();

	private EngineConfiguration engineConfiguration;

	@Override
	public void clearCurrent() {
		monitors.clear();
	}

	@Override
	public void clearPrevious() {
		previousMonitors.clear();
	}

	@Override
	public void backup() {
		previousMonitors.putAll(monitors);
	}

	@Override
	public void addMonitor(Monitor monitor) {

		Assert.notNull(monitor, MONITOR_CANNOT_BE_NULL);

		final String id = monitor.getId();
		Assert.notNull(id, MONITOR_ID_CANNOT_BE_NULL);

		final MonitorType monitorType = monitor.getMonitorType();
		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

		Assert.isTrue(MonitorType.TARGET.equals(monitorType) || Objects.nonNull(monitor.getParentId()),
			PARENT_ID_CANNOT_BE_NULL);

		Assert.notNull(monitor.getTargetId(), TARGET_ID_CANNOT_BE_NULL);

		// The monitor is created then it is present
		monitor.setAsPresent();

		//

		if (monitors.containsKey(monitorType)) {

			Map<String, Monitor> map = monitors.get(monitorType);

			final Monitor previousMonitor = map.get(id);

			// Copy the parameters from the monitor instance previously collected
			copyParameters(previousMonitor, monitor);

			// Copy the alert rules from the monitor instance previously collected
			copyAlertRules(previousMonitor, monitor);

			map.put(id, monitor);

		} else {
			monitors.put(monitorType, createLinkedHashMap(id, monitor));
		}
	}

	/**
	 * Copy parameters from previous to current monitor. <br>
	 * If the parameter is already collected the parameter's copy is skipped. E.g a present parameter set in the discovery
	 *
	 * @param previous The monitor previously collected by the {@link CollectOperation} strategy
	 * @param current  The monitor to created passed by the {@link DiscoveryOperation} or {@link DetectionOperation} strategy
	 */
	static void copyParameters(final Monitor previous, final Monitor current) {
		// This means that we are in the first discovery or a new monitor has been discovered
		// Nothing to copy, just return
		if (previous == null) {
			return;
		}

		// Copy the parameters from previous, skip parameters already collected
		previous.getParameters()
			.entrySet()
			.stream()
			.filter(entry -> !current.getParameters().containsKey(entry.getKey()))
			.map(Entry::getValue)
			.forEach(current::addParameter);

	}

	/**
	 * Copy alert rules from previous to current monitor.
	 *
	 * @param previous The monitor previously collected by the {@link CollectOperation} strategy
	 * @param current  The monitor to created passed by the {@link DiscoveryOperation} or {@link DetectionOperation} strategy
	 */
	static void copyAlertRules(Monitor previous, Monitor current) {
		// This means that we are in the first discovery or a new monitor has been discovered
		// Nothing to copy, just return
		if (previous == null) {
			return;
		}

		// Copy the alert rules from previous, skip alert rules already created
		previous.getAlertRules()
			.entrySet()
			.stream()
			.filter(entry -> !current.getAlertRules().containsKey(entry.getKey()))
			.forEach(entry -> current.addAlertRules(entry.getKey(), entry.getValue()));
	}

	@Override
	public void addMonitor(final Monitor monitor, final String id,
			final String connectorName, final MonitorType monitorType,
			final String attachedToDeviceId, final String attachedToDeviceType) {
		Assert.notNull(monitor, MONITOR_CANNOT_BE_NULL);
		Assert.notNull(connectorName, CONNECTOR_NAME_CANNOT_BE_NULL);
		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);
		Assert.notNull(monitor.getTargetId(), TARGET_ID_CANNOT_BE_NULL);

		monitor.setMonitorType(monitorType);

		if (monitor.getId() == null) {
			monitor.setId(buildMonitorId(connectorName, monitorType, monitor.getTargetId(), id));
		}

		if (monitor.getParentId() == null) {
			monitor.setParentId(buildParentId(monitor.getTargetId(), connectorName, attachedToDeviceId, attachedToDeviceType));
		}

		addMonitor(monitor);
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
	String buildParentId(final String targetId, final String connectorName, final String attachedToDeviceId, final String attachedToDeviceType) {

		Assert.notNull(targetId, TARGET_ID_CANNOT_BE_NULL);
		Assert.notNull(connectorName, CONNECTOR_NAME_CANNOT_BE_NULL);

		// We have a parent defined by the connector
		if (attachedToDeviceId != null) {

			// Get the monitorType parent
			// By default, get the Enclosure
			final MonitorType monitorType = MonitorType.getByNameOptional(attachedToDeviceType)
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
	public static String buildMonitorId(final String connectorName, final MonitorType monitorType, final String targetId, final String id) {

		Assert.notNull(connectorName, CONNECTOR_NAME_CANNOT_BE_NULL);
		Assert.notNull(targetId, TARGET_ID_CANNOT_BE_NULL);
		Assert.notNull(id, MONITOR_ID_CANNOT_BE_NULL);
		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

		return new StringBuilder()
				.append(connectorName)
				.append(HardwareConstants.ID_SEPARATOR)
				.append(monitorType.getName().toLowerCase())
				.append(HardwareConstants.ID_SEPARATOR)
				.append(targetId)
				.append(HardwareConstants.ID_SEPARATOR)
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
		if (null == monitor) {
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
	public Set<Monitor> selectChildren(String parentIdentifier, MonitorType childrenMonitorType) {

		return Collections.emptySet();
	}

	@Override
	public Monitor findById(String monitorIdentifier) {

		Assert.notNull(monitorIdentifier, "monitorIdentifier cannot be null.");

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
	public void addSourceTable(String key, SourceTable sourceTable) {
		Assert.notNull(key, "The key cannot be null.");
		Assert.notNull(sourceTable, "The sourceTable cannot be null.");

		sourceTables.put(key, sourceTable);
	}

	@Override
	public SourceTable getSourceTableByKey(String key) {
		Assert.notNull(key, "The key cannot be null.");

		return sourceTables.get(key);
	}

	@Override
	public void resetParameters() {
		monitors.values().forEach(
				sameTypeMonitors -> sameTypeMonitors.values().forEach(
						mo -> mo.getParameters().values().forEach(
								IParameterValue::reset)));
	}

	@Override
	public void addMissingMonitor(Monitor monitor) {
		Assert.notNull(monitor, MONITOR_CANNOT_BE_NULL);

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

		log.error("Engine called for thread {}", Thread.currentThread().getName());

		checkEngineConfiguration();

		EngineResult lastEngineResult = null;

		for (IStrategy strategy : strategies) {

			log.info("Calling strategy {}", strategy.getClass().getSimpleName());
			lastEngineResult = run(strategy);
			log.info("{} status {}", strategy.getClass().getSimpleName(), lastEngineResult.getOperationStatus());
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
	private EngineResult run(final IStrategy strategy) {

		Assert.notNull(strategy, "strategy cannot be null.");

		final ApplicationContext applicationContext = createApplicationContext(strategy);

		try {

			final boolean result = applicationContext.getBean(Context.class).executeStrategy();

			return EngineResult
				.builder()
				.hostMonitoring(this)
				.operationStatus(result ? OperationStatus.SUCCESS : OperationStatus.ERROR)
				.build();

		} catch (ExecutionException e) {

			log.error("Execution error", e);

			return EngineResult
				.builder()
				.hostMonitoring(this)
				.operationStatus(OperationStatus.EXECUTION_EXCEPTION)
				.build();

		} catch (TimeoutException e) {

			log.error("Timeout error", e);

			return EngineResult
				.builder()
				.hostMonitoring(this)
				.operationStatus(OperationStatus.TIMEOUT_EXCEPTION)
				.build();

		} catch(InterruptedException e) {

			log.error("Interrupted error", e);

			Thread.currentThread().interrupt();

			return EngineResult
				.builder()
				.hostMonitoring(this)
				.operationStatus(OperationStatus.INTERRUPTED_EXCEPTION)
				.build();

		} catch (Exception e) {

			log.error("Unknown exception", e);

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

		log.debug("Creating spring context");

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
}
