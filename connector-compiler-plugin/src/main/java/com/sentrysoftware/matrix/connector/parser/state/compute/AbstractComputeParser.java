package com.sentrysoftware.matrix.connector.parser.state.compute;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.notNull;

public abstract class AbstractComputeParser implements IConnectorStateParser {

	protected abstract Class<? extends IConnectorStateParser> getTypeProcessor();
	protected abstract Class<? extends Compute> getComputeType();
	protected abstract String getTypeValue();
	public abstract Matcher getMatcher(String key);

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
	 * 						  <i>^\\s*<b><u>(.*)</u></b>\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i><b><u>enclosure</u></b>.discovery.source(1).compute(1).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>enclosure</u></b></i>.
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
	private String getMonitorName(Matcher matcher) {

		return matcher.group(1);
	}

	/**
	 * Extracts the name of the {@link HardwareMonitor}'s job
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The name of the job is expected to be the second capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*(.*)\\.<b><u>(discovery|collect)</u></b>\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>enclosure.<b><u>discovery</u></b>.source(1).compute(1).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>discovery</u></b></i>.
	 *
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The name of the {@link HardwareMonitor}'s job,
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
	private String getMonitorJobName(Matcher matcher) {
	
		return matcher.group(2);
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
	 * 						  <i>^\\s*(.*)\\.<b><u>(discovery|collect)\\.source\\(<b><u>([1-9]\\d*)</u></b>\\)\\.compute\\(([1-9]\\d*)\\)\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>enclosure.discovery.source(<b><u>1</u></b>).compute(2).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>1</u></b></i>.
	 *
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The index of the {@link Source},
	 * 					which is supposed to be the third capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NullPointerException			If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no third capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws NumberFormatException		if the third capturing group does not contain a parsable integer.
	 */
	private Integer getSourceIndex(Matcher matcher) {
	
		return Integer.parseInt(matcher.group(3));
	}

	/**
	 * Extracts the index of the {@link Compute}
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The index of the {@link Compute} is expected to be the fourth capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*(.*)\\.<b><u>(discovery|collect)\\.source\\(<b><u>([1-9]\\d*)</u></b>\\)\\.compute\\(([1-9]\\d*)\\)\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>enclosure.discovery.source(<b><u>1</u></b>).compute(2).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>1</u></b></i>.
	 *
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The index of the {@link Compute},
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
	 * @throws NumberFormatException		if the fourth capturing group does not contain a parsable integer.
	 */
	public Integer getComputeIndex(Matcher matcher) {
	
		return Integer.parseInt(matcher.group(4));
	}

	/**
	 * @param connector		The {@link Connector} whose {@link HardwareMonitor} is being search for.
	 * @param monitorName	The name of the {@link HardwareMonitor} being searched for.
	 *
	 * @return				The {@link HardwareMonitor} in the given {@link Connector}
	 * 						whose name matches the given monitor name, if available.
	 */
	public HardwareMonitor getHardwareMonitor(Connector connector, String monitorName) {
	
		notNull(connector, "Connector cannot be null.");
	
		return connector
			.getHardwareMonitors()
			.stream()
			.filter(hardwareMonitor -> hardwareMonitor
				.getType()
				.getName()
				.equalsIgnoreCase(monitorName))
			.findFirst()
			.orElse(null);
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
	public Source getSource(HardwareMonitor hardwareMonitor, String monitorJobName, int sourceIndex) {
	
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
	
		return sources
			.stream()
			.filter(source -> source.getIndex() == sourceIndex)
			.findFirst()
			.orElse(null);
	}

	/**
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher},
	 *                  whose match operation has been called successfully,
	 *                  and from which
	 *                  a {@link HardwareMonitor} name, a job name and a {@link Source} index can be extracted.
	 * @param connector	The {@link Connector} whose source is being searched for.
	 *
	 * @return			The {@link Source} in the given {@link Connector} matching the given {@link Matcher},
	 * 					if available.
	 */
	public Source getSource(Matcher matcher, Connector connector) {
	
		String monitorName = getMonitorName(matcher);
		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, monitorName);
	
		return getSource(hardwareMonitor, getMonitorJobName(matcher), getSourceIndex(matcher));
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {
	
		notNull(key, "key cannot be null.");
		notNull(value, "value cannot be null.");
		notNull(connector, "Connector cannot be null.");
	}

	/**
	 * @param <T>			A sub-type of the {@link Compute} class.
	 * @param computes		A list of {@link Compute}s.
	 * @param computeIndex	The index of the {@link Compute} being searched for.
	 *
	 * @return				The {@link Compute} in the given list having the given index.
	 */
	@SuppressWarnings("unchecked")
	private <T> T getCompute(List<Compute> computes, int computeIndex) {
	
		if (computes == null) {
			return null;
		}
	
		return (T) computes
			.stream()
			.filter(compute -> getComputeType().isInstance(compute) && compute.getIndex() == computeIndex)
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
	public <T> T getCompute(Source source, int computeIndex) {
	
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
	public <T> T getCompute(Matcher matcher, Connector connector) {
	
		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, getMonitorName(matcher));
	
		Source source = getSource(hardwareMonitor, getMonitorJobName(matcher), getSourceIndex(matcher));
	
		return getCompute(source, getComputeIndex(matcher));
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
	
		if (getTypeProcessor().isInstance(this)) {
	
			return getTypeValue().equalsIgnoreCase(value);
		}
	
		return getCompute(matcher, connector) != null;
	}

	@Override
	public boolean detect(final String key, final String value, final Connector connector) {
	
		Matcher matcher;
	
		return value != null
			&& key != null
			&& (matcher = getMatcher(key)).matches() //NOSONAR - Assigning matcher on purpose
			&& isComputeContext(value, matcher, connector);
	}
}
