package com.sentrysoftware.matrix.engine.target;

import com.sentrysoftware.matrix.connector.model.common.OSType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TargetType {

	HP_OPEN_VMS(OSType.VMS),
	HP_TRU64_UNIX(OSType.OSF1),
	HP_UX(OSType.HP),
	IBM_AIX(OSType.RS6000),
	LINUX(OSType.LINUX),
	MGMT_CARD_BLADE_ESXI(OSType.OOB),
	MS_WINDOWS(OSType.NT),
	NETWORK_SWITCH(OSType.NETWORK),
	STORAGE(OSType.STORAGE),
	SUN_SOLARIS(OSType.SOLARIS);

	@Getter
	private OSType osType;

}
