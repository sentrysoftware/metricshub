package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DOMAIN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOSTNAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PERCENT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SHARE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SHARE_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_STATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_STATE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.DiscreteParamType;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.SimpleParamType;
import com.sentrysoftware.matrix.common.meta.parameter.state.PowerState;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertRule;

public class Vm implements IMetaMonitor {

	private static final List<String> METADATA = List.of(DEVICE_ID, DOMAIN, HOSTNAME, IDENTIFYING_INFORMATION);

	public static final MetaParameter POWER_STATE = MetaParameter.builder()
			.basicCollect(true)
			.name(POWER_STATE_PARAMETER)
			.unit(POWER_STATE_PARAMETER_UNIT)
			.type(new DiscreteParamType(PowerState::interpret))
			.build();

	public static final MetaParameter POWER_SHARE = MetaParameter.builder()
			.basicCollect(true)
			.name(POWER_SHARE_PARAMETER)
			.unit(EMPTY)
			.type(SimpleParamType.NUMBER)
			.build();

	public static final MetaParameter POWER_SHARE_PERCENT = MetaParameter.builder()
			.basicCollect(false)
			.name(POWER_SHARE_PERCENT_PARAMETER)
			.unit(PERCENT_PARAMETER_UNIT)
			.type(SimpleParamType.NUMBER)
			.build();

			
	private static final Map<String, MetaParameter> META_PARAMETERS;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(PRESENT_PARAMETER, PRESENT);
		map.put(POWER_STATE_PARAMETER, POWER_STATE);
		map.put(POWER_SHARE_PARAMETER, POWER_SHARE);
		map.put(ENERGY_PARAMETER, ENERGY);
		map.put(ENERGY_USAGE_PARAMETER, ENERGY_USAGE);
		map.put(POWER_CONSUMPTION_PARAMETER, POWER_CONSUMPTION);
		map.put(POWER_SHARE_PERCENT_PARAMETER, POWER_SHARE_PERCENT);

		META_PARAMETERS = Collections.unmodifiableMap(map);

	}

	@Override
	public void accept(IMonitorVisitor monitorVisitor) {
		monitorVisitor.visit(this);
	}

	@Override
	public Map<String, MetaParameter> getMetaParameters() {
		return META_PARAMETERS;
	}

	@Override
	public MonitorType getMonitorType() {
		return MonitorType.VM;
	}

	@Override
	public List<String> getMetadata() {
		return METADATA;
	}

	@Override
	public Map<String, List<AlertRule>> getStaticAlertRules() {
		return Collections.emptyMap();
	}
}