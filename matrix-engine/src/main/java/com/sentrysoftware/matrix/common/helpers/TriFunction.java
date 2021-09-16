package com.sentrysoftware.matrix.common.helpers;

import java.util.function.Function;

import lombok.NonNull;

@FunctionalInterface
public interface TriFunction<A, B, C, R> {

	R apply(A a, B b, C c);

	default <V> TriFunction<A, B, C, V> andThen(@NonNull Function<? super R, ? extends V> after) {
		return (A a, B b, C c) -> after.apply(apply(a, b, c));
	}
}