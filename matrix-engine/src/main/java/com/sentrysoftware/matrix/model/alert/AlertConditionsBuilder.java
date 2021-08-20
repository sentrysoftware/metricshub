package com.sentrysoftware.matrix.model.alert;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.matrix.common.helpers.NumberHelper;

import lombok.NonNull;

public class AlertConditionsBuilder {

	public static final Set<AlertCondition> ERROR_COUNT_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().gte(1D).build());

	public static final Set<AlertCondition> STATUS_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().eq(2D).build());

	public static final Set<AlertCondition> PRESENT_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().eq(0D).build());

	public static final Set<AlertCondition> STATUS_WARN_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().eq(1D).build());

	public static final Set<AlertCondition> CHARGE_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().lte(30D).build());

	public static final Set<AlertCondition> CHARGE_WARN_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().lte(50D).build());

	public static final Set<AlertCondition> CORRECTED_ERROR_COUNT_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().gte(1D).build());

	public static final Set<AlertCondition> SPEED_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().eq(0D).build());

	public static final Set<AlertCondition> SPEED_WARN_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().lte(500D).build());

	public static final Set<AlertCondition> SPEED_PERCENT_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().eq(0D).build());

	public static final Set<AlertCondition> SPEED_PERCENT_WARN_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().lte(5D).build());

	public static final Set<AlertCondition> AVAILABLE_PATH_COUNT_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().lt(1D).build());

	public static final Set<AlertCondition> BANDWIDTH_UTILIZATION_WARN_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().gte(80D).build());

	public static final Set<AlertCondition> ERROR_PERCENT_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().gte(30D).build());

	public static final Set<AlertCondition> ERROR_PERCENT_WARN_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().gte(10D).build());

	public static final Set<AlertCondition> ENDURANCE_REMAINING_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().lte(2D).build());

	public static final Set<AlertCondition> ENDURANCE_REMAINING_WARN_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().lte(5D).build());

	public static final Set<AlertCondition> USED_CAPACITY_ALARM_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().gte(99D).build());

	public static final Set<AlertCondition> USED_CAPACITY_WARN_CONDITION = Collections
			.unmodifiableSet(AlertConditionsBuilder.newInstance().gte(90D).build());

	private Set<AlertCondition> alertConditions = new HashSet<>();

	public static AlertConditionsBuilder newInstance() {
		return new AlertConditionsBuilder();
	}

	/**
	 * Add a new equals alert condition
	 * 
	 * @param threshold The threshold we wish to add in the alert condition
	 * @return the current builder
	 */
	public AlertConditionsBuilder eq(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.EQ).threshold(NumberHelper.round(threshold, 1, RoundingMode.HALF_UP)).build());
		return this;
	}

	/**
	 * Add a new not-equals alert condition
	 * 
	 * @param threshold The threshold we wish to add in the alert condition
	 * @return the current builder
	 */
	public AlertConditionsBuilder ne(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.NE).threshold(NumberHelper.round(threshold, 1, RoundingMode.HALF_UP)).build());
		return this;
	}

	/**
	 * Add a new greater-than alert condition
	 * 
	 * @param threshold The threshold we wish to add in the alert condition
	 * @return the current builder
	 */
	public AlertConditionsBuilder gt(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.GT).threshold(NumberHelper.round(threshold, 1, RoundingMode.HALF_UP)).build());
		return this;
	}

	/**
	 * Add a new greater-than-or-equals alert condition
	 * 
	 * @param threshold The threshold we wish to add in the alert condition
	 * @return the current builder
	 */
	public AlertConditionsBuilder gte(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.GTE).threshold(NumberHelper.round(threshold, 1, RoundingMode.HALF_UP)).build());
		return this;
	}

	/**
	 * Add a new less-than alert condition
	 * 
	 * @param threshold The threshold we wish to add in the alert condition
	 * @return the current builder
	 */
	public AlertConditionsBuilder lt(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.LT).threshold(NumberHelper.round(threshold, 1, RoundingMode.HALF_UP)).build());
		return this;
	}

	/**
	 * Add a new less-than-or-equals alert condition
	 * 
	 * @param threshold The threshold we wish to add in the alert condition
	 * @return the current builder
	 */
	public AlertConditionsBuilder lte(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.LTE).threshold(NumberHelper.round(threshold, 1, RoundingMode.HALF_UP)).build());
		return this;
	}

	/**
	 * Build the final result
	 * 
	 * @return Set of {@link AlertCondition} instances
	 */
	public Set<AlertCondition> build() {
		return alertConditions;
	}
}
