package com.sentrysoftware.matrix.model.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TARGET_FQDN;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Monitor {

	private String id;
	private String name;
	private MonitorType monitorType;
	private String parentId;
	private String targetId;
	private String extendedType;

	// parameter name to Parameter value
	@Default
	private Map<String, IParameterValue> parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Default
	private Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Default
	private Map<String, List<AlertRule>> alertRules = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * Add the given parameter to the internal map of parameters
	 * 
	 * @param parameter The parameter we wish to add
	 */
	public void addParameter(IParameterValue parameter) {
		parameters.put(parameter.getName(), parameter);
	}

	/**
	 * Add the given metadata key-value to the internal map of metadata
	 * 
	 * @param key   The metadata key, example: serialNumber
	 * @param value The metadata value we wish to add
	 */
	public void addMetadata(final String key, final String value) {
		Assert.notNull(key, "key cannot be null");

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
	 * @return {@link IParameterValue} instance
	 */
	public <T extends IParameterValue> T getParameter(final String parameterName, final Class<T> type) {
		return type.cast(parameters.get(parameterName));
	}

	/**
	 * Set the monitor as missing
	 */
	public void setAsMissing() {

		if (!monitorType.getMetaMonitor().hasPresentParameter()) {
			return;
		}

		final PresentParam presentParam = getParameter(HardwareConstants.PRESENT_PARAMETER,
				PresentParam.class);

		if (presentParam != null) {
			presentParam.setPresent(0);
			presentParam.setState(ParameterState.ALARM);
		} else {
			addParameter(PresentParam.missing());
		}

	}

	/**
	 * Set the monitor as present
	 */
	public void setAsPresent() {

		if (monitorType.getMetaMonitor().hasPresentParameter()) {
			addParameter(PresentParam.present());
		}
	}

	@JsonIgnore
	public String getFqdn() {

		String fqdn = metadata.get(FQDN);

		return fqdn != null ? fqdn : metadata.get(TARGET_FQDN);
	}

	/**
	 * Check whether the monitor is missing or not
	 * 
	 * @param conditions The conditions used to check abnormality
	 * @return {@link AssertedParameter} instance, never null but internal parameter can be null
	 */
	public AssertedParameter<PresentParam> assertPresentParameter(final List<AlertCondition> conditions) {
		if (!monitorType.getMetaMonitor().hasPresentParameter()) {
			return AssertedParameter.<PresentParam>builder().abnormal(false).build();
		}

		final PresentParam presentParam = getParameter(HardwareConstants.PRESENT_PARAMETER, PresentParam.class);
		final Integer present = presentParam != null ? presentParam.getPresent() : null;
		final Double presentValue = present != null ? presentParam.getPresent().doubleValue() : null;

		return AssertedParameter.<PresentParam>builder()
				.parameter(presentParam)
				.abnormal(allConditionsMatched(conditions, presentValue))
				.build();

	}

	/**
	 * Assert the given status parameter identified by its name.
	 * 
	 * @param parameterName The parameter name we wish to assert
	 * @param conditions    The conditions used to check abnormality
	 * @return {@link AssertedParameter} instance, never null but internal parameter can be null
	 */
	public AssertedParameter<StatusParam> assertStatusParameter(final String parameterName, final List<AlertCondition> conditions) {
		final StatusParam status = getParameter(parameterName, StatusParam.class);
		final ParameterState state = status != null ? status.getState() : null;
		final Double statusValue = state != null ? (double) state.ordinal() : null;

		return AssertedParameter.<StatusParam>builder()
				.parameter(status)
				.abnormal(allConditionsMatched(conditions, statusValue))
				.build();
	}

	/**
	 * 
	 * @param conditions
	 * @param value
	 * @return
	 */
	private static boolean allConditionsMatched(final List<AlertCondition> conditions, final Double value) {
		return value != null && conditions != null && !conditions.isEmpty() 
				&& conditions.stream()
					.allMatch(condition -> condition
						.getOperator()
						.getFunction()
						.apply(value, condition.getThreshold()));
	}

	/**
	 * Assert the given number parameter identified by its name.
	 * 
	 * @param parameterName The parameter name we wish to assert
	 * @param conditions    The conditions used to check abnormality
	 * @return {@link AssertedParameter} instance, never null
	 */
	public AssertedParameter<NumberParam> assertNumberParameter(final String parameterName, final List<AlertCondition> conditions) {
		final NumberParam parameter = getParameter(parameterName, NumberParam.class);
		final Double value = parameter != null ? parameter.getValue() : null;
		return AssertedParameter.<NumberParam>builder()
				.parameter(parameter)
				.abnormal(allConditionsMatched(conditions, value))
				.build();
	}

	@Data
	@Builder
	public static class AssertedParameter<T extends IParameterValue> {
		private T parameter;
		private boolean abnormal;
	}
}
