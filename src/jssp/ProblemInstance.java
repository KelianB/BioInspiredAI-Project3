package jssp;

/**
 * Represents a JSSP problem instance.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class ProblemInstance {	
	// Name of the problem instance
	private String name;
	
	// Store jobs as arrays of operations
	private Operation[][] jobs;

	/**
	 * Create a new problem instance.
	 * @param name - The name of this problem instance
	 * @param jobs - The jobs, each represented as an array of operations
	 */
	public ProblemInstance(String name, Operation[][] jobs) {
		this.name = name;
		this.jobs = jobs;
		
		// Test print
		/*
		System.out.println("jobs: " + numJobs + ", machines: " + numMachines); 
		for(int i = 0; i < jobs.length; i++) {
			String str = "";
			for(int j = 0; j < jobs[i].length; j++)
				str += jobs[i][j] + ", ";
			System.out.println("Job " + i + ": " + str);
		}*/
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
	public int getNumberOfOperations() {
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
		for(int o = 0; o < jobs[job].length; o++) {
			if(jobs[job][o].getMachine() == machine)
				return jobs[job][o];
		}
		return null;
	}
	
	/**
	 * Get the name of this problem instance.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
