package com.sentrysoftware.matrix.common.helpers;

import java.util.function.Function;

import org.springframework.util.Assert;

@FunctionalInterface
public interface TriFunction<A, B, C, R> {

	R apply(A a, B b, C c);

	default <V> TriFunction<A, B, C, V> andThen(Function<? super R, ? extends V> after) {
		Assert.notNull(after, "after Function cannot be null.");

		return (A a, B b, C c) -> after.apply(apply(a, b, c));
	}
}