package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import java.util.Set;
import java.util.function.Predicate;

import com.sentrysoftware.matrix.common.meta.parameter.state.IState;
import com.sentrysoftware.matrix.common.meta.parameter.state.IntrusionStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.Present;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MappingConstants {

	// Predicates
	public static final Predicate<IState> OK_STATUS_PREDICATE = state -> Status.OK == state;
	public static final Predicate<IState> DEGRADED_STATUS_PREDICATE = state -> Status.DEGRADED == state;
	public static final Predicate<IState> FAILED_STATUS_PREDICATE = state -> Status.FAILED == state;
	public static final Predicate<IState> PRESENT_PREDICATE = state -> Present.PRESENT == state;
	public static final Predicate<IState> INTRUSION_STATUS_PREDICATE = state -> IntrusionStatus.OPEN == state;

	// Attribute keys and values
	public static final String VM_HOST_NAME = "vm.host.name";
	public static final String STATE_ATTRIBUTE_KEY = "state";
	public static final String OK_ATTRIBUTE_VALUE = "ok";
	public static final String DEGRADED_ATTRIBUTE_VALUE = "degraded";
	public static final String FAILED_ATTRIBUTE_VALUE = "failed";
	public static final String PROTOCOL_ATTRIBUTE_KEY = "protocol";
	public static final String SNMP_ATTRIBUTE_VALUE = "snmp";
	public static final String WMI_ATTRIBUTE_VALUE = "wmi";
	public static final String WBEM_ATTRIBUTE_VALUE = "wbem";
	public static final String SSH_ATTRIBUTE_VALUE = "ssh";
	public static final String PRESENT_ATTRIBUTE_VALUE = "present";
	public static final String INTRUSION_ATTRIBUTE_VALUE = "open";
	

	// Default attribute keys
	public static final String NAME = "name";
	public static final String PARENT = "parent";
	public static final String ID = "id";

	// Units
	public static final String CELCIUS_UNIT = "Cel";
	public static final String JOULES_UNIT = "J";
	public static final String WATTS_UNIT = "W";

	public static final Set<String> DEFAULT_ATTRIBUTE_NAMES = Set.of(ID, NAME, PARENT);
}
