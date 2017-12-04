import desmoj.core.simulator.ProcessQueue;

public interface TransportStrategy {

	/**
	 * Implement this method in a class that implements this interface to define
	 * the algorithm of the strategy that finds the jobs for the transporters
	 * and is used by transporter control.
	 */
	public void schedule(ProcessQueue<?> transporters, ProcessQueue<?> stations);
}