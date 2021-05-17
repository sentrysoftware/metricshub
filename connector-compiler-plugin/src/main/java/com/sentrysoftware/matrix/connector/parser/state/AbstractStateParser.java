package com.sentrysoftware.matrix.connector.parser.state;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COMPUTE;
import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.DOT;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public abstract class AbstractStateParser implements IConnectorStateParser {

	protected abstract Class<?> getType();
	protected abstract String getTypeValue();
	protected abstract Matcher getMatcher(String key);

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
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher}
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
	protected Integer getSourceIndex(Matcher matcher) {

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
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher}
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
	 * @throws IndexOutOfBoundsException	If there is no fourth capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NumberFormatException		if the fifth capturing group does not contain any parsable integer.
	 */
	protected Integer getComputeIndex(Matcher matcher) {
	
		return Integer.parseInt(matcher.group(5));
	}

	/**
	 * @param connector				The {@link Connector} whose {@link HardwareMonitor} is being search for.
	 * @param monitorName			The name of the {@link HardwareMonitor} being searched for.
	 * @param createMonitorIfNull	Indicates whether a new {@link HardwareMonitor} should be created if none was found.
	 *
	 * @return						The {@link HardwareMonitor} in the given {@link Connector}
	 *								whose name matches the given monitor name, if available.
	 */
	private HardwareMonitor getHardwareMonitor(Connector connector, String monitorName, boolean createMonitorIfNull) {
	
		notNull(connector, "Connector cannot be null.");

		return connector
			.getHardwareMonitors()
			.stream()
			.filter(hardwareMonitor -> hardwareMonitor
				.getType()
				.getName()
				.equalsIgnoreCase(monitorName))
			.findFirst()
			.orElseGet(() -> createMonitorIfNull ? createHardwareMonitor(monitorName, connector) : null);
	}
	/**
	 * @param monitorName	The name of the monitor type.
	 * @param connector		The {@link Connector} to wich the newly created {@link HardwareMonitor} should be added.
	 *
	 * @return				The newly created {@link HardwareMonitor} instance.
	 */
	private HardwareMonitor createHardwareMonitor(final String monitorName, final Connector connector) {

		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.discovery(Discovery.builder().build())
			.collect(Collect.builder().build())
			.type(MonitorType.getByName(monitorName))
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
		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, monitorName, true);

		return ConnectorParserConstants.DISCOVERY.equalsIgnoreCase(getMonitorJobName(matcher))
			? hardwareMonitor.getDiscovery()
			: hardwareMonitor.getCollect();
	}

	/**
	 * @param hardwareMonitor	The {@link HardwareMonitor} whose job name is being searched for.
	 * @param monitorJobName	The name of the job (i.e. <i>discovery</i> or <i>collect</i>)
	 *                          in the given {@link HardwareMonitor}
	 *                          whose {@link Source} is being searched for.
	 * @param sourceIndex		The index of the {@link Source} in the given job.
	 *
	 * @return					The {@link Source} with the given index in he given {@link HardwareMonitor} job,
	 * 							if available.
	 */
	@SuppressWarnings("unchecked")
	private <T extends Source> T getSource(HardwareMonitor hardwareMonitor, String monitorJobName, int sourceIndex) {
	
		if (hardwareMonitor == null) {
			return null;
		}
	
		MonitorJob monitorJob = ConnectorParserConstants.DISCOVERY.equalsIgnoreCase(monitorJobName)
			? hardwareMonitor.getDiscovery()
			: hardwareMonitor.getCollect();
	
		if (monitorJob == null) {
			return null;
		}
	
		List<Source> sources = monitorJob.getSources();
		if (sources == null) {
			return null;
		}
	
		return (T) sources
			.stream()
			.filter(source -> getType().isInstance(source) && source.getIndex() == sourceIndex)
			.findFirst()
			.orElse(null);
	}

	/**
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher},
	 * 					whose match operation has been called successfully,
	 * 					and from which
	 * 					a {@link HardwareMonitor} name, a job name
	 * 					and a {@link Source} index can be extracted.
	 * @param connector	The {@link Connector} whose source is being searched for.
	 *
	 * @return			The {@link Source} in the given {@link Connector}
	 * 					matching the given {@link Matcher}, if available.
	 */
	protected <T extends Source> T getSource(Matcher matcher, Connector connector) {
	
		String monitorName = getMonitorName(matcher);
		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, monitorName, false);
	
		return getSource(hardwareMonitor, getMonitorJobName(matcher), getSourceIndex(matcher));
	}

	/**
	 * @param key		The key to parse.
	 * @param connector	The {@link Connector} whose source is being searched for.
	 * @param <T>		The type of {@link Source} being searched for.
	 *
	 * @return			The {@link Source} matching the given key.
	 */
	protected <T extends Source> T getSource(String key, Connector connector) {

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> "Invalid key: " + key + ConnectorParserConstants.DOT);

		T source = getSource(matcher, connector);
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
	
		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, getMonitorName(matcher), false);
	
		Source source = getSource(hardwareMonitor, getMonitorJobName(matcher), getSourceIndex(matcher));
	
		return getCompute(source, getComputeIndex(matcher));
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

		if (this instanceof TypeProcessor) {

			return getTypeValue().equalsIgnoreCase(value);
		}

		return getSource(matcher, connector) != null;
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
	
		if (this instanceof TypeProcessor) {
	
			return getTypeValue().equalsIgnoreCase(value);
		}
	
		return getCompute(matcher, connector) != null;
	}

	/**
	 * @param key       The key will determine which context evaluation method should be called:<br>
	 *                  <ul>
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

		return key.contains(DOT + COMPUTE)
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
	public void parse(final String key, final String value, final Connector connector) {

		notNull(key, "key cannot be null.");
		notNull(value, "value cannot be null.");
		notNull(connector, "Connector cannot be null.");
	}
}
