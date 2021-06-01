package com.sentrysoftware.matrix.common.helpers;

import org.springframework.util.Assert;

@FunctionalInterface
public interface TriConsumer<A, B, C> {

	void accept(A a, B b, C c);

	default TriConsumer<A, B, C> andThen(TriConsumer<? super A, ? super B, ? super C> after) {
		Assert.notNull(after, "after TriConsumer cannot be null.");
		return (a, b, c) ->
			{
				accept(a, b, c);
				after.accept(a, b, c);
			};
	}
}