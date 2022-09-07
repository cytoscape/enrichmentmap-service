package ca.utoronto.tdccbr.services.enrichmentmap.task;

@FunctionalInterface
public interface Task {

	void run() throws Exception;
}
