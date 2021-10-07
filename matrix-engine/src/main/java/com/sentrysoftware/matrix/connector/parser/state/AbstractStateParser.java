package com.sentrysoftware.matrix.connector.parser.state;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.detection.snmp.OidProcessor;

import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COLLECT;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.DETECTION_DOT_CRITERIA;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.DISCOVERY;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.DOT_COMPUTE;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;

public abstract class AbstractStateParser implements IConnectorStateParser {

	protected abstract Class<?> getType();
	protected abstract String getTypeValue();
	protected abstract Matcher getMatcher(String key);

	private static final String INVALID_KEY = "Invalid key: ";

	private static final Set<String> TRUE_STRING_VALUES = Set.of("1", "true", "yes");

	/**
	 * Extracts the name of the {@link HardwareMonitor}
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The name of the {@link HardwareMonitor} is expected to be the first capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*<b><u>((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))</u></b>\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i><b><u>enclosure.discovery.source(1)</u></b>.type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>enclosure.discovery.source(1)</u></b></i>.
	 *
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The name of the {@link HardwareMonitor},
	 * 					which is supposed to be the first capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NullPointerException			If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 */
	protected String getSourceKey(Matcher matcher) {

		return matcher.group(1);
	}

	/**
	 * Extracts the name of the {@link HardwareMonitor}
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The name of the {@link HardwareMonitor} is expected to be the second capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*(<b><u>(.*)</u></b>\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i><b><u>enclosure</u></b>.discovery.source(1).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>enclosure</u></b></i>.
	 *
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The name of the {@link HardwareMonitor},
	 * 					which is supposed to be the second capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NullPointerException			If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no second capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 */
	private String getMonitorName(Matcher matcher) {

		return matcher.group(2);
	}

	/**
	 * Extracts the name of the {@link HardwareMonitor}'s job
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The name of the job is expected to be the third capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*((.*)\\.<b><u>(discovery|collect)</u></b>\\.source\\(([1-9]\\d*)\\))\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>enclosure.<b><u>discovery</u></b>.source(1).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>discovery</u></b></i>.
	 *
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The name of the {@link HardwareMonitor}'s job,
	 * 					which is supposed to be the third capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NullPointerException			If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no third capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 */
	private String getMonitorJobName(Matcher matcher) {

		return matcher.group(3);
	}

	/**
	 * Extracts the index of the {@link Source}
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The index of the {@link Source} is expected to be the fourth capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*((.*)\\.(discovery|collect)\\.source\\(<b><u>([1-9]\\d*)</u></b>\\))\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>enclosure.discovery.source(<b><u>1</u></b>).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>1</u></b></i>.
	 *
	 * @param matcher   A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The index of the {@link Source},
	 * 					which is supposed to be the fourth capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NullPointerException			If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no fourth capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NumberFormatException		if the fourth capturing group does not contain any parsable integer.
	 */
	protected int getSourceIndex(Matcher matcher) {

		return Integer.parseInt(matcher.group(4));
	}

	/**
	 * Extracts the index of the {@link Compute}
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The index of the {@link Compute} is expected to be the fifth capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(<b><u>([1-9]\\d*)</u></b>\\)\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>enclosure.discovery.source(1).compute(<b><u>2</u></b>).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>2</u></b></i>.
	 *
	 * @param matcher   A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The index of the {@link Compute},
	 * 					which is supposed to be the fifth capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NullPointerException			If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no fifth capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NumberFormatException		if the fifth capturing group does not contain any parsable integer.
	 */
	protected int getComputeIndex(Matcher matcher) {
	
		return Integer.parseInt(matcher.group(5));
	}

