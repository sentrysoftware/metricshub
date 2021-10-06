package com.sentrysoftware.matrix.model.alert;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class AlertRule {
	@JsonIgnore
	private BiFunction<Monitor, Set<AlertCondition>, AlertDetails> conditionsChecker;
	private long period;
	private Set<AlertCondition> conditions;
	private Severity severity;
	private Long firstTriggerTimestamp;
	private AlertDetails details;
	private AlertRuleState active = AlertRuleState.INACTIVE;
	private AlertRuleType type;

	public AlertRule(@NonNull BiFunction<Monitor, Set<AlertCondition>, AlertDetails> conditionsChecker, @NonNull Set<AlertCondition> conditions,
			long period, @NonNull Severity severity, @NonNull AlertRuleType type) {
		this.conditionsChecker = conditionsChecker;
		this.conditions = conditions;
		this.period = period;
		this.severity = severity;
		this.type = type;
	}

	public AlertRule(@NonNull BiFunction<Monitor, Set<AlertCondition>, AlertDetails> conditionsChecker, @NonNull Set<AlertCondition> conditions,
			@NonNull Severity severity, @NonNull AlertRuleType type) {
		this(conditionsChecker, conditions, 0, severity, type);
	}

	public AlertRule(@NonNull BiFunction<Monitor, Set<AlertCondition>, AlertDetails> conditionsChecker, @NonNull Set<AlertCondition> conditions,
			long period, @NonNull Severity severity) {
		this(conditionsChecker, conditions, period, severity, AlertRuleType.STATIC);
	}

	public AlertRule(@NonNull BiFunction<Monitor, Set<AlertCondition>, AlertDetails> conditionsChecker, @NonNull Set<AlertCondition> conditions,
			@NonNull Severity severity) {
		this(conditionsChecker, conditions, 0, severity, AlertRuleType.STATIC);
	}

	public enum AlertRuleState {
		INACTIVE, PENDING, ACTIVE
	}

	public enum AlertRuleType {
		STATIC, INSTANCE, USER
	}

	/**
	 * Evaluate the current {@link AlertRule}
	 * 
	 * @param monitor The monitor on wish we apply the condition
	 */
	public void evaluate(Monitor monitor) {
		if (conditions != null) {
			details = conditionsChecker.apply(monitor, conditions);
			refresh();
		}
	}

	/**
	 * Get the alert rule state. This method refreshes the current AlertRule before returning its state
	 * 
	 * @return {@link AlertRuleState}: INACTIVE, PENDING or ACTIVE
	 */
	public AlertRuleState getActive() {
		refresh();
		return active;
	}

	/**
	 * Check if the current AlertRule is active
	 * 
	 * @return <code>true</code> or <code>false</code>
	 */
	public boolean isActive() {
		refresh();
		return AlertRuleState.ACTIVE.equals(active);
	}

	/**
	 * Refresh the current {@link AlertRule}.
	 */
	private void refresh() {
		// We have a details ? means the condition returned true (unfortunately)
		if (details != null) {
			long currentTimeMillis = System.currentTimeMillis();
			firstTriggerTimestamp = firstTriggerTimestamp == null ? currentTimeMillis : firstTriggerTimestamp;
			// If we reach the time limit defined by the period then the AlertRule becomes ACTIVE otherwise it is PENDING
			if (currentTimeMillis - firstTriggerTimestamp >= period) {
				active = AlertRuleState.ACTIVE;
			} else {
				active = AlertRuleState.PENDING;
			}
		} else {
			firstTriggerTimestamp = null;
		}
	}

	/**
	 * Copy the current alert rule and build a new one
	 * 
	 * @return a new alert rule
	 */
	public AlertRule copy() {

		return new AlertRule(conditionsChecker, conditions.stream()
					.filter(Objects::nonNull)
					.map(AlertCondition::copy)
					.collect(Collectors.toCollection(HashSet::new)),
				period,
				severity,
				type);

	}

	/**
	 * Check if the given object is the same as the current AlertRule instance. <br>
	 * Must match the three fields <em>conditions</em>, <em>period</em> and <em>severity</em>
	 * 
	 * @param obj The object we wish to compare
	 * @return true or false
	 */
	public boolean same(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlertRule other = (AlertRule) obj;
		if (conditions == null) {
			if (other.conditions != null)
				return false;
		} else if (!conditions.equals(other.conditions))
			return false;
		if (period != other.period)
			return false;
		return severity == other.severity;
	}

}
