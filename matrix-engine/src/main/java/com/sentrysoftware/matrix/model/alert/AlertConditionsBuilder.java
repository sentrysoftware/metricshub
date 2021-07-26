package com.sentrysoftware.matrix.model.alert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;

public class AlertConditionsBuilder {

	public static final List<AlertCondition> ERROR_COUNT_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().gte(1D).build());

	public static final List<AlertCondition> STATUS_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().eq(2D).build());

	public static final List<AlertCondition> PRESENT_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().eq(0D).build());

	public static final List<AlertCondition> STATUS_WARN_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().eq(1D).build());

	public static final List<AlertCondition> CHARGE_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().lte(30D).build());

	public static final List<AlertCondition> CHARGE_WARN_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().gt(30D).and().lte(50D).build());

	public static final List<AlertCondition> CORRECTED_ERROR_COUNT_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().gte(1D).build());

	public static final List<AlertCondition> SPEED_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().eq(0D).build());

	public static final List<AlertCondition> AVAILABLE_PATH_COUNT_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().lt(1D).build());

	public static final List<AlertCondition> BANDWIDTH_UTILIZATION_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().gt(50D).build());

	public static final List<AlertCondition> ERROR_PERCENT_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().gte(30D).build());

	public static final List<AlertCondition> ERROR_PERCENT_WARN_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().gte(10D).and().lt(30D).build());

	public static final List<AlertCondition> ENDURANCE_REMAINING_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().lte(2D).build());

	public static final List<AlertCondition> ENDURANCE_REMAINING_WARN_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().lte(5D).and().gt(2D).build());

	public static final List<AlertCondition> USED_CAPACITY_ALARM_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().gte(99D).build());

	public static final List<AlertCondition> USED_CAPACITY_WARN_CONDITION = Collections
			.unmodifiableList(AlertConditionsBuilder.newInstance().gte(90D).and().lt(99D).build());

	private List<AlertCondition> alertConditions = new ArrayList<>();

	public static AlertConditionsBuilder newInstance() {
		return new AlertConditionsBuilder();
	}

	public AlertConditionsBuilder eq(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.EQ).threshold(threshold).build());
		return this;
	}

	public AlertConditionsBuilder ne(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.NE).threshold(threshold).build());
		return this;
	}

	public AlertConditionsBuilder gt(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.GT).threshold(threshold).build());
		return this;
	}

	public AlertConditionsBuilder gte(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.GTE).threshold(threshold).build());
		return this;
	}

	public AlertConditionsBuilder lt(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.LT).threshold(threshold).build());
		return this;
	}

	public AlertConditionsBuilder lte(@NonNull final Double threshold) {
		alertConditions.add(AlertCondition.builder().operator(AlertOperator.LTE).threshold(threshold).build());
		return this;
	}

	public AlertConditionsBuilder and() {
		return this;
	}

	public List<AlertCondition> build() {
		return alertConditions;
	}
}
