package com.sentrysoftware.matrix.common.helpers;

import lombok.NonNull;

@FunctionalInterface
public interface TriConsumer<A, B, C> {

	void accept(A a, B b, C c);

	default TriConsumer<A, B, C> andThen(@NonNull TriConsumer<? super A, ? super B, ? super C> after) {
		return (a, b, c) ->
			{
				accept(a, b, c);
				after.accept(a, b, c);
			};
	}
}