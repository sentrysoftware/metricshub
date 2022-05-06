package com.sentrysoftware.matrix.common.meta.parameter;

import java.util.Optional;
import java.util.function.Function;

import com.sentrysoftware.matrix.common.meta.parameter.state.IState;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DiscreteParamType implements IParameterType {

	@Getter
	private Function<String, Optional<? extends IState>> interpreter;

}
