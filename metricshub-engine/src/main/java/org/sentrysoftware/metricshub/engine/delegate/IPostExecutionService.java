package org.sentrysoftware.metricshub.engine.delegate;

/**
 * Interface for post-execution services in the MetricsHub engine.
 * Implementations of this interface should encapsulate the necessary steps
 * for the successful execution of a post-business operation.
 *
 * Example:
 * <pre>
 * {@code
 *   public class IHardwarePostExecutionImpl implements IPostExecutionService {
 *
 *       public void run() {
 *           // Implementation specific to the business process
 *           // This may involve calling multiple lower-level services,
 *           // handling exceptions, and performing any required business logic.
 *           // ...
 *       }
 *   }
 * }
 * </pre>
 */
public interface IPostExecutionService {
	/**
	 * Executes the designated post-execution service.
	 * The implementation of this method should encapsulate the necessary
	 * steps for the successful execution of the post-business operation.
	 */
	void run();
}