	/**
	 * Extracts the index of the {@link Criterion}
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The index of the {@link Criterion} is expected to be the first capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>detection.criteria(<b><u>1</u></b>).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>1</u></b></i>.
	 *
	 * @param matcher   A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The index of the {@link Criterion},
	 * 					which is supposed to be the first capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NullPointerException			If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NumberFormatException		if the first capturing group does not contain any parsable integer.
	 */
	protected int getCriterionIndex(Matcher matcher) {

		return Integer.parseInt(matcher.group(1));
	}

	/**
	 * Extracts the index of the {@link Step}
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The index of the {@link Step} is expected to be the first capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.Step\\(([1-9]\\d*)\\)\\.Type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>detection.criteria(<b><u>1</u></b>).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>1</u></b></i>.
	 *
	 * @param matcher   A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The index of the {@link Step},
	 * 					which is supposed to be the first capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NullPointerException			If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NumberFormatException		if the first capturing group does not contain any parsable integer.
	 */
	protected static int getDetectionStepIndex(final Matcher matcher) {
		return getStepIndex(matcher, 2);
	}

	/**
	 * Extracts the index of the {@link Step}
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The index of the {@link Step} is expected to be the first capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.Step\\(([1-9]\\d*)\\)\\.Type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>detection.criteria(<b><u>1</u></b>).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>1</u></b></i>.
	 *
	 * @param matcher   A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The index of the {@link Step},
	 * 					which is supposed to be the first capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NullPointerException			If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NumberFormatException		if the first capturing group does not contain any parsable integer.
	 */
	protected static int getSourceStepIndex(final Matcher matcher) {
		return getStepIndex(matcher, 5);
	}

	private static int getStepIndex(final Matcher matcher, final int group) {
		return Integer.parseInt(matcher.group(group));
	}

	/**
	 * @param name		The name of the {@link TranslationTable} being searched for.
	 * @param connector	The {@link Connector} whose {@link TranslationTable} is being searched for.
	 *
	 * @return			The {@link TranslationTable} having the given name in the given {@link Connector},
	 * 					if available.
	 */
	protected TranslationTable getTranslationTable(String name, Connector connector) {

		Map<String, TranslationTable> translationTables = connector.getTranslationTables();
		state(translationTables != null, () -> "No translation tables found in " + connector.getCompiledFilename());

		TranslationTable translationTable = translationTables.get(name);
		state(translationTable != null,
			() -> "Could not find translation table " + name + " in " + connector.getCompiledFilename());

		return translationTable;
	}

	/**
	 * @param connector				The {@link Connector} whose {@link HardwareMonitor} is being search for.
	 * @param monitorName			The name of the {@link HardwareMonitor} being searched for.
	 * @param monitorJobName		The name of the current job (<i>discovery</i> or <i>collect</i>).
	 * @param createMonitorIfNull	Indicates whether a new {@link HardwareMonitor} should be created if none was found.
	 *
	 * @return						The {@link HardwareMonitor} in the given {@link Connector}
	 *								whose name matches the given monitor name, if available.
	 */
	private HardwareMonitor getHardwareMonitor(@NonNull Connector connector, String monitorName, String monitorJobName,
											   boolean createMonitorIfNull) {
	
		HardwareMonitor hardwareMonitor = connector
			.getHardwareMonitors()
			.stream()
			.filter(instance -> instance
				.getType()
				.getNameInConnector()
				.equalsIgnoreCase(monitorName))
			.findFirst()
			.orElseGet(() -> createMonitorIfNull ? createHardwareMonitor(monitorName, connector) : null);

		if (hardwareMonitor != null) {

			if (hardwareMonitor.getDiscovery() == null && DISCOVERY.equalsIgnoreCase(monitorJobName)) {
				hardwareMonitor.setDiscovery(Discovery.builder().build());
			}

			if (hardwareMonitor.getCollect() == null && COLLECT.equalsIgnoreCase(monitorJobName)) {
				hardwareMonitor.setCollect(Collect.builder().build());
			}
		}

		return hardwareMonitor;
	}
	/**
	 * @param monitorName	The name of the monitor type.
	 * @param connector		The {@link Connector} to which the newly created {@link HardwareMonitor} should be added.
	 *
	 * @return				The newly created {@link HardwareMonitor} instance.
	 */
	private HardwareMonitor createHardwareMonitor(final String monitorName, final Connector connector) {

		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.discovery(Discovery.builder().build())
			.collect(Collect.builder().build())
			.type(MonitorType.getByNameInConnector(monitorName))
			.build();

		// Add the hardware monitor to the connector
		connector.getHardwareMonitors().add(hardwareMonitor);

		return hardwareMonitor;
	}

