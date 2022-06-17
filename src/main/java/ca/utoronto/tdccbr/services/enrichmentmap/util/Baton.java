package ca.utoronto.tdccbr.services.enrichmentmap.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Baton<T> {

	private T t;

	public Supplier<T> supplier() {
		return new Supplier<T>() {
			@Override
			public T get() {
				return t;
			}
		};
	}

	public Consumer<T> consumer() {
		return new Consumer<T>() {
			@Override
			public void accept(T t) {
				Baton.this.t = t;
			}
		};
	}
}
