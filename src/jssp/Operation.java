package jssp;

/**
 * Class used to store an operation in the context of the Job Shop Scheduling Problem
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Operation {
	// The index of the machine on which this operation needs to be performed
	private int machine;
	
	// The time this operation requires
	private int duration;
	
	/**
	 * Create an Operation
	 * @param job - The index of the job this operation belongs to 
	 * @param machine - The index of the machine on which this operation needs to be performed
	 * @param duration - The time this operation requires
	 */
	public Operation(int machine, int duration) {
		this.machine = machine;
		this.duration = duration;
	}
	
	/**
	 * Get the index of the machine on which this operation needs to be performed 
	 * @return a machine index
	 */
	public int getMachine() {
		return machine;
	}
	
	/**
	 * Get the time this operation requires
	 * @return a duration
	 */
	public int getDuration() {
		return duration;
	}
}