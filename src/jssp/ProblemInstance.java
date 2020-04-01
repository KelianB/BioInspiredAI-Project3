package jssp;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a JSSP problem instance.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class ProblemInstance {	
	// Name of the problem instance
	private String name;
	
	// Store jobs as arrays of operations
	private Operation[][] jobs;

	// Also store jobs in machine:Operation maps for faster access
	private Map<Integer, Operation>[] jobMachineMaps;
		
	/**
	 * Create a new problem instance.
	 * @param name - The name of this problem instance
	 * @param jobs - The jobs, each represented as an array of operations
	 */
	@SuppressWarnings("unchecked")
	public ProblemInstance(String name, Operation[][] jobs) {
		this.name = name;
		this.jobs = jobs;
		this.jobMachineMaps = new Map[jobs.length];
		
		// Fill a machine:Operation map for each job
		for(int i = 0; i < jobs.length; i++) {
			Map<Integer, Operation> machineMap = new HashMap<Integer, Operation>();
			for(int j = 0; j < jobs[i].length; j++)
				machineMap.put(jobs[i][j].getMachine(), jobs[i][j]);
			jobMachineMaps[i] = machineMap;
		}
	}
	
	/**
	 * Get the number of jobs in this problem instance.
	 * @return the number of jobs
	 */
	public int getNumberOfJobs() {
		return this.jobs.length;
	}
	
	/**
	 * Get the number of operations in each job.
	 * @return the number of operations in each job (i.e. the number of machines)
	 */
	public int getOperationsPerJob() {
		return this.jobs[0].length;
	}
	
	/**
	 * Get the total number of operations in this problem instance.
	 * @return the total number of operations (i.e. the number of jobs times the number of machines)
	 */
	public int getTotalOperations() {
		return getNumberOfJobs() * getOperationsPerJob();
	}
	
	/**
	 * Get the machine a given operation must be performed on.
	 * @param job - The job index
	 * @param operation - The operation index, within the given job
	 * @return a machine index between 0 (inclusive) and the number of machines (exclusive)
	 */
	public int getMachine(int job, int operation) {
		return jobs[job][operation].getMachine();
	}
	
	/**
	 * Get the duration of a given operation.
	 * @param job - A job index
	 * @param operation - An operation index, within the given job
	 * @return a number of time units
	 */
	public int getDuration(int job, int operation) {
		return jobs[job][operation].getDuration();
	}
		
	/**
	 * Get the operation of the given job that must be performed on the given machine.
	 * @param job - A job index
	 * @param machine - A machine index
	 * @return the Operation that must be performed on the given machine, for the given job
	 */
	public Operation getOperationOnMachine(int job, int machine) {
		return jobMachineMaps[job].get(machine);
	}
		
	/**
	 * Get the name of this problem instance.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
