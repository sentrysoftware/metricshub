package com.sentrysoftware.matrix.model.monitor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.TreeMap;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TARGET_FQDN;

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
	 * @param value the metadata value we wish to add
	 */
	public void addMetadata(final String key, final String value) {
		Assert.notNull(key, "key cannot be null");

		metadata.put(key, value);
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

		final PresentParam presentParam = getParameter(HardwareConstants.PRESENT_PARAMETER, PresentParam.class);

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
}