	/**
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher},
	 *					whose match operation has been called successfully,
	 *					and from which
	 *					a {@link HardwareMonitor} name and a job name can be extracted.
	 * @param connector	The {@link Connector} whose {@link MonitorJob} is being searched for.
	 *
	 * @return			The {@link MonitorJob} in the given {@link Connector}
	 * 					matching the given {@link Matcher}, if available.
	 */
	protected MonitorJob getMonitorJob(Matcher matcher, Connector connector) {

		String monitorName = getMonitorName(matcher);
		String monitorJobName = getMonitorJobName(matcher);
		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, monitorName, monitorJobName, true);

		return DISCOVERY.equalsIgnoreCase(monitorJobName)
			? hardwareMonitor.getDiscovery() // NOSONAR - hardwareMonitor is never null here
			: hardwareMonitor.getCollect(); // NOSONAR - hardwareMonitor is never null here
	}

	/**
	 * @param hardwareMonitor	The {@link HardwareMonitor} whose job name is being searched for.
	 * @param monitorJobName	The name of the job (i.e. <i>discovery</i> or <i>collect</i>)
	 *                          in the given {@link HardwareMonitor}
	 *                          whose {@link Source} is being searched for.
	 * @param sourceIndex		The index of the {@link Source} in the given job.
	 * @param ignoreSourceType	Indicates whether the {@link Source} type should be ignored or not when filtering.
	 *
	 * @return					The {@link Source} with the given index in he given {@link HardwareMonitor} job,
	 * 							if available.
	 */
	@SuppressWarnings("unchecked")
	private <T extends Source> T getSource(HardwareMonitor hardwareMonitor, String monitorJobName, int sourceIndex,
										   boolean ignoreSourceType) {
	
		if (hardwareMonitor == null) {
			return null;
		}
	
		MonitorJob monitorJob = DISCOVERY.equalsIgnoreCase(monitorJobName)
			? hardwareMonitor.getDiscovery()
			: hardwareMonitor.getCollect();

		// monitorJob cannot be null here
		List<Source> sources = monitorJob.getSources();
		if (sources == null) {
			return null;
		}

		return (T) sources
			.stream()
			.filter(source -> (ignoreSourceType || getType().isInstance(source)) && source.getIndex() == sourceIndex)
			.findFirst()
			.orElse(null);
	}

	/**
	 * @param matcher			A <u>NON-NULL</u> {@link Matcher},
	 * 							whose match operation has been called successfully,
	 * 							and from which
	 * 							a {@link HardwareMonitor} name, a job name
	 * 							and a {@link Source} index can be extracted.
	 * @param connector			The {@link Connector} whose source is being searched for.
	 * @param ignoreSourceType	Indicates whether the {@link Source} type should be ignored or not when filtering.
	 *
	 * @return					The {@link Source} in the given {@link Connector}
	 * 							matching the given {@link Matcher}, if available.
	 */
	protected <T extends Source> T getSource(Matcher matcher, Connector connector, boolean ignoreSourceType) {
	
		String monitorName = getMonitorName(matcher);
		String monitorJobName = getMonitorJobName(matcher);
		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, monitorName, monitorJobName, false);
	
		return getSource(hardwareMonitor, monitorJobName, getSourceIndex(matcher), ignoreSourceType);
	}

	/**
	 * @param key		The key to parse.
	 * @param connector	The {@link Connector} whose {@link Source} is being searched for.
	 * @param <T>		The type of {@link Source} being searched for.
	 *
	 * @return			The {@link Source} matching the given key.
	 */
	protected <T extends Source> T getSource(String key, Connector connector) {

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> INVALID_KEY + key + ConnectorParserConstants.DOT);

		T source = getSource(matcher, connector, false);
		notNull(source,
			() -> "Could not find any Source for the following key: " + key + ConnectorParserConstants.DOT);

		return source;
	}

	/**
	 * @param <T>			A sub-type of the {@link Compute} class.
	 * @param computes		A list of {@link Compute}s.
	 * @param computeIndex	The index of the {@link Compute} being searched for.
	 *
	 * @return				The {@link Compute} in the given list having the given index.
	 */
	@SuppressWarnings("unchecked")
	private <T extends Compute> T getCompute(List<Compute> computes, int computeIndex) {
	
		if (computes == null) {
			return null;
		}
	
		return (T) computes
			.stream()
			.filter(compute -> getType().isInstance(compute) && compute.getIndex() == computeIndex)
			.findFirst()
			.orElse(null);
	}

	/**
	 *
	 * @param <T>			A sub-type of the {@link Compute} class.
	 * @param source		The {@link Source} whose {@link Compute} is being searched for.
	 * @param computeIndex	The index of the {@link Compute} being searched for.
	 *
	 * @return				The {@link Compute} of the given {@link Source} having the given index, if available.
	 */
	protected <T extends Compute> T getCompute(Source source, int computeIndex) {
	
		return source == null
			? null
			: getCompute(source.getComputes(), computeIndex);
	}

	/**
	 * @param <T>		A sub-type of the {@link Compute} class.
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher},
	 * 					whose match operation has been called successfully,
	 * 					and from which
	 * 					a {@link HardwareMonitor} name, a job name, a {@link Source} index and a {@link Compute} index
	 * 					can be extracted.
	 * @param connector	The {@link Connector} whose {@link Compute} is being searched for.
	 *
	 * @return			The {@link Compute} in the given {@link Connector} matching the given {@link Matcher}.
	 */
	protected <T extends Compute> T getCompute(Matcher matcher, Connector connector) {

		String monitorJobName = getMonitorJobName(matcher);
		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, getMonitorName(matcher), monitorJobName, false);
	
		Source source = getSource(hardwareMonitor, monitorJobName, getSourceIndex(matcher), true);
	
		return getCompute(source, getComputeIndex(matcher));
	}

	/**
	 * @param key		The key to parse.
	 * @param connector	The {@link Connector} whose {@link Compute} is being searched for.
	 * @param <T>		The type of {@link Compute} being searched for.
	 *
	 * @return			The {@link Compute} matching the given key.
	 */
	protected <T extends Compute> T getCompute(String key, Connector connector) {

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> INVALID_KEY + key + ConnectorParserConstants.DOT);

		T compute = getCompute(matcher, connector);
		notNull(compute,
			() -> "Could not find any Compute for the following key: " + key + ConnectorParserConstants.DOT);

		return compute;
	}

	/**
	 * @param value		The value of the {@link Source} type,
	 *                  in case <i>this</i> is a <i>TypeProcessor</i>
	 * @param matcher	The {@link Matcher} used to retrieve the {@link Source}
	 *                  in case <i>this</i> is not a <i>TypeProcessor</i>
	 * @param connector	The {@link Connector} used to retrieve the {@link Source}
	 * 					in case <i>this</i> is not a <i>TypeProcessor</i>
	 *
	 * @return			<ul>
	 * 						<li>
	 * 							<b>true</b> if:
	 * 							<ul>
	 * 								<li><i>this</i> is a <i>TypeProcessor</i> whose type matches the given value.</li>
	 * 								<li>
	 * 									<i>this</i> is not a <i>TypeProcessor</i>
	 * 									and there is a {@link Source} in the given {@link Connector}
	 * 									matching the given {@link Matcher}.
	 * 								</li>
	 * 							</ul>
	 * 						</li>
	 * 						<li><b>false</b> otherwise.</li>
	 *					</ul>
	 */
	private boolean isSourceContext(String value, Matcher matcher, Connector connector) {

		if (this instanceof com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor ||
				this instanceof com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step.TypeProcessor) {

			return getTypeValue().equalsIgnoreCase(value);
		}

		return this instanceof com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step.StepProcessor ?
				getSourceStep(matcher, connector) != null :
				getSource(matcher, connector, false) != null;
	}

	/**
	 * @param value		The value of the {@link Compute} type,
	 *                  in case <i>this</i> is a <i>TypeProcessor</i>
	 * @param matcher	The {@link Matcher} used to retrieve the {@link Compute}
	 *                  in case <i>this</i> is not a <i>TypeProcessor</i>
	 * @param connector	The {@link Connector} used to retrieve the {@link Compute}
	 * 					in case <i>this</i> is not a <i>TypeProcessor</i>
	 *
	 * @return			<ul>
	 * 						<li>
	 * 							<b>true</b> if:
	 * 							<ul>
	 * 								<li><i>this</i> is a <i>TypeProcessor</i> whose type matches the given value.</li>
	 * 								<li>
	 * 									<i>this</i> is not a <i>TypeProcessor</i>
	 * 									and there is a {@link Compute} in the given {@link Connector}
	 * 									matching the given {@link Matcher}.
	 * 								</li>
	 * 							</ul>
	 * 						</li>
	 * 						<li><b>false</b> otherwise.</li>
	 *					</ul>
	 */
	private boolean isComputeContext(String value, Matcher matcher, Connector connector) {
	
		if (this instanceof com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor) {
	
			return getTypeValue().equalsIgnoreCase(value);
		}
	
		return getCompute(matcher, connector) != null;
	}

	/**
	 * @param <T>		A sub-type of the {@link Criterion} class.
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher},
	 * 					whose match operation has been called successfully,
	 * 					and from which a {@link Criterion} index can be extracted.
	 * @param connector	The {@link Connector} whose {@link Criterion} is being searched for.
	 *
	 * @return			The {@link Criterion} in the given {@link Connector} matching the given {@link Matcher}.
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Criterion> T getCriterion(Matcher matcher, @NonNull Connector connector) {

		Detection detection = connector.getDetection();
		if (detection == null) {
			return null;
		}

		List<Criterion> criteria = detection.getCriteria();
		if (criteria == null) {
			return null;
		}

		int criterionIndex = getCriterionIndex(matcher);

		return (T) criteria
			.stream()
			.filter(criterion -> getType().isInstance(criterion) && criterion.getIndex() == criterionIndex)
			.findFirst()
			.orElse(null);
	}

	/**
	 * @param key		The key to parse.
	 * @param connector	The {@link Connector} whose {@link Criterion} is being searched for.
	 * @param <T>		The type of {@link Criterion} being searched for.
	 *
	 * @return			The {@link Criterion} matching the given key.
	 */
	protected <T extends Criterion> T getCriterion(String key, Connector connector) {

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> INVALID_KEY + key + ConnectorParserConstants.DOT);

		T criterion = getCriterion(matcher, connector);
		notNull(criterion,
			() -> "Could not find any Criterion for the following key: " + key + ConnectorParserConstants.DOT);

		return criterion;
	}

	/**
	 * 
	 * @param matcher	The {@link Matcher} used to retrieve the {@link Step}
	 *                  in case <i>this</i> is not a <i>TypeProcessor</i>
	 * @param connector The {@link Connector} whose {@link Step} is being searched for.
	 * @return The type of {@link Step} being searched for.
	 */
	protected Step getDetectionStep(final Matcher matcher, final Connector connector) {

		final Detection detection = connector.getDetection();
//		state(detection != null, "No detection in the connector");
		if (detection == null) {
			return null;
		}

		final List<Criterion> criteria = detection.getCriteria();
//		state(criteria != null, "No criteria in the connector.");
		if (criteria == null) {
			return null;
		}

		final int criterionIndex = getCriterionIndex(matcher);
//		state(
//				criterionIndex <= criteria.size(),
//				() -> String.format("Criterion %d not detected yet.", criterionIndex));
		if (criterionIndex > criteria.size()) {
			return null;
		}

		final Criterion criterion = criteria.get(criterionIndex - 1);
//		state(criterion != null && criterion instanceof SshInteractive, () -> "criterion should be SshInteractive");
		if (criterion == null || !(criterion instanceof SshInteractive)) {
			return null;
		}

		final SshInteractive sshInteractive = (SshInteractive) criterion;

		final int stepIndex = getDetectionStepIndex(matcher);

		return sshInteractive.getSteps().stream()
				.filter(step -> step.getIndex() != null && step.getIndex() == stepIndex)
				.findFirst()
				.orElse(null);
	}

	/**
	 * 
	 * @param key The key to parse.
	 * @param connector The {@link Connector} whose {@link Step} is being searched for.
	 * @return The type of {@link Step} being searched for.
	 */
	protected Step getDetectionStep(final String key, final Connector connector) {

		final Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> String.format("Invalid key: %s.", key));

		final Detection detection = connector.getDetection();
		state(detection != null, () -> String.format("No detection in the connector for the following key: %s.", key));

		final List<Criterion> criteria = detection.getCriteria();
		state(criteria != null, () -> String.format("No criteria in the connector for the following key: %s.", key));

		final int criterionIndex = getCriterionIndex(matcher);
		state(
				criterionIndex <= criteria.size(),
				() -> String.format("Criterion %d not detected yet for the following key: %s.", criterionIndex, key));

		final Criterion criterion = criteria.get(criterionIndex - 1);
		state(criterion != null && criterion instanceof SshInteractive, () -> "criterion should be SshInteractive");

		final SshInteractive sshInteractive = (SshInteractive) criterion;

		final int stepIndex = getDetectionStepIndex(matcher);

		return sshInteractive.getSteps().stream()
				.filter(step -> step.getIndex() != null && step.getIndex() == stepIndex)
				.findFirst()
				.orElse(null);
	}

	/**
	 * 
	 * @param matcher The key matcher.
	 * @param connector The {@link Connector} whose {@link Step} is being searched for.
	 * @return The type of {@link SshInteractiveSource} being searched for.
	 */
	protected SshInteractiveSource getSshInteractiveSource(
			final Matcher matcher,
			final Connector connector) {

		final String monitorJobName = getMonitorJobName(matcher);

		final String monitorName = getMonitorName(matcher);

		final HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, monitorName, monitorJobName, false);

		final int sourceIndex = getSourceIndex(matcher);

		final Source source = getSource(hardwareMonitor, monitorJobName, sourceIndex, true);
		state(
				source != null && source instanceof SshInteractiveSource,
				"No SshInteractiveSource in the connector.");

		return (SshInteractiveSource) source;
	}

	/**
	 * 
	 * @param matcher	The {@link Matcher} used to retrieve the {@link Source}
	 *                  in case <i>this</i> is not a <i>TypeProcessor</i>
	 * @param connector The {@link Connector} whose {@link Step} is being searched for.
	 * @return The type of {@link Step} being searched for.
	 */
	protected Step getSourceStep(final Matcher matcher, final Connector connector) {

		final SshInteractiveSource sshInteractiveSource = getSshInteractiveSource(matcher, connector);

		final int stepIndex = getSourceStepIndex(matcher);

		return sshInteractiveSource.getSteps().stream()
				.filter(step -> step.getIndex() != null && step.getIndex() == stepIndex)
				.findFirst()
				.orElse(null);
	}

	/**
	 * 
	 * @param key The key to parse.
	 * @param connector The {@link Connector} whose {@link Step} is being searched for.
	 * @return The type of {@link Step} being searched for.
	 */
	protected Step getSourceStep(final String key, final Connector connector) {

		final Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> String.format("Invalid key: %s.", key));

		return getSourceStep(matcher, connector);
	}

	/**
	 * Defined if the string value is the boolean true value.
	 *  
	 * @param value The string value
	 * @return true if "true", "yes" or "1".
	 */
	protected static boolean isStringTrueValue(final String value) {
		return value != null && TRUE_STRING_VALUES.contains(value.toLowerCase());
	}

	/**
	 *
	 * @param value		The value of the {@link Criterion} type,
	 *                  in case <i>this</i> is a <i>TypeProcessor</i>
	 * @param matcher   The {@link Matcher} used to retrieve the {@link Criterion}
	 *                  in case <i>this</i> is not an <i>OidProcessor</i>
	 * @param connector    The {@link Connector} used to retrieve the {@link Criterion}
	 * 					in case <i>this</i> is not an <i>OidProcessor</i>
	 *
	 * @return			<ul>
	 * 						<li>
	 * 							<b>true</b> if:
	 * 							<ul>
	 * 								<li><i>this</i> is a <i>TypeProcessor</i> whose type matches the given value.</li>
	 * 								<li><i>this</i> is an <i>OidProcessor</i>.</li>
	 * 								<li>
	 * 									<i>this</i> is neither a <i>TypeProcessor</i> nor an <i>OidProcessor</i>
	 * 									and there is a {@link Compute} in the given {@link Connector}
	 * 									matching the given {@link Matcher}.
	 * 								</li>
	 * 							</ul>
	 * 						</li>
	 * 						<li><b>false</b> otherwise.</li>
	 *					</ul>
	 */
	private boolean isCriterionContext(String value, Matcher matcher, Connector connector) {

		if (this instanceof com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor ||
				this instanceof com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.TypeProcessor) {

			return getTypeValue().equalsIgnoreCase(value);
		}

		//steps
		if (this instanceof com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.StepProcessor) {
			return getDetectionStep(matcher, connector) != null;
		}
		return (this instanceof OidProcessor) || getCriterion(matcher, connector) != null;
	}

	/**
	 * @param key       The key will determine which context evaluation method should be called:<br>
	 *                  <ul>
	 *                  	<li>
	 *                      	if the key starts with "detection.criteria" (ignoring case),
	 *                      	the {@link Criterion} context evaluation method will be called.
	 *                  	</li>
	 *                  	<li>
	 *                      	if the key contains ".compute" (ignoring case),
	 *                      	the {@link Compute} context evaluation method will be called.
	 *                  	</li>
	 *                  	<li>otherwise, the {@link Source} context evaluation will be called.</li>
	 *                  </ul>
	 * @param value     The value used to determine the context.
	 * @param matcher	The {@link Matcher} used to determine the context.
	 * @param connector	The {@link Connector} used to determine the context.
	 *
	 * @return			Whether or not the context evaluation method call evaluates to <i>true</i>.
	 */
	private boolean isAccurateContext(String key, String value, Matcher matcher, Connector connector) {

		if (key.startsWith(DETECTION_DOT_CRITERIA)) {
			return isCriterionContext(value, matcher, connector);
		}

		return key.contains(DOT_COMPUTE)
			? isComputeContext(value, matcher, connector)
			: isSourceContext(value, matcher, connector);
	}

	@Override
	public boolean detect(final String key, final String value, final Connector connector) {
	
		Matcher matcher;

		return value != null
			&& key != null
			&& (matcher = getMatcher(key)).matches() //NOSONAR - Assigning matcher on purpose
			&& isAccurateContext(key, value, matcher, connector);
	}

	@Override
	public void parse(@NonNull final String key, @NonNull final String value, @NonNull final Connector connector) {
		// Not implemented
		// Implementation provided by lombok
	}
}
