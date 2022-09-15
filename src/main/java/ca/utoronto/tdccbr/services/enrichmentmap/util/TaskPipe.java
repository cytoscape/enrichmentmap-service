package ca.utoronto.tdccbr.services.enrichmentmap.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TaskPipe<T> {

	private T t;

	public Consumer<T> in() {
		return t -> { TaskPipe.this.t = t; };
	}
	
	public Supplier<T> out() {
		return () -> t;
	}
	
}
