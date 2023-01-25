package com.sentrysoftware.matrix.connector.model.identity.criterion;

import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.OsType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeviceType extends Criterion {

	private static final long serialVersionUID = 1L;

	private Set<OsType> keep = new HashSet<>();
	private Set<OsType> exclude = new HashSet<>();

	@Builder
	public DeviceType(String type, boolean forceSerialization, Set<OsType> keep, Set<OsType> exclude) {
		super(type, forceSerialization);
		this.keep = keep;
		this.exclude = exclude;
	}

}
