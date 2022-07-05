package com.sentrysoftware.matrix.model.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOST_FQDN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sentrysoftware.matrix.common.meta.parameter.state.IState;
import com.sentrysoftware.matrix.common.meta.parameter.state.Present;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Monitor {

	private String id;
	private String name;
	private MonitorType monitorType;
	private String parentId;
	private String hostId;
	private String extendedType;
	private Long discoveryTime;

	// parameter name to Parameter value
	@Default
	private Map<String, IParameter> parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Default
	private Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Default
	private Map<String, List<AlertRule>> alertRules = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * Collect the given parameter to the internal map of parameters
	 * 
	 * @param parameter The parameter we wish to collect
	 */
	public void collectParameter(IParameter parameter) {
		addParameter(parameter);

		// Evaluate the alert rules
		this.alertRules
			.getOrDefault(parameter.getName(), Collections.emptyList())
			.forEach(rule -> rule.evaluate(this));
	}

	/**
	 * Add the given parameter to the internal map of parameters
	 * 
	 * @param parameter The parameter we wish to add
	 */
	public void addParameter(IParameter parameter) {
		parameters.put(parameter.getName(), parameter);
	}

	/**
	 * Add the given alert rules to the internal map of alert rules
	 * 
	 * @param parameterName  The parameter on which we want to set the alert rules
	 * @param alertRules     The alert rules we wish to add
	 */
	public void addAlertRules(@NonNull final String parameterName, @NonNull List<AlertRule> alertRules) {

		if (this.alertRules.containsKey(parameterName)) {
			alertRules = mergeAlertRules(this.alertRules.get(parameterName), alertRules);
		}

		this.alertRules.put(parameterName, new ArrayList<>(alertRules));
	}

	/**
	 * Merge the given two list of alert rules, keep not updated alert rules and add the new ones
	 * 
	 * @param existingAlertRules The existing alert rules
	 * @param newAlertRules      The new alert rules we wish to merge
	 * @return new {@link List} of {@link AlertRule} instances
	 */
	private static List<AlertRule> mergeAlertRules(final List<AlertRule> existingAlertRules, final List<AlertRule> newAlertRules) {
		if (existingAlertRules == null || existingAlertRules.isEmpty()) {
			return newAlertRules;
		}

		final List<AlertRule> existingAlertRulesToKeep = new ArrayList<>();

		// Filter alert rules and remove existing alert rules if they are the same
		// If the alert rule exists and it is the same, it means we aren't in the first discovery
		// so we must keep the same alert rule as it may be active and contains lot of information about the problem
		final List<AlertRule> alertRulesToAdd = newAlertRules.stream()
				.filter(alertRule -> existingAlertRules
						.stream().noneMatch(existingAlertRule -> {
							boolean same = alertRule.same(existingAlertRule);
							// If we are adding the same rule then we keep the existing one
							if (same) {
								existingAlertRulesToKeep.add(existingAlertRule);
							}
							return same;
						}))
				.collect(Collectors.toList());

		alertRulesToAdd.addAll(existingAlertRulesToKeep);

		return alertRulesToAdd;
	}

	/**
	 * Add the given metadata key-value to the internal map of metadata
	 * 
	 * @param key   The metadata key, example: serialNumber
	 * @param value The metadata value we wish to add
	 */
	public void addMetadata(@NonNull final String key, final String value) {
		metadata.put(key, value);
	}

	/**
	 * Get the metadata value identified by the given key
	 * 
	 * @param key The metadata key, example: serialNumber
	 * @return String value
	 */
	public String getMetadata(final String key) {
		return metadata.get(key);
	}

	/**
	 * Get a parameter by type
	 * 
	 * @param parameterName The unique name of the parameter
	 * @param type          The type of the parameter
	 * @return {@link IParameter} instance
	 */
	public <T extends IParameter> T getParameter(final String parameterName, final Class<T> type) {
		return type.cast(parameters.get(parameterName));
	}

	/**
	 * Set the monitor as missing
	 */
	public void setAsMissing() {

		if (!monitorType.getMetaMonitor().hasPresentParameter()) {
			return;
		}

		final DiscreteParam presentParam = getParameter(PRESENT_PARAMETER,
				DiscreteParam.class);

		if (presentParam != null) {
			presentParam.setState(Present.MISSING);
			collectParameter(presentParam);
		} else {
			collectParameter(DiscreteParam.missing());
		}

	}

	/**
	 * Set the monitor as present
	 */
	public void setAsPresent() {

		if (monitorType.getMetaMonitor().hasPresentParameter()) {
			collectParameter(DiscreteParam.present());
		}
	}

	@JsonIgnore
	public String getFqdn() {

		String fqdn = metadata.get(FQDN);

		return fqdn != null ? fqdn : metadata.get(HOST_FQDN);
	}

	/**
	 * Check whether the monitor is missing or not
	 * 
	 * @param conditions The conditions used to check abnormality
	 * @return {@link AssertedParameter} instance, never null but internal parameter can be null
	 */
	public AssertedParameter<DiscreteParam> assertPresentParameter(final Set<AlertCondition> conditions) {
		if (!monitorType.getMetaMonitor().hasPresentParameter()) {
			return AssertedParameter.<DiscreteParam>builder().abnormal(false).build();
		}

		final DiscreteParam presentParam = getParameter(PRESENT_PARAMETER, DiscreteParam.class);
		final IState present = presentParam != null ? presentParam.getState() : null;
		final Double presentValue = present != null ? (double) present.getNumericValue() : null;

		return AssertedParameter
				.<DiscreteParam>builder()
				.parameter(presentParam)
				.abnormal(isAbnormal(conditions, presentValue))
				.build();

	}

	/**
	 * Check if the current monitor is missing or not. Missing means the present value is 0.
	 * If the monitor is not eligible to missing devices then it can never be missing.
	 * 
	 * @return <code>true</code> if the monitor is missing otherwise <code>false</code>
	 */
	@JsonIgnore
	public boolean isMissing() {
		if (!monitorType.getMetaMonitor().hasPresentParameter()) {
			return false;
		}

		final DiscreteParam presentParam = getParameter(PRESENT_PARAMETER, DiscreteParam.class);
		final IState present = presentParam != null ? presentParam.getState() : null;

		return Present.MISSING.equals(present);
	}

	/**
	 * Assert the given status parameter identified by its name.
	 * 
	 * @param parameterName The parameter name we wish to assert
	 * @param conditions    The conditions used to check abnormality
	 * @return {@link AssertedParameter} instance, never null but internal parameter can be null
	 */
	public AssertedParameter<DiscreteParam> assertStatusParameter(final String parameterName, final Set<AlertCondition> conditions) {
		final DiscreteParam status = getParameter(parameterName, DiscreteParam.class);
		final IState state = status != null ? status.getState() : null;
		final Double statusValue = state != null ? (double) state.getNumericValue() : null;

		return AssertedParameter
				.<DiscreteParam>builder()
				.parameter(status)
				.abnormal(isAbnormal(conditions, statusValue))
				.build();
	}

	/**
	 * Check if the given value is abnormal by applying all the set of {@link AlertCondition}
	 * 
	 * @param conditions The conditions used to check abnormality
	 * @param value      The value to check
	 * @return boolean value
	 */
	private static boolean isAbnormal(final Set<AlertCondition> conditions, final Double value) {
		return value != null && conditions != null && !conditions.isEmpty() 
				&& conditions.stream()
					.allMatch(condition -> condition
						.getOperator()
						.getFunction()
						.apply(value, condition.getThreshold())
					);
	}

	/**
	 * Assert the given number parameter identified by its name.
	 * 
	 * @param parameterName The parameter name we wish to assert
	 * @param conditions    The conditions used to check abnormality
	 * @return {@link AssertedParameter} instance, never null
	 */
	public AssertedParameter<NumberParam> assertNumberParameter(final String parameterName, final Set<AlertCondition> conditions) {
		final NumberParam parameter = getParameter(parameterName, NumberParam.class);
		final Double value = parameter != null ? parameter.getValue() : null;
		return AssertedParameter
				.<NumberParam>builder()
				.parameter(parameter)
				.abnormal(isAbnormal(conditions, value))
				.build();
	}

	@Data
	@Builder
	public static class AssertedParameter<T extends IParameter> {
		private T parameter;
		private boolean abnormal;
	}

	/**
	 * Check if the given parameter is correctly updated.
	 * 
	 * @param parameterName name of the parameter we wish to check.
	 * @return boolean value
	 */
	public boolean isParameterUpdated(final String parameterName) {
		final IParameter parameter = getParameters().get(parameterName);
		return parameter != null && parameter.isUpdated();
	}

}
